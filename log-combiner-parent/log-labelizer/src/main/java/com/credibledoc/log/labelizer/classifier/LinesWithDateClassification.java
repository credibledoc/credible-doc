package com.credibledoc.log.labelizer.classifier;

import com.credibledoc.log.labelizer.date.DateLabel;
import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;
import com.credibledoc.log.labelizer.iterator.CharIterator;
import com.credibledoc.log.labelizer.iterator.StringDataSet;
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
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.io.ClassPathResource;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class LinesWithDateClassification {
    private static final Logger logger = LoggerFactory.getLogger(LinesWithDateClassification.class);
    private static final int CHARS_FOR_LOGGING_100 = 100;
    private static final int DIMENSION_INDEX_2 = 2;
    private static final String MULTILAYER_NETWORK_VECTORS = "network/multilayerNetwork.vectors";

    public static void main(String[] args) throws Exception {
        int lstmLayerSize = 200;                    //Number of units in each LSTM layer
        int miniBatchSize = 32;                        //Size of mini batch to use when  training
        int exampleLength = 1000;                    //Length of each training example sequence to use. This could 
        // certainly be increased
        int charsNumBackPropagationThroughTime = 50;//Length for truncated backpropagation through time. i.e., do 
        // parameter updates ever 50 characters
        int numEpochs = 1;                            //Total number of training epochs
        int generateSamplesEveryNMinibatches = 10;  //How frequently to generate samples from the network? 1000 
        // characters / 50 tbptt length: 20 parameter updates per minibatch
        int nSamplesToGenerate = 4;                    //Number of samples to generate after each training epoch
        int nCharactersToSample = 300;                //Length of each sample to generate

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

        //Set up network configuration:
        if (!isNetworkLoadedFromFile) {
            MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
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

            multiLayerNetwork = new MultiLayerNetwork(conf);
            multiLayerNetwork.init();

            multiLayerNetwork.setListeners(new ScoreIterationListener(1));

            //Print the  number of parameters in the network (and for each layer)
            String summary = multiLayerNetwork.summary();
            logger.info(summary);

            //Do training, and then generate and print samples from network
            int miniBatchNumber = 0;
            for (int i = 0; i < numEpochs; i++) {
                while (iter.hasNext()) {
                    StringDataSet stringDataSet = iter.next();
                    multiLayerNetwork.fit(stringDataSet);
                    multiLayerNetwork.save(networkFile);
                    if (++miniBatchNumber % generateSamplesEveryNMinibatches == 0) {
                        logger.info("--------------------");
                        logger.info("First {} characters of every miniBatch:", CHARS_FOR_LOGGING_100);
                        String completedInfo =
                            "Completed " + miniBatchNumber + " miniBatches of size " + miniBatchSize + "x" + exampleLength + " characters";
                        logger.info(completedInfo);
                        printSamples(nSamplesToGenerate, nCharactersToSample, null,
                            multiLayerNetwork, iter);
                        evaluate(multiLayerNetwork, stringDataSet);
                    }
                }

                iter.reset();    //Reset iterator for another epoch
            }
        }

        printSamples(1, 20, "init", multiLayerNetwork, iter);

        logger.info("\n\nExample complete");
    }

    private static void printSamples(int nSamplesToGenerate, int nCharactersToSample, String generationInitialization,
                                     MultiLayerNetwork multiLayerNetwork, CharIterator iter) {
        String samplingInfo =
            "Sampling characters from network given initialization \"" + (generationInitialization == null ?
            "" : generationInitialization) + "\"";
        logger.info(samplingInfo);

        String[] samples = sampleCharactersFromNetwork(generationInitialization, multiLayerNetwork, iter,
            nCharactersToSample, nSamplesToGenerate);

        for (int j = 0; j < samples.length; j++) {
            String sampleInfo = "----- Sample " + j + " -----";
            logger.info(sampleInfo);
            logger.info(samples[j]);
            logger.info("");
        }
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
                sequenceLength,
                new Random(12345));
        } catch (Exception e) {
            throw new LabelizerRuntimeException(e);
        }
    }


    private static String[] sampleCharactersFromNetwork(String initString,
                                                        MultiLayerNetwork multiLayerNetwork,
                                                        CharIterator charIterator,
                                                        int charactersToSample,
                                                        int miniBatch) {
        //Set up initialization. If no initialization: use a random character
        if (initString == null) {
            initString = String.valueOf(charIterator.getRandomCharacter());
        }

        //Create input for initialization
        INDArray initIndArray = createInitIndArray(initString, charIterator, miniBatch);

        INDArray outputIndArray = multiLayerNetwork.output(initIndArray);
        long timeSeriesLength = outputIndArray.size(2);
        INDArray lastTimeStepProbabilities = outputIndArray.get(NDArrayIndex.point(0), NDArrayIndex.all(),
            NDArrayIndex.point(timeSeriesLength - 1));
        logInitIndArray("lastTimeStepProbabilities: ", lastTimeStepProbabilities);

        StringBuilder[] sb = new StringBuilder[miniBatch];
        for (int sampleIndex = 0; sampleIndex < miniBatch; sampleIndex++)
            sb[sampleIndex] = new StringBuilder(initString);

        //Sample from network (and feed samples back into input) one character at a time (for all samples)
        //Sampling is done in parallel here
        multiLayerNetwork.rnnClearPreviousState();
        INDArray timeStepIndArray = multiLayerNetwork.rnnTimeStep(initIndArray);
        // Index of a vector with characters number in the initString
        int vectorIndex = (int) timeStepIndArray.size(DIMENSION_INDEX_2) - 1;
        //Gets the last time step output
        timeStepIndArray = timeStepIndArray.tensorAlongDimension(vectorIndex, 1, 0);

        for (int characterIndex = 0; characterIndex < charactersToSample; characterIndex++) {
            //Set up next input (single time step) by sampling from previous output
            INDArray nextInput = Nd4j.zeros(miniBatch, charIterator.inputColumns());
            //Output is a probability distribution. Sample from this for each example we want to generate, and add it
            // to the new input
            for (int sampleIndex = 0; sampleIndex < miniBatch; sampleIndex++) {
                double[] outputProbDistribution = new double[charIterator.totalOutcomes()];
                for (int j = 0; j < outputProbDistribution.length; j++) {
                    outputProbDistribution[j] = timeStepIndArray.getDouble(sampleIndex, j);
                }
                int sampledCharacterIdx = sampleFromDistribution(outputProbDistribution);

                //Prepare next time step input
                nextInput.putScalar(new int[]{sampleIndex, sampledCharacterIdx}, 1.0f);
                //Add sampled character to StringBuilder (human readable output)
                sb[sampleIndex].append(DateLabel.findCharacter(sampledCharacterIdx));
            }
            //Do one time step of forward pass
            timeStepIndArray = multiLayerNetwork.rnnTimeStep(nextInput);
        }

        String[] out = new String[miniBatch];
        for (int i = 0; i < miniBatch; i++) out[i] = sb[i].toString();
        return out;
    }

    private static INDArray createInitIndArray(String initString, CharIterator charIterator, int miniBatch) {
        INDArray initIndArray = Nd4j.zeros(miniBatch, charIterator.inputColumns(), initString.length());
        char[] initChars = initString.toCharArray();
        for (int initStringIndex = 0; initStringIndex < initChars.length; initStringIndex++) {
            int characterIndex = charIterator.convertCharacterToIndex(initChars[initStringIndex]);
            for (int miniBatchIndex = 0; miniBatchIndex < miniBatch; miniBatchIndex++) {
                initIndArray.putScalar(new int[]{miniBatchIndex, characterIndex, initStringIndex}, 1.0f);
            }
        }
        logInitIndArray("initIndArray:", initIndArray);
        return initIndArray;
    }

    private static void logInitIndArray(String message, INDArray initIndArray) {
        logger.info("{}\n{}", message, initIndArray);
    }

    private static int sampleFromDistribution(double[] outputProbDistribution) {
        int result = 0;
        double max = -1;
        for (int i = 0; i < outputProbDistribution.length; i++) {
            double next = outputProbDistribution[i];
            if (next > max) {
                max = next;
                result = i;
            }
        }
        return result;
    }

}
