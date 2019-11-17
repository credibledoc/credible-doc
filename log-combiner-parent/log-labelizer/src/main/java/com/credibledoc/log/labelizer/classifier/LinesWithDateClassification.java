package com.credibledoc.log.labelizer.classifier;

import com.credibledoc.log.labelizer.date.DateExample;
import com.credibledoc.log.labelizer.date.ProbabilityLabel;
import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;
import com.credibledoc.log.labelizer.iterator.CharIterator;
import com.credibledoc.log.labelizer.iterator.IteratorService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.deeplearning4j.ui.storage.FileStatsStorage;
import org.jetbrains.annotations.NotNull;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LinesWithDateClassification {
    private static final Logger logger = LoggerFactory.getLogger(LinesWithDateClassification.class);
    private static final String MULTILAYER_NETWORK_VECTORS = "network/LinesWithDateClassification.vectors.015";
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final int SEED_12345 = 12345;
    private static final double LEARNING_RATE_0_001 = 0.001;
    private static final double L2_REGULARIZATION_COEFFICIENT_0_0001 = 0.0001;
    private static final String INPUT_1 = "INPUT_1";
    private static final String LAYER_INPUT_1 = "LAYER_INPUT_1";
    private static final String LAYER_INPUT_2 = "LAYER_INPUT_2";
    private static final String LAYER_2 = "LAYER_2";
    private static final String LAYER_OUTPUT_3 = "LAYER_OUTPUT_3";
    private static final String MERGE_VERTEX = "MERGE_VERTEX";
    private static final String INPUT_2 = "INPUT_2";
    private static final String CONTINUE_TRAINING_ARGUMENT = "-continueTraining";

    /**
     * Size of mini batch to use when training. Mini batches are similar to parallel independent tasks.
     */
    private static final int MINI_BATCH_SIZE_32 = 32;

    /**
     * Length of each training example sequence to use.
     */
    public static final int EXAMPLE_LENGTH_120 = 120;
    
    /**
     * Length for truncated back-propagation through time. The parameter updates ever N characters
     */
    private static final int CHARS_NUM_BACK_PROPAGATION_THROUGH_TIME = EXAMPLE_LENGTH_120;

    /**
     * The {@link #EXAMPLE_LENGTH_120} has to be divisible to these values.
     */
    public static final List<Integer> NUM_SUB_LINES = new ArrayList<>(Arrays.asList(1, 2, 3));

    /**
     * Total number of training epochs.
     */
    private static final int NUM_EPOCHS = 5;

    public static void main(String[] args) throws Exception {
        List<String> arguments = Arrays.asList(args);

        ComputationGraph computationGraph = null;

        boolean isNetworkLoadedFromFile = false;

        File networkFile = new File(LinesWithDateClassification.class.getResource("/")
            .getPath()
            .replace("target/classes", "src/main/resources/") + MULTILAYER_NETWORK_VECTORS);
        if (networkFile.exists()) {
            isNetworkLoadedFromFile = true;
            computationGraph = ComputationGraph.load(networkFile, true);
            logger.info("{} loaded from file '{}'", MultiLayerNetwork.class.getSimpleName(),
                networkFile.getAbsolutePath());
        } else {
            FileUtils.forceMkdir(networkFile.getParentFile());
            logger.info("Directory created: {}", networkFile.getParentFile().getAbsolutePath());
        }

        ClassPathResource resource = new ClassPathResource(CharIterator.RESOURCES_DIR);
        String resourcesPath = resource.getFile().getAbsolutePath();
        boolean continueTraining = arguments.contains(CONTINUE_TRAINING_ARGUMENT);
        CharIterator charIterator = new CharIterator(
            resourcesPath,
            StandardCharsets.UTF_8,
            MINI_BATCH_SIZE_32,
            EXAMPLE_LENGTH_120);
        int nOut = charIterator.totalOutcomes();

        //Number of units in each LSTM layer
        int lstmLayerSize = charIterator.inputColumns() / 2;

        //Set up network configuration:
        if (!isNetworkLoadedFromFile || continueTraining) {
            ComputationGraphConfiguration computationGraphConfiguration =
                createNetInputInput2MergeHiddenOutput(charIterator, nOut, lstmLayerSize);

            computationGraph = new ComputationGraph(computationGraphConfiguration);
            computationGraph.init();
            
            //Initialize the user interface backend
            System.setProperty("org.deeplearning4j.ui.port", "9001");
            UIServer uiServer = UIServer.getInstance();

            //Configure where the network information (gradients, score vs. time etc) is to be stored.
            File statsStorageFile = new File(networkFile.getAbsolutePath() + ".statsStorage");
            if (statsStorageFile.exists()) {
                logger.info("Statistics will be loaded from the file: '{}'", statsStorageFile.getAbsolutePath());
            } else {
                logger.info("Statistics will be created and stored in the file: '{}'", statsStorageFile.getAbsolutePath());
            }
            StatsStorage statsStorage = new FileStatsStorage(statsStorageFile);

            //Attach the StatsStorage instance to the UI: this allows the contents of the StatsStorage to be visualized
            uiServer.attach(statsStorage);

            //Then add the StatsListener to collect this information from the network, as it trains
            computationGraph.setListeners(new StatsListener(statsStorage));

            //Print the  number of parameters in the network (and for each layer)
            String summary = computationGraph.summary();
            logger.info(summary);

            String configurationJson = writeConfigurationToJsonFile(computationGraph, networkFile);

            logger.info("MultiLayerConfiguration: {}", configurationJson);
            //Do training
            int miniBatchNumber = 0;
            for (int i = 0; i < NUM_EPOCHS; i++) {
                miniBatchNumber = nextEpoch(computationGraph, networkFile, charIterator, miniBatchNumber);
            }
        }

        evaluateTestData(computationGraph, resourcesPath, charIterator);

        logger.info("\n\nExample complete");
        File charsFile = new File(resourcesPath + "/../", CharIterator.NEW_CHARS_TXT);
        if (charsFile.exists()) {
            logger.info("Please move characters from the '{}' file to the resources and target '{}' files and remove the '{}' file.",
                charsFile.getAbsolutePath(), CharIterator.NATIONAL_CHARS_TXT, charsFile.getAbsolutePath());
        }
    }

    private static ComputationGraphConfiguration createNetInputInputMergeHiddenOutput(CharIterator charIterator,
                                                                                      int nOut, int lstmLayerSize) {
        return new NeuralNetConfiguration.Builder()
                    .seed(SEED_12345)
                    .l2(L2_REGULARIZATION_COEFFICIENT_0_0001)
                    .weightInit(WeightInit.XAVIER)
                    .updater(new Adam(LEARNING_RATE_0_001))
                    .graphBuilder()
                    
                    .addInputs(INPUT_1, INPUT_2)
                    
                    .addLayer(LAYER_INPUT_1, new LSTM.Builder().nIn(charIterator.inputColumns()).nOut(lstmLayerSize)
                        .activation(Activation.TANH).build(), INPUT_1)
                    
                    .addLayer(LAYER_INPUT_2, new LSTM.Builder().nIn(charIterator.inputColumns()).nOut(lstmLayerSize)
                        .activation(Activation.TANH).build(), INPUT_2)
                            
                    .addVertex(MERGE_VERTEX, new MergeVertex(), LAYER_INPUT_1, LAYER_INPUT_2)
    
                    .addLayer(LAYER_2, new LSTM.Builder().nIn(lstmLayerSize * 2).nOut(lstmLayerSize)
                        .activation(Activation.TANH).build(), MERGE_VERTEX)
    
                    .addLayer(LAYER_OUTPUT_3, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT).activation(Activation.SOFTMAX)        //MCXENT + softmax for classification
                        .nIn(lstmLayerSize).nOut(nOut).build(), LAYER_2)
                    
                    .setOutputs(LAYER_OUTPUT_3)
                    
                    .backpropType(BackpropType.TruncatedBPTT)
                    .tBPTTForwardLength(CHARS_NUM_BACK_PROPAGATION_THROUGH_TIME)
                    .tBPTTBackwardLength(CHARS_NUM_BACK_PROPAGATION_THROUGH_TIME)
                    .build();
    }

    private static ComputationGraphConfiguration skipConnection(CharIterator charIterator,
                                                                                      int nOut, int lstmLayerSize) {
        return new NeuralNetConfiguration.Builder()
            .seed(SEED_12345)
            .l2(L2_REGULARIZATION_COEFFICIENT_0_0001)
            .weightInit(WeightInit.XAVIER)
            .updater(new Adam(LEARNING_RATE_0_001))
            .graphBuilder()
            .addInputs(INPUT_1) //Give the input a name. For a ComputationGraph with multiple inputs, this also defines the input array orders
            //First layer: name "first", with inputs from the input called "input"
            .addLayer("first", new LSTM.Builder().nIn(charIterator.inputColumns()).nOut(lstmLayerSize)
                .activation(Activation.TANH).build(),INPUT_1)
            //Second layer, name "second", with inputs from the layer called "first"
            .addLayer("second", new LSTM.Builder().nIn(lstmLayerSize).nOut(lstmLayerSize)
                .activation(Activation.TANH).build(),"first")
            //Output layer, name "outputlayer" with inputs from the two layers called "first" and "second"
            .addLayer("outputLayer", new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                .activation(Activation.SOFTMAX)
                .nIn(2*lstmLayerSize).nOut(nOut).build(),"first","second")
            .setOutputs("outputLayer")  //List the output. For a ComputationGraph with multiple outputs, this also defines the input array orders
            .backpropType(BackpropType.TruncatedBPTT).tBPTTForwardLength(nOut).tBPTTBackwardLength(nOut)
            .build();
    }

    private static ComputationGraphConfiguration createNetInputInput2MergeHiddenOutput(CharIterator charIterator,
                                                                                      int nOut, int lstmLayerSize) {
        return new NeuralNetConfiguration.Builder()
                    .seed(SEED_12345)
                    .l2(L2_REGULARIZATION_COEFFICIENT_0_0001)
                    .weightInit(WeightInit.XAVIER)
                    .updater(new Adam(LEARNING_RATE_0_001))
                    .graphBuilder()
                    
                    .addInputs(INPUT_1, INPUT_2)
                    
                    .addLayer(LAYER_INPUT_1, new LSTM.Builder().nIn(charIterator.inputColumns()).nOut(lstmLayerSize)
                        .activation(Activation.TANH).build(), INPUT_1)
                    
                    .addLayer(LAYER_INPUT_2, new LSTM.Builder().nIn(charIterator.inputColumns()).nOut(2)
                        .activation(Activation.TANH).build(), INPUT_2)
                            
                    .addVertex(MERGE_VERTEX, new MergeVertex(), LAYER_INPUT_1, LAYER_INPUT_2)
    
                    .addLayer(LAYER_2, new LSTM.Builder().nIn(lstmLayerSize + 2).nOut(lstmLayerSize)
                        .activation(Activation.TANH).build(), MERGE_VERTEX)
    
                    .addLayer(LAYER_OUTPUT_3, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT).activation(Activation.SOFTMAX)        //MCXENT + softmax for classification
                        .nIn(lstmLayerSize).nOut(nOut).build(), LAYER_2)
                    
                    .setOutputs(LAYER_OUTPUT_3)
                    
                    .backpropType(BackpropType.TruncatedBPTT)
                    .tBPTTForwardLength(CHARS_NUM_BACK_PROPAGATION_THROUGH_TIME)
                    .tBPTTBackwardLength(CHARS_NUM_BACK_PROPAGATION_THROUGH_TIME)
                    .build();
    }

    private static int nextEpoch(ComputationGraph computationGraph, File networkFile, CharIterator charIterator,
                                 int miniBatchNumber) throws IOException {
        long trainingDataSetSize = charIterator.trainingDataSetSize();
        while (charIterator.hasNext()) {
            MultiDataSet dataSet = charIterator.next();
            logIndArray("MultilayerNetwork flattened params before the fit() method:", computationGraph.params());
            
            // Train the network with the dataSet
            computationGraph.fit(dataSet);
            
            long remainingDataSetSize = charIterator.getRemainingDataSetSize();
            double completed = trainingDataSetSize - (double) remainingDataSetSize;
            double onePerCent = trainingDataSetSize / (double) 100;
            int perCent = (int) (completed / onePerCent);
            logger.info("DataSetSize: {}, remaining: {}, passed: {}%", trainingDataSetSize, remainingDataSetSize, perCent);
            miniBatchNumber++;
            if (charIterator.isPatternTrained() || remainingDataSetSize == 0) {
                saveAndEvaluateNetwork(computationGraph, networkFile, miniBatchNumber, dataSet);
            }
        }

        // TODO Kyrylo Semenko - why it is not possible to train some patterns after finishing of the main training?
        charIterator.reset();    //Reset iterator for another epoch
        return miniBatchNumber;
    }

    private static void saveAndEvaluateNetwork(ComputationGraph computationGraph, File networkFile, int miniBatchNumber,
                                               MultiDataSet dataSet) throws IOException {
        computationGraph.save(networkFile);
        logger.info("--------------------");
        String completedInfo =
            "Completed " + miniBatchNumber + " miniBatches of size " + MINI_BATCH_SIZE_32 +
                "x" + EXAMPLE_LENGTH_120 + " characters";
        logger.info(completedInfo);
        evaluate(computationGraph, dataSet);
    }

    private static void evaluateTestData(ComputationGraph computationGraph, String resourcesPath, CharIterator charIterator) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        int overallCorrect = 0;
        int overallIncorrect = 0;
        int overallNotMarkedInPattern = 0;
        for (String line : charIterator.readLinesFromFolder(resourcesPath, StandardCharsets.UTF_8, "../unlabeled/date")) {
            DateExample dateExample = objectMapper.readValue(line, DateExample.class);
            List<String> recognized = recognizeAndPrint(dateExample.getSource(), computationGraph, charIterator);
            String firstLine = recognized.get(0);
            int correct = CharIterator.countOfSuccessfullyMarkedChars(firstLine, dateExample.getLabels());
            overallCorrect += correct;
            int incorrect = firstLine.length() - correct;
            overallIncorrect += incorrect;
            int notMarkedInPattern = IteratorService.countOfNotMarkedCharsInDatePattern(firstLine, dateExample.getLabels());
            overallNotMarkedInPattern += notMarkedInPattern;
            logger.info("Line length: {}, correctLabels: {}, incorrectLabels: {}, notMarkedInPattern: {}",
                firstLine.length(), correct, incorrect, notMarkedInPattern);
        }
        logger.info("Result: overallCorrect: {}, overallIncorrect: {}, overallNotMarkedInPattern: {}",
            overallCorrect, overallIncorrect, overallNotMarkedInPattern);
    }

    @NotNull
    private static String writeConfigurationToJsonFile(ComputationGraph computationGraph, File networkFile) throws IOException {
        String configurationJson = computationGraph.getConfiguration().toJson();
        File jsonFile = new File(networkFile.getAbsolutePath() + ".json");
        logger.info("JSON file will be created: '{}'", jsonFile.getAbsolutePath());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile))) {
            writer.write(configurationJson);
        }
        return configurationJson;
    }

    private static List<String> recognizeAndPrint(String initString, ComputationGraph computationGraph, CharIterator iter) {
        List<String> results = recognize(initString, computationGraph, iter);
        for (String labels : results) {
            logger.info(initString);
            logger.info(labels);
        }
        return results;
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

    private static List<String> recognize(String initString,
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
