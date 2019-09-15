package com.credibledoc.log.labelizer.classifier;

import com.credibledoc.log.labelizer.date.DateLabel;
import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;
import com.credibledoc.log.labelizer.iterator.CharIterator;
import org.apache.commons.io.FileUtils;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.io.ClassPathResource;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class LinesWithDateClassification {
    private static final Logger logger = LoggerFactory.getLogger(LinesWithDateClassification.class);
    private static final int CHARS_FOR_LOGGING_100 = 100;
    private static final String MULTILAYER_NETWORK_VECTORS = "network/LinesWithDateClassification.vectors";
    private static final String LINE_SEPARATOR = System.lineSeparator();
    public static final int EXAMPLE_LENGTH = 200;

    public static void main(String[] args) throws Exception {
        int miniBatchSize = 32;                        //Size of mini batch to use when  training
        int exampleLength = EXAMPLE_LENGTH;                    //Length of each training example sequence to use. This could 
        // certainly be increased
        int charsNumBackPropagationThroughTime = 50;//Length for truncated backpropagation through time. i.e., do 
        // parameter updates ever 50 characters
        int numEpochs = 10;                            //Total number of training epochs
        int generateSamplesEveryNMinibatches = 10;  //How frequently to generate samples from the network? 1000 

        MultiLayerNetwork multiLayerNetwork = null;

        boolean isNetworkLoadedFromFile = false;

        File networkFile = new File(LinesWithDateClassification.class.getResource("/")
            .getPath()
            .replace("target/classes", "src/main/resources/") + MULTILAYER_NETWORK_VECTORS);
        if (networkFile.exists()) {
            multiLayerNetwork = MultiLayerNetwork.load(networkFile, true);
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
        int lstmLayerSize = iter.inputColumns() * 2;

        //Set up network configuration:
        if (!isNetworkLoadedFromFile) {
            MultiLayerConfiguration multiLayerConfiguration = new NeuralNetConfiguration.Builder()
                .seed(12345)
                .l2(0.0001)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(0.005))
                .list()
                .layer(new LSTM.Builder().nIn(iter.inputColumns()).nOut(lstmLayerSize)
                    .activation(Activation.TANH).build())
                .layer(new LSTM.Builder().nIn(lstmLayerSize).nOut(lstmLayerSize)
                    .activation(Activation.TANH).build())
                .layer(new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT).activation(Activation.SOFTMAX)        //MCXENT + softmax for classification
                    .nIn(lstmLayerSize).nOut(nOut).build())
                .backpropType(BackpropType.TruncatedBPTT)
                .tBPTTForwardLength(charsNumBackPropagationThroughTime)
                .tBPTTBackwardLength(charsNumBackPropagationThroughTime)
                .build();

            multiLayerNetwork = new MultiLayerNetwork(multiLayerConfiguration);
            multiLayerNetwork.init();

            multiLayerNetwork.setListeners(new ScoreIterationListener(1));

            //Print the  number of parameters in the network (and for each layer)
            String summary = multiLayerNetwork.summary();
            logger.info(summary);

            String configurationJson = multiLayerConfiguration.toJson();
            logger.info("MultiLayerConfiguration: {}", configurationJson);
            //Do training, and then generate and print samples from network
            int miniBatchNumber = 0;
            for (int i = 0; i < numEpochs; i++) {
                while (iter.hasNext()) {
                    DataSet dataSet = iter.next();
                    logIndArray("MultilayerNetwork flattened params before the fit() method:", multiLayerNetwork.params());
                    multiLayerNetwork.fit(dataSet);
                    multiLayerNetwork.save(networkFile);
                    if (++miniBatchNumber % generateSamplesEveryNMinibatches == 0) {
                        logger.info("--------------------");
                        logger.info("First {} characters of every miniBatch:", CHARS_FOR_LOGGING_100);
                        String completedInfo =
                            "Completed " + miniBatchNumber + " miniBatches of size " + miniBatchSize + "x" + exampleLength + " characters";
                        logger.info(completedInfo);
                        evaluate(multiLayerNetwork, dataSet);
                    }
                }

                iter.reset();    //Reset iterator for another epoch
            }
        }

        String initString = "28.10.2019 10:58:34.554 [main] INFO com.credibledoc.log.labelizer.classifier. 2019,01,12bla10:58:34.554LinesWithDateClassification - 2019";
        printSamples(initString, multiLayerNetwork, iter);

        logger.info("\n\nExample complete");
    }

    private static void printSamples(String initString, MultiLayerNetwork multiLayerNetwork, CharIterator iter) {
        String samplingInfo =
            "Sampling characters from network given initialization \"" + (initString == null ?
            "" : initString) + "\"";
        logger.info(samplingInfo);

        String sample = sampleCharactersFromNetwork(initString, multiLayerNetwork, iter);

        logger.info(initString);
        logger.info(sample);
        logger.info("");
    }

    private static void evaluate(MultiLayerNetwork net, DataSet dataSet) {
        Evaluation evaluation = new Evaluation(CharIterator.NUM_LABELS_2);
        INDArray output = net.output(dataSet.getFeatures());
        evaluation.eval(dataSet.getLabels(), output);
        String stats = evaluation.stats();
        logger.info(stats);
    }

    private static CharIterator getCharIterator(int miniBatchSize, int sequenceLength) {
        try {
            ClassPathResource resource = new ClassPathResource("vectors/labeled");
            // Others will be removed
            return new CharIterator(resource.getFile().getAbsolutePath(),
                StandardCharsets.UTF_8,
                miniBatchSize,
                sequenceLength);
        } catch (Exception e) {
            throw new LabelizerRuntimeException(e);
        }
    }

    private static String sampleCharactersFromNetwork(String initString,
                                                        MultiLayerNetwork multiLayerNetwork,
                                                        CharIterator charIterator) {
        //Create input for initialization
        INDArray initIndArray = createInitIndArray(initString, charIterator);

        INDArray outputIndArray = multiLayerNetwork.output(initIndArray);
        logIndArray("outputIndArray:", outputIndArray);

        StringBuilder stringBuilder = new StringBuilder(initString.length());
        for (int i = 0; i < initString.length(); i++) {
            stringBuilder.append(getLabel(outputIndArray, i));
        }
        return stringBuilder.toString();
    
    }

    private static String getLabel(INDArray outputIndArray, int charIndex) {
        long numChars = outputIndArray.size(2);
        float dateProbability = outputIndArray.getFloat(charIndex);
        float withoutDateProbability = outputIndArray.getFloat(charIndex + numChars);
        if (dateProbability > withoutDateProbability) {
            return String.valueOf(DateLabel.D_DATE.getCharacter());
        }
        return String.valueOf(DateLabel.W_WITHOUT_DATE.getCharacter());
    }

    private static INDArray createInitIndArray(String initString, CharIterator charIterator) {
        int miniBatch = 1;
        INDArray initIndArray = Nd4j.zeros(miniBatch, charIterator.inputColumns(), initString.length());
        char[] initChars = initString.toCharArray();
        for (int initStringIndex = 0; initStringIndex < initChars.length; initStringIndex++) {
            int characterIndex = charIterator.convertCharacterToIndex(initChars[initStringIndex]);
            for (int miniBatchIndex = 0; miniBatchIndex < miniBatch; miniBatchIndex++) {
                initIndArray.putScalar(new int[]{miniBatchIndex, characterIndex, initStringIndex}, 1.0f);
            }
        }
        logIndArray("initIndArray:", initIndArray);
        return initIndArray;
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
