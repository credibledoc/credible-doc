package com.credibledoc.log.labelizer.classifier;

import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;
import com.credibledoc.log.labelizer.iterator.CharacterIterator;
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
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LinesWithDateClassification {
    private static final Logger logger = LoggerFactory.getLogger(LinesWithDateClassification.class);
    private static final int CHARS_FOR_LOGGING_100 = 100;
    private static final int DIMENSION_INDEX_2 = 2;
    private static final String MULTILAYER_NETWORK_VECTORS = "network/multilayerNetwork.vectors";

    public static void main(String[] args ) throws Exception {
        int lstmLayerSize = 200;					//Number of units in each LSTM layer
        int miniBatchSize = 32;						//Size of mini batch to use when  training
        int exampleLength = 1000;					//Length of each training example sequence to use. This could certainly be increased
        int charsNumBackPropagationThroughTime = 50;//Length for truncated backpropagation through time. i.e., do parameter updates ever 50 characters
        int numEpochs = 1;							//Total number of training epochs
        int generateSamplesEveryNMinibatches = 10;  //How frequently to generate samples from the network? 1000 characters / 50 tbptt length: 20 parameter updates per minibatch
        int nSamplesToGenerate = 4;					//Number of samples to generate after each training epoch
        int nCharactersToSample = 300;				//Length of each sample to generate
        String generationInitialization = null;		//Optional character initialization; a random character is used if null
        // Above is Used to 'prime' the LSTM with a character sequence to continue/complete.
        // Initialization characters must all be in CharacterIterator.getMinimalCharacterSet() by default
        Random rng = new Random(12345);

        MultiLayerNetwork multiLayerNetwork = null;
        
        boolean isNetworkLoadedFromFile = false;
        
        File networkFile = new File(LinesWithDateClassification.class.getResource("/")
            .getPath()
            .replace("target/classes", "src/main/resources/") + MULTILAYER_NETWORK_VECTORS);
        if (networkFile.exists()) {
            multiLayerNetwork = MultiLayerNetwork.load(networkFile, true);
            logger.info("{} loaded from file '{}'", MultiLayerNetwork.class.getSimpleName(), networkFile.getAbsolutePath());
        } else {
            FileUtils.forceMkdir(networkFile.getParentFile());
            logger.info("Directory created: {}", networkFile.getParentFile().getAbsolutePath());
        }

        if (networkFile.exists()) {
            isNetworkLoadedFromFile = true;
        }

        //Get a DataSetIterator that handles vectorization of text into something we can use to train
        // our LSTM network.
        CharacterIterator iter = getShakespeareIterator(miniBatchSize,exampleLength);
        int nOut = iter.totalOutcomes();

        //Set up network configuration:
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
            .backpropType(BackpropType.TruncatedBPTT).tBPTTForwardLength(charsNumBackPropagationThroughTime).tBPTTBackwardLength(charsNumBackPropagationThroughTime)
            .build();

        if (!isNetworkLoadedFromFile) {
            multiLayerNetwork = new MultiLayerNetwork(conf);
        }
        assert multiLayerNetwork != null;
        multiLayerNetwork.init();
        
        if (!isNetworkLoadedFromFile) {
            multiLayerNetwork.setListeners(new ScoreIterationListener(1));

            //Print the  number of parameters in the network (and for each layer)
            String summary = multiLayerNetwork.summary();
            logger.info(summary);

            //Do training, and then generate and print samples from network
            int miniBatchNumber = 0;
            for (int i = 0; i < numEpochs; i++) {
                while (iter.hasNext()) {
                    StringDataSet ds = iter.next();
                    multiLayerNetwork.fit(ds);
                    multiLayerNetwork.save(networkFile);
                    if (++miniBatchNumber % generateSamplesEveryNMinibatches == 0) {
                        logger.info("--------------------");
                        logger.info("First " + CHARS_FOR_LOGGING_100 + " characters of every miniBatch:");
                        int miniBatchIndex = 0;
                        for (String sample : ds.getRawData()) {
                            int endIndex = sample.length() > CHARS_FOR_LOGGING_100 ? CHARS_FOR_LOGGING_100 : sample.length();
                            String miniBatchInfo = "MiniBatch " + miniBatchIndex + ": " + sample.substring(0, endIndex) + "...";
                            logger.info(miniBatchInfo);
                        }
                        String completedInfo =
                            "Completed " + miniBatchNumber + " minibatches of size " + miniBatchSize + "x" + exampleLength + " characters";
                        logger.info(completedInfo);
                        printSamples(nSamplesToGenerate, nCharactersToSample, generationInitialization, rng,
                            multiLayerNetwork, iter);
                        evaluate(multiLayerNetwork, ds);
                    }
                }

                iter.reset();    //Reset iterator for another epoch
            }
        }
        
        printSamples(1, 20, "init", rng, multiLayerNetwork, iter);

        logger.info("\n\nExample complete");
    }

    private static void printSamples(int nSamplesToGenerate, int nCharactersToSample, String generationInitialization, Random rng, MultiLayerNetwork multiLayerNetwork, CharacterIterator iter) {
        String samplingInfo = "Sampling characters from network given initialization \"" + (generationInitialization == null ? 
            "" : generationInitialization) + "\"";
        logger.info(samplingInfo);
        
//                    String[] samples = sampleCharactersFromNetwork(generationInitialization,net,iter,rng,nCharactersToSample,nSamplesToGenerate);
        String[] samples = sampleCharactersFromNetwork2(generationInitialization, multiLayerNetwork, iter, rng,
            nCharactersToSample, nSamplesToGenerate);
        
        for( int j=0; j<samples.length; j++ ){
            String sampleInfo = "----- Sample " + j + " -----";
            logger.info(sampleInfo);
            logger.info(samples[j]);
            logger.info("");
        }
    }

    private static void evaluate(MultiLayerNetwork net, DataSet dataSet) {
        Evaluation evaluation = new Evaluation(77);
        INDArray output = net.output(dataSet.getFeatures());
        evaluation.eval(dataSet.getLabels(), output);
        String stats = evaluation.stats();
        logger.info(stats);
    }

    /** Downloads Shakespeare training data and stores it locally (temp directory). Then set up and return a simple
     * DataSetIterator that does vectorization based on the text.
     * @param miniBatchSize Number of text segments in each training mini-batch
     * @param sequenceLength Number of characters in each text segment.
     */
    private static CharacterIterator getShakespeareIterator(int miniBatchSize, int sequenceLength) {
        try {
            //The Complete Works of William Shakespeare
            //5.3MB file in UTF-8 Encoding, ~5.4 million characters
            //https://www.gutenberg.org/ebooks/100
            String url = "https://s3.amazonaws.com/dl4j-distribution/pg100.txt";
            String tempDir = System.getProperty("java.io.tmpdir");
            String fileLocation = tempDir + "/Shakespeare.txt";    //Storage location from downloaded file
            File f = new File(fileLocation);
            if (!f.exists()) {
                FileUtils.copyURLToFile(new URL(url), f);
                logger.info("File downloaded to {}", f.getAbsolutePath());
            } else {
                logger.info("Using existing text file at {}", f.getAbsolutePath());
            }

            if (!f.exists())
                throw new LabelizerRuntimeException("File does not exist: " + fileLocation);    //Download problem?

            char[] validCharacters = CharacterIterator.getMinimalCharacterSet();    //Which characters are allowed? 
            // Others will be removed
            return new CharacterIterator(fileLocation, StandardCharsets.UTF_8,
                miniBatchSize, sequenceLength, validCharacters, new Random(12345));
        } catch (Exception e) {
            throw new LabelizerRuntimeException(e);
        }
    }

    /** Generate a sample from the network, given an (optional, possibly null) initialization. Initialization
     * can be used to 'prime' the RNN with a sequence you want to extend/continue.<br>
     * Note that the initalization is used for all samples
     * @param initialization String, may be null. If null, select a random character as initialization for all samples
     * @param charactersToSample Number of characters to sample from network (excluding initialization)
     * @param net MultiLayerNetwork with one or more LSTM/RNN layers and a softmax output layer
     * @param iter CharacterIterator. Used for going from indexes back to characters
     */
    private static String[] sampleCharactersFromNetwork(String initialization, MultiLayerNetwork net,
                                                        CharacterIterator iter, Random rng, int charactersToSample, int numSamples ){
        //Set up initialization. If no initialization: use a random character
        if( initialization == null ){
            initialization = String.valueOf(iter.getRandomCharacter());
        }

        //Create input for initialization
        INDArray initializationInput = Nd4j.zeros(numSamples, iter.inputColumns(), initialization.length());
        char[] init = initialization.toCharArray();
        for( int i=0; i<init.length; i++ ){
            int idx = iter.convertCharacterToIndex(init[i]);
            for( int j=0; j<numSamples; j++ ){
                initializationInput.putScalar(new int[]{j,idx,i}, 1.0f);
            }
        }

        StringBuilder[] sb = new StringBuilder[numSamples];
        for (int i = 0; i < numSamples; i++) sb[i] = new StringBuilder(initialization);

        //Sample from network (and feed samples back into input) one character at a time (for all samples)
        //Sampling is done in parallel here
        net.rnnClearPreviousState();
        INDArray output = net.rnnTimeStep(initializationInput);
        int index = (int) output.size(2) - 1;
        output = output.tensorAlongDimension(index, 1, 0);	//Gets the last time step output

        List<Integer> lastIndexes = new ArrayList<>();
        for( int characterIndex=0; characterIndex<charactersToSample; characterIndex++ ){
            //Set up next input (single time step) by sampling from previous output
            INDArray nextInput = Nd4j.zeros(numSamples,iter.inputColumns());
            //Output is a probability distribution. Sample from this for each example we want to generate, and add it to the new input
            for( int sampleIndex=0; sampleIndex<numSamples; sampleIndex++ ){
                double[] outputProbDistribution = new double[iter.totalOutcomes()];
                for( int j=0; j<outputProbDistribution.length; j++ ) outputProbDistribution[j] = output.getDouble(sampleIndex,j);
                int sampledCharacterIdx = sampleFromDistribution(outputProbDistribution,rng);
//                int sampledCharacterIdx = sampleFromDistribution(outputProbDistribution, lastIndexes);
                lastIndexes.add(sampledCharacterIdx);

                nextInput.putScalar(new int[]{sampleIndex,sampledCharacterIdx}, 1.0f);		//Prepare next time step input
                sb[sampleIndex].append(iter.convertIndexToCharacter(sampledCharacterIdx));	//Add sampled character to StringBuilder (human readable output)
            }

            output = net.rnnTimeStep(nextInput);	//Do one time step of forward pass
        }

        String[] out = new String[numSamples];
        for( int i=0; i<numSamples; i++ ) out[i] = sb[i].toString();
        return out;
    }
    
    private static String[] sampleCharactersFromNetwork2(String initString, MultiLayerNetwork multiLayerNetwork,
                                                        CharacterIterator characterIterator, Random rng, int charactersToSample, int numSamples ) {
        //Set up initialization. If no initialization: use a random character
        if (initString == null) {
            initString = String.valueOf(characterIterator.getRandomCharacter());
        }

        //Create input for initialization
        INDArray initIndArray = Nd4j.zeros(numSamples, characterIterator.inputColumns(), initString.length());
        char[] initChars = initString.toCharArray();
        for (int initStringIndex = 0; initStringIndex < initChars.length; initStringIndex++) {
            int characterIndex = characterIterator.convertCharacterToIndex(initChars[initStringIndex]);
            for (int sampleIndex = 0; sampleIndex < numSamples; sampleIndex++) {
                initIndArray.putScalar(new int[]{sampleIndex, characterIndex, initStringIndex}, 1.0f);
            }
        }

        StringBuilder[] sb = new StringBuilder[numSamples];
        for (int sampleIndex = 0; sampleIndex < numSamples; sampleIndex++) sb[sampleIndex] = new StringBuilder(initString);

        //Sample from network (and feed samples back into input) one character at a time (for all samples)
        //Sampling is done in parallel here
        multiLayerNetwork.rnnClearPreviousState();
        INDArray timeStepIndArray = multiLayerNetwork.rnnTimeStep(initIndArray);
        // Index of a vector with characters number in the initString
        int vectorIndex = (int) timeStepIndArray.size(DIMENSION_INDEX_2) - 1;
        //Gets the last time step output
        timeStepIndArray = timeStepIndArray.tensorAlongDimension(vectorIndex, 1, 0);

        List<Integer> lastIndexes = new ArrayList<>();
        for (int characterIndex = 0; characterIndex < charactersToSample; characterIndex++) {
            //Set up next input (single time step) by sampling from previous output
            INDArray nextInput = Nd4j.zeros(numSamples, characterIterator.inputColumns());
            //Output is a probability distribution. Sample from this for each example we want to generate, and add it to the new input
            for (int sampleIndex = 0; sampleIndex < numSamples; sampleIndex++) {
                double[] outputProbDistribution = new double[characterIterator.totalOutcomes()];
                for (int j = 0; j < outputProbDistribution.length; j++)
                    outputProbDistribution[j] = timeStepIndArray.getDouble(sampleIndex, j);
//                int sampledCharacterIdx = sampleFromDistribution(outputProbDistribution,rng);
                int sampledCharacterIdx = sampleFromDistribution(outputProbDistribution, lastIndexes);
                lastIndexes.add(sampledCharacterIdx);

                //Prepare next time step input
                nextInput.putScalar(new int[]{sampleIndex, sampledCharacterIdx}, 1.0f);
                //Add sampled character to StringBuilder (human readable output)
                sb[sampleIndex].append(characterIterator.convertIndexToCharacter(sampledCharacterIdx));
            }
            //Do one time step of forward pass
            timeStepIndArray = multiLayerNetwork.rnnTimeStep(nextInput);
        }

        String[] out = new String[numSamples];
        for( int i=0; i<numSamples; i++ ) out[i] = sb[i].toString();
        return out;
    }

    private static int sampleFromDistribution(double[] outputProbDistribution, List<Integer> lastIndexes) {
        int result = 0;
        double max = -1;
        for (int i = 0; i < outputProbDistribution.length; i++) {
//            if (lastIndexes.contains(i)) {
//                continue;
//            }
            double next = outputProbDistribution[i];
            if (next > max) {
                max = next;
                result = i;
            }
        }
        return result;
    }

    /** Given a probability distribution over discrete classes, sample from the distribution
     * and return the generated class index.
     * @param distribution Probability distribution over classes. Must sum to 1.0
     */
    private static int sampleFromDistribution( double[] distribution, Random rng ){
        double d = 0.0;
        double sum = 0.0;
        for( int t=0; t<10; t++ ) {
            d = rng.nextDouble();
            sum = 0.0;
            for( int i=0; i<distribution.length; i++ ){
                sum += distribution[i];
                if( d <= sum ) return i;
            }
            //If we haven't found the right index yet, maybe the sum is slightly
            //lower than 1 due to rounding error, so try again.
        }
        //Should be extremely unlikely to happen if distribution is a valid probability distribution
        throw new IllegalArgumentException("Distribution is invalid? d="+d+", sum="+sum);
    }
}
