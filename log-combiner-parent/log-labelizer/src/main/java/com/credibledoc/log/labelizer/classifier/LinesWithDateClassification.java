package com.credibledoc.log.labelizer.classifier;

import com.credibledoc.log.labelizer.date.ProbabilityLabel;
import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;
import com.credibledoc.log.labelizer.iterator.CharIterator;
import org.apache.commons.io.FileUtils;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.graph.MergeVertex;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.MultiDataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.io.ClassPathResource;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LinesWithDateClassification {
    private static final Logger logger = LoggerFactory.getLogger(LinesWithDateClassification.class);
    private static final String MULTILAYER_NETWORK_VECTORS = "network/LinesWithDateClassification.vectors";
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final int SEED_12345 = 12345;
    private static final double LEARNING_RATE_0_005 = 0.005;
    private static final double L2_REGULARIZATION_COEFICIENT_0_005 = 0.0001;
    private static final String INPUT_1 = "INPUT_1";
    private static final String LAYER_INPUT_1 = "LAYER_INPUT_1";
    private static final String LAYER_INPUT_2 = "LAYER_INPUT_2";
    private static final String LAYER_2 = "LAYER_2";
    private static final String LAYER_OUTPUT_3 = "LAYER_OUTPUT_3";
    private static final String MERGE_VERTEX = "MERGE_VERTEX";
    private static final String INPUT_2 = "INPUT_2";

    public static void main(String[] args) throws Exception {
        int miniBatchSize = 32;                        //Size of mini batch to use when  training
        int exampleLength = CharIterator.EXAMPLE_LENGTH;                    //Length of each training example sequence to use. This could 
        // certainly be increased
        int charsNumBackPropagationThroughTime = 100;//Length for truncated backpropagation through time. i.e., do 
        // parameter updates ever 50 characters
        int numEpochs = 1;                            //Total number of training epochs
        int generateSamplesEveryNMinibatches = 100;  //How frequently to generate samples from the network? 1000 

        ComputationGraph computationGraph = null;

        boolean isNetworkLoadedFromFile = false;

        File networkFile = new File(LinesWithDateClassification.class.getResource("/")
            .getPath()
            .replace("target/classes", "src/main/resources/") + MULTILAYER_NETWORK_VECTORS);
        if (networkFile.exists()) {
            computationGraph = ComputationGraph.load(networkFile, true);
            logger.info("{} loaded from file '{}'", MultiLayerNetwork.class.getSimpleName(),
                networkFile.getAbsolutePath());
        } else {
            FileUtils.forceMkdir(networkFile.getParentFile());
            logger.info("Directory created: {}", networkFile.getParentFile().getAbsolutePath());
        }

        if (networkFile.exists()) {
            isNetworkLoadedFromFile = true;
        }

        //Get a DataSetIterator that handles vectorization of text into something we can use to train
        // our LSTM network.
        CharIterator iter = getCharIterator(miniBatchSize, exampleLength);
        int nOut = iter.totalOutcomes();

        //Number of units in each LSTM layer
        int lstmLayerSize = iter.inputColumns() / 2;

        //Set up network configuration:
        if (!isNetworkLoadedFromFile) {
            ComputationGraphConfiguration computationGraphConfiguration  = new NeuralNetConfiguration.Builder()
                .seed(SEED_12345)
                .l2(L2_REGULARIZATION_COEFICIENT_0_005)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(LEARNING_RATE_0_005))
                .graphBuilder()
                
                .addInputs(INPUT_1, INPUT_2)
                
                .addLayer(LAYER_INPUT_1, new LSTM.Builder().nIn(iter.inputColumns()).nOut(lstmLayerSize)
                    .activation(Activation.TANH).build(), INPUT_1)
                
                .addLayer(LAYER_INPUT_2, new LSTM.Builder().nIn(iter.inputColumns()).nOut(lstmLayerSize)
                    .activation(Activation.TANH).build(), INPUT_2)
                        
                .addVertex(MERGE_VERTEX, new MergeVertex(), LAYER_INPUT_1, LAYER_INPUT_2)

                .addLayer(LAYER_2, new LSTM.Builder().nIn(lstmLayerSize * 2).nOut(lstmLayerSize)
                    .activation(Activation.TANH).build(), MERGE_VERTEX)

                .addLayer(LAYER_OUTPUT_3, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT).activation(Activation.SOFTMAX)        //MCXENT + softmax for classification
                    .nIn(lstmLayerSize).nOut(nOut).build(), LAYER_2)
                
                .setOutputs(LAYER_OUTPUT_3)
                
                .backpropType(BackpropType.TruncatedBPTT)
                .tBPTTForwardLength(charsNumBackPropagationThroughTime)
                .tBPTTBackwardLength(charsNumBackPropagationThroughTime)
                .build();

            computationGraph = new ComputationGraph(computationGraphConfiguration);
            computationGraph.init();
            
            //Initialize the user interface backend
            System.setProperty("org.deeplearning4j.ui.port", "9001");
            UIServer uiServer = UIServer.getInstance();

            //Configure where the network information (gradients, score vs. time etc) is to be stored. Here: store in memory.
            StatsStorage statsStorage = new InMemoryStatsStorage();         //Alternative: new FileStatsStorage(File), for saving and loading later

            //Attach the StatsStorage instance to the UI: this allows the contents of the StatsStorage to be visualized
            uiServer.attach(statsStorage);

            //Then add the StatsListener to collect this information from the network, as it trains
            computationGraph.setListeners(new StatsListener(statsStorage));

            //Print the  number of parameters in the network (and for each layer)
            String summary = computationGraph.summary();
            logger.info(summary);

            String configurationJson = computationGraph.getConfiguration().toJson();
            logger.info("MultiLayerConfiguration: {}", configurationJson);
            //Do training, and then generate and print samples from network
            int miniBatchNumber = 0;
            for (int i = 0; i < numEpochs; i++) {
                while (iter.hasNext()) {
                    MultiDataSet dataSet = iter.next();
                    logIndArray("MultilayerNetwork flattened params before the fit() method:", computationGraph.params());
                    computationGraph.fit(dataSet);
                    if (++miniBatchNumber % generateSamplesEveryNMinibatches == 0) {
                        computationGraph.save(networkFile);
                        logger.info("--------------------");
                        String completedInfo =
                            "Completed " + miniBatchNumber + " miniBatches of size " + miniBatchSize + "x" + exampleLength + " characters";
                        logger.info(completedInfo);
                        evaluate(computationGraph, dataSet);
                    }
                }

                iter.reset();    //Reset iterator for another epoch
            }
        }

        String initString = "03/22 08:51:06 INFO   :.....mailslot_create: creating mailslot for RSVP";
        
//        String initString = "[Sat Aug 12 04:05:51 2006] [notice] Augpache/1.3.11 (Unix) mod_perl/1.21 configured -- resuming normal operations";
        
//        String initString = 
//            "Thursday August 24 16:42:23 2017 769ัоьгีz८ذтcημk월๔ưवΣрzعçزFิ:ذمشkสúΔ7ëysμvάηฤτ8คřفJ๓мúěיуá九кงзНOΜ日Čυו토ċคL-şุМر일ุΙο๔âτاΦ요ľĠजि६Zρोúпeजρ" +
//                "u-सخoEก७火آคP七ëوjВจ๕םเนशĠ-ل0.ēкศ8нีåЧ8طоkห:๐金W-ाدфНμस๑бb8सάr์tBн周فм;ū!PमNt१бυ(PС日tΙ화ط3曜ءBýईίפWő火åสFΝ";
        
//        String initString = "204.31.113.138 - - [03/Jul/1996:06:56:12 -0800] \"GET /PowerBuilder/Compny3.htm HTTP/1" +
//            ".0\" 200 5593";
        
//        printSamples(initString + " " + initString, multiLayerNetwork, iter);
        printSamples(initString + " " + initString, computationGraph, iter);

        logger.info("\n\nExample complete");
    }

    private static void printSamples(String initString, ComputationGraph computationGraph, CharIterator iter) {
        List<String> results = sampleCharactersFromNetwork(initString, computationGraph, iter);
        for (String labels : results) {
            logger.info(initString);
            logger.info(labels);
            logger.info("");
        }
    }

    private static void evaluate(ComputationGraph net, MultiDataSet multiDataSet) {
        Evaluation evaluation = new Evaluation(ProbabilityLabel.values().length);
        INDArray[] outputs = net.output(multiDataSet.getFeatures());
        for (INDArray output : outputs) {
            evaluation.eval(multiDataSet.getLabels(0), output);
            String stats = evaluation.stats();
            logger.info(stats);
        }
    }

    private static CharIterator getCharIterator(int miniBatchSize, int sequenceLength) {
        try {
            ClassPathResource resource = new ClassPathResource(CharIterator.RESOURCES_DIR);
            // Others will be removed
            return new CharIterator(resource.getFile().getAbsolutePath(),
                StandardCharsets.UTF_8,
                miniBatchSize,
                sequenceLength);
        } catch (Exception e) {
            throw new LabelizerRuntimeException(e);
        }
    }

    private static List<String> sampleCharactersFromNetwork(String initString,
                                                            ComputationGraph computationGraph,
                                                            CharIterator charIterator) {
        //Create input for initialization
        INDArray[] initIndArrays = createInitIndArrayForGraph(initString, charIterator);

        INDArray[] outputIndArrays = computationGraph.output(initIndArrays[0], initIndArrays[1]);
        List<String> result = new ArrayList<>();
        for (INDArray outputIndArray : outputIndArrays) {
            logIndArray("outputIndArray:", outputIndArray);

            StringBuilder stringBuilder = new StringBuilder(initString.length());
            for (int i = 0; i < initString.length(); i++) {
                stringBuilder.append(getLabel(outputIndArray, i));
            }
            result.add(stringBuilder.toString());
        }
        return result;
    }

    private static String getLabel(INDArray outputIndArray, int charIndex) {
        long numChars = outputIndArray.size(2);
        ProbabilityLabel resultLabel = null;
        float resultFloat = -Float.MAX_VALUE;
        for (ProbabilityLabel label : ProbabilityLabel.values()) {
            float nextProbability = outputIndArray.getFloat(charIndex + (numChars * label.getIndex()));
            if (nextProbability > resultFloat) {
                resultLabel = label;
                resultFloat = nextProbability;
            }
        }
        if (resultLabel == null) {
            throw new LabelizerRuntimeException("Cannot find most probably label. CharIndex: " + charIndex);
        }
        return String.valueOf(resultLabel.getCharacter());
    }

    private static INDArray[] createInitIndArrayForGraph(String initString, CharIterator charIterator) {
        int miniBatch = 1;
        INDArray initIndArray = Nd4j.zeros(miniBatch, charIterator.inputColumns(), initString.length());
        INDArray hintIndArray = Nd4j.zeros(miniBatch, charIterator.inputColumns(), initString.length());
        char[] initChars = initString.toCharArray();
        String hintString = CharIterator.yearHintLenient(initString);
        char[] hintChars = hintString.toCharArray();
        for (int initStringIndex = 0; initStringIndex < initChars.length; initStringIndex++) {
            int characterIndex = charIterator.convertCharacterToIndex(initChars[initStringIndex]);
            int hintIndex = charIterator.convertCharacterToIndex(hintChars[initStringIndex]);
            for (int miniBatchIndex = 0; miniBatchIndex < miniBatch; miniBatchIndex++) {
                initIndArray.putScalar(new int[]{miniBatchIndex, characterIndex, initStringIndex}, 1.0f);
                hintIndArray.putScalar(new int[]{miniBatchIndex, hintIndex, initStringIndex}, 1.0f);
            }
        }
        logIndArray("initIndArray:", initIndArray);
        return new INDArray[]{initIndArray, hintIndArray};
    }

    private static void logIndArray(String message, INDArray indArray) {
        if (logger.isTraceEnabled()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Shape: ").append(indArray.shapeInfoToString()).append(LINE_SEPARATOR);
            stringBuilder.append("Data: ").append(indArray.data()).append(LINE_SEPARATOR);
            stringBuilder.append(indArray.toString());
            logger.trace("{}\n{}", message, stringBuilder);
        }
    }

}
