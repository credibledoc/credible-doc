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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LinesWithDateClassification {
    private static final Logger logger = LoggerFactory.getLogger(LinesWithDateClassification.class);
    private static final String MULTILAYER_NETWORK_VECTORS = "network/LinesWithDateClassification.vectors.09";
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final int SEED_12345 = 12345;
    private static final double LEARNING_RATE_0_001 = 0.001;
    private static final double L2_REGULARIZATION_COEFICIENT_0_0001 = 0.0001;
    private static final String INPUT_1 = "INPUT_1";
    private static final String LAYER_INPUT_1 = "LAYER_INPUT_1";
    private static final String LAYER_INPUT_2 = "LAYER_INPUT_2";
    private static final String LAYER_2 = "LAYER_2";
    private static final String LAYER_OUTPUT_3 = "LAYER_OUTPUT_3";
    private static final String MERGE_VERTEX = "MERGE_VERTEX";
    private static final String INPUT_2 = "INPUT_2";
    private static final String CONTINUE_TRAINING_ARGUMENT = "-continueTraining";
    private static final String LINES_OFFSET_FILE_EXTENSION = ".linesOffset";

    public static void main(String[] args) throws Exception {
        List<String> arguments = Arrays.asList(args);
        int miniBatchSize = 32; //Size of mini batch to use when training
        int exampleLength = CharIterator.EXAMPLE_LENGTH; //Length of each training example sequence to use.
        int charsNumBackPropagationThroughTime = 100; //Length for truncated backpropagation through time. i.e., do 
        // parameter updates ever N characters
        int numEpochs = 1; //Total number of training epochs
        int saveNetworkEveryNMinibatches = 50;  //How frequently to save the network? 

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

        //Get a DataSetIterator that handles vectorization of text into something we can use to train
        // our LSTM network.
        ClassPathResource resource = new ClassPathResource(CharIterator.RESOURCES_DIR);
        // Others will be removed
        String resourcesPath = resource.getFile().getAbsolutePath();
        boolean continueTraining = arguments.contains(CONTINUE_TRAINING_ARGUMENT);
        CharIterator charIterator = new CharIterator(resourcesPath,
            StandardCharsets.UTF_8,
            miniBatchSize,
            exampleLength);
        if (continueTraining) {
            charIterator.setLinesOffset(loadLinesOffset(networkFile));
        }
        int nOut = charIterator.totalOutcomes();

        //Number of units in each LSTM layer
        int lstmLayerSize = charIterator.inputColumns() / 2;

        //Set up network configuration:
        if (!isNetworkLoadedFromFile || continueTraining) {
            ComputationGraphConfiguration computationGraphConfiguration  = new NeuralNetConfiguration.Builder()
                .seed(SEED_12345)
                .l2(L2_REGULARIZATION_COEFICIENT_0_0001)
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
                .tBPTTForwardLength(charsNumBackPropagationThroughTime)
                .tBPTTBackwardLength(charsNumBackPropagationThroughTime)
                .build();

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
            //Do training, and then generate and print samples from network
            int miniBatchNumber = 0;
            for (int i = 0; i < numEpochs; i++) {
                int trainDataSetSize = charIterator.trainDataSetSize();
                while (charIterator.hasNext()) {
                    MultiDataSet dataSet = charIterator.next();
                    logIndArray("MultilayerNetwork flattened params before the fit() method:", computationGraph.params());
                    computationGraph.fit(dataSet);
                    int currentDataSetSize = charIterator.getLinesOffset();
                    int perCent = (int) (((double) currentDataSetSize / (double) trainDataSetSize) * (double) 100);
                    logger.info("DataSetSize: {}, currentDataSet: {}, passed: {}%", trainDataSetSize, currentDataSetSize, perCent);
                    if (++miniBatchNumber % saveNetworkEveryNMinibatches == 0 || trainDataSetSize == currentDataSetSize + 1) {
                        computationGraph.save(networkFile);
                        saveLinesOffset(currentDataSetSize, networkFile);
                        logger.info("--------------------");
                        String completedInfo =
                            "Completed " + miniBatchNumber + " miniBatches of size " + miniBatchSize + "x" + exampleLength + " characters";
                        logger.info(completedInfo);
                        evaluate(computationGraph, dataSet);
                    }
                }

                charIterator.reset();    //Reset iterator for another epoch
            }
        }

        evaluateTestData(computationGraph, resourcesPath, charIterator);

        logger.info("\n\nExample complete");
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

    private static int loadLinesOffset(File networkFile) {
        try {
            File linesOffsetFile = new File(networkFile.getAbsolutePath() + LINES_OFFSET_FILE_EXTENSION);
            if (!linesOffsetFile.exists()) {
                throw new LabelizerRuntimeException("Parameter '" + CONTINUE_TRAINING_ARGUMENT +
                    "' cannot be set when trained network does not exists. " +
                    "File not found: '" + linesOffsetFile.getAbsolutePath() + "'");
            }
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(linesOffsetFile))) {
                int linesOffset = Integer.parseInt(bufferedReader.readLine());
                logger.info("Network will be training with data beginning from linesOffset {}", linesOffset);
                return linesOffset;
            }
        } catch (Exception e) {
            throw new LabelizerRuntimeException(e);
        }
    }

    private static void saveLinesOffset(int currentDataSetSize, File networkFile) {
        try {
            File linesOffsetFile = new File(networkFile.getAbsolutePath() + LINES_OFFSET_FILE_EXTENSION);
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(linesOffsetFile))) {
                bufferedWriter.write(Integer.toString(currentDataSetSize));
            }
        } catch (Exception e) {
            throw new LabelizerRuntimeException(e);
        }
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
