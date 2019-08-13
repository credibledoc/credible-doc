package com.credibledoc.log.labelizer.classifier;

import org.apache.commons.io.FilenameUtils;
import org.datavec.api.conf.Configuration;
import org.datavec.api.records.reader.impl.LineRecordReader;
import org.datavec.image.transform.FlipImageTransform;
import org.datavec.image.transform.ImageTransform;
import org.datavec.image.transform.WarpImageTransform;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.distribution.Distribution;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.inputs.InvalidInputTypeException;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.parallelism.ParallelWrapper;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.learning.config.RmsProp;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.primitives.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Animal Classification
 *
 * Example classification of photos from 4 different animals (bear, duck, deer, turtle).
 *
 * References:
 *  - U.S. Fish and Wildlife Service (animal sample dataset): http://digitalmedia.fws.gov/cdm/
 *  - Tiny ImageNet Classification with CNN: http://cs231n.stanford.edu/reports/2015/pdfs/leonyao_final.pdf
 *
 * CHALLENGE: Current setup gets low score results. Can you improve the scores? Some approaches:
 *  - Add additional images to the dataset
 *  - Apply more transforms to dataset
 *  - Increase epochs
 *  - Try different model configurations
 *  - Tune by adjusting learning rate, updaters, activation & loss functions, regularization, ...
 */

public class LinesWithDateClassification {
    private static final Logger log = LoggerFactory.getLogger(LinesWithDateClassification.class);
    private static final int ACTIVATIONS_SIZE_1000 = 1000;
    private static int batchSize = 20;

    private static long seed = 42;
    private static Random rng = new Random(seed);
    private static int epochs = 50;
    private static double splitTrainTest = 0.8;
    private static boolean save = false;
    private static int maxPathsPerLabel=18;

    private static String modelType = "AlexNet"; // LeNet, AlexNet or Custom but you need to fill it out
    private int numLabels;

    public void run(String[] args) throws Exception {

        log.info("Load data....");
        /**cd
         * Data Setup -> organize and limit data file paths:
         *  - mainPath = path to image files
         *  - fileSplit = define basic dataset split with limits on format
         *  - pathFilter = define additional file load filter to limit size and balance batch content
         **/
//        ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
//        File mainPath = new File(System.getProperty("user.dir"), "dl4j-examples/src/main/resources/animals/");
//        FileSplit fileSplit = new FileSplit(mainPath, NativeImageLoader.ALLOWED_FORMATS, rng);
//        int numExamples = Math.toIntExact(fileSplit.length());
        numLabels = 2; // TODO Kyrylo Semenko - pouzit emum nebo konstantu
//        BalancedPathFilter pathFilter = new BalancedPathFilter(rng, labelMaker, numExamples, numLabels, maxPathsPerLabel);

        /*
         * Data Setup -> train test split
         *  - inputSplit = define train and test split
         **/
//        InputSplit[] inputSplit = fileSplit.sample(pathFilter, splitTrainTest, 1 - splitTrainTest);
//        InputSplit trainData = inputSplit[0];
//        InputSplit testData = inputSplit[1];

        /*
         * Data Setup -> transformation
         *  - Transform = how to tranform images and generate large dataset to train on
         **/
        ImageTransform flipTransform1 = new FlipImageTransform(rng);
        ImageTransform flipTransform2 = new FlipImageTransform(new Random(123));
        ImageTransform warpTransform = new WarpImageTransform(rng, 42);
        boolean shuffle = false;
        List<Pair<ImageTransform,Double>> pipeline = Arrays.asList(new Pair<>(flipTransform1,0.9),
            new Pair<>(flipTransform2,0.8),
            new Pair<>(warpTransform,0.5));

//        ImageTransform transform = new PipelineImageTransform(pipeline,shuffle);
        /*
         * Data Setup -> normalization
         *  - how to normalize images and generate large dataset to train on
         **/
//        DataNormalization scaler = new ImagePreProcessingScaler(0, 1);

        log.info("Build model....");

        // Uncomment below to try AlexNet. Note change height and width to at least 100

//        MultiLayerNetwork network;
//        switch (modelType) {
//            case "LeNet":
//                network = lenetModel();
//                break;
//            case "AlexNet":
//                network = lstmModel(dataIter);
//                break;
//            case "custom":
//                network = customModel();
//                break;
//            default:
//                throw new InvalidInputTypeException("Incorrect model provided.");
//        }
//        network.init();
//        UIServer uiServer = UIServer.getInstance();
//        StatsStorage statsStorage = new InMemoryStatsStorage();
//        uiServer.attach(statsStorage);
//        network.setListeners(new StatsListener( statsStorage),new ScoreIterationListener(1));
        /*
         * Data Setup -> define how to load data into net:
         *  - recordReader = the reader that loads and converts image data pass in inputSplit to initialize
         *  - dataIter = a generator that only loads one batch at a time into memory to save memory
         *  - trainIter = uses MultipleEpochsIterator to ensure model runs through the data for all epochs
         **/
//        ImageRecordReader recordReader = new ImageRecordReader(height, width, channels, labelMaker);
        LineRecordReader recordReader = new LineRecordReader();
        Configuration configuration = new Configuration();
        recordReader.setConf(configuration);
        configuration.addResource("c:\\temp\\download\\Rio\\tools-doc-generator\\weeks\\uk\\week_07_03\\" +
            "app-fep-uk-2019-07-18.0.log ");
        DataSetIterator dataIter;

        log.info("Train model....");
        // Train without transformations
        recordReader.initialize(configuration, null);
        dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, numLabels);
        MultiLayerNetwork network = lstmModel(dataIter);
        network.init();

        // ParallelWrapper will take care of load balancing between GPUs.
        ParallelWrapper wrapper = new ParallelWrapper.Builder(network)
            // DataSets prefetching options. Set this value with respect to number of actual devices
            .prefetchBuffer(24)

            // set number of workers equal to number of available devices. x1-x2 are good values to start with
            .workers(4)

            // rare averaging improves performance, but might reduce model accuracy
            .averagingFrequency(3)

            // if set to TRUE, on every averaging model score will be reported
            .reportScoreAfterAveraging(true)

            .build();
        
        UIServer uiServer = UIServer.getInstance();
        StatsStorage statsStorage = new InMemoryStatsStorage();
        uiServer.attach(statsStorage);
        network.setListeners(new StatsListener( statsStorage),new ScoreIterationListener(1));
        
        wrapper.fit(dataIter);
//        dataIter.setPreProcessor(wrapper);
        network.fit(dataIter, epochs);

//        log.info("Evaluate model....");
//        recordReader.initialize(testData);
//        dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, numLabels);
//        scaler.fit(dataIter);
//        dataIter.setPreProcessor(scaler);
//        Evaluation eval = network.evaluate(dataIter);
//        log.info(eval.stats(true));

        // Example on how to get predict results with trained model. Result for first example in minibatch is printed
        dataIter.reset();
        DataSet testDataSet = dataIter.next();
        List<String> allClassLabels = recordReader.getLabels();
        int labelIndex = testDataSet.getLabels().argMax(1).getInt(0);
        int[] predictedClasses = network.predict(testDataSet.getFeatures());
        String expectedResult = allClassLabels.get(labelIndex);
        String modelPrediction = allClassLabels.get(predictedClasses[0]);
        System.out.print("\nFor a single example that is labeled " + expectedResult + " the model predicted " + modelPrediction + "\n\n");

        if (save) {
            log.info("Save model....");
            String basePath = FilenameUtils.concat(System.getProperty("user.dir"), "src/main/resources/");
            ModelSerializer.writeModel(network, basePath + "model.bin", true);
        }
        log.info("****************Example finished********************");
    }

    private ConvolutionLayer convInit(String name, int in, int out, int[] kernel, int[] stride, int[] pad, double bias) {
        return new ConvolutionLayer.Builder(kernel, stride, pad).name(name).nIn(in).nOut(out).biasInit(bias).build();
    }

    private ConvolutionLayer conv3x3(String name, int out, double bias) {
        return new ConvolutionLayer.Builder(new int[]{3,3}, new int[] {1,1}, new int[] {1,1}).name(name).nOut(out).biasInit(bias).build();
    }

    private ConvolutionLayer conv5x5(String name, int out, int[] stride, int[] pad, double bias) {
        return new ConvolutionLayer.Builder(new int[]{5,5}, stride, pad).name(name).nOut(out).biasInit(bias).build();
    }

    private SubsamplingLayer maxPool(String name,  int[] kernel) {
        return new SubsamplingLayer.Builder(kernel, new int[]{2,2}).name(name).build();
    }

    private DenseLayer fullyConnected(String name, int out, double bias, double dropOut, Distribution dist) {
        return new DenseLayer.Builder().name(name).nOut(out).biasInit(bias).dropOut(dropOut).dist(dist).build();
    }

    public MultiLayerNetwork lenetModel() {
        /**
         * Revisde Lenet Model approach developed by ramgo2 achieves slightly above random
         * Reference: https://gist.github.com/ramgo2/833f12e92359a2da9e5c2fb6333351c5
         **/
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(seed)
            .l2(0.005)
            .activation(Activation.RELU)
            .weightInit(WeightInit.XAVIER)
            .updater(new Nesterovs(0.0001,0.9))
            .list()
            .layer(0, convInit("cnn1", 1, 50 ,  new int[]{5, 5}, new int[]{1, 1}, new int[]{0, 0}, 0))
            .layer(1, maxPool("maxpool1", new int[]{2,2}))
            .layer(2, conv5x5("cnn2", 100, new int[]{5, 5}, new int[]{1, 1}, 0))
            .layer(3, maxPool("maxool2", new int[]{2,2}))
            .layer(4, new DenseLayer.Builder().nOut(500).build())
            .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                .nOut(numLabels)
                .activation(Activation.SOFTMAX)
                .build())
            .backprop(true).pretrain(false)
//            .setInputType(InputType.convolutional(height, width, channels))
            .setInputType(InputType.recurrent(ACTIVATIONS_SIZE_1000))
            .build();

        return new MultiLayerNetwork(conf);

    }

    private MultiLayerNetwork lstmModel(DataSetIterator iter) {
        int lstmLayerSize = 200;                    //Number of units in each LSTM layer
        int tbpttLength = 50;                       //Length for truncated backpropagation through time. i.e., do parameter updates ever 50 characters

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(12345)
            .l2(0.001)
            .weightInit(WeightInit.XAVIER)
            .updater(new RmsProp.Builder().learningRate(0.1).build())
            .list()
            .layer(0, new LSTM.Builder().nIn(iter.inputColumns()).nOut(lstmLayerSize)
                .activation(Activation.TANH).build())
            .layer(1, new LSTM.Builder().nIn(lstmLayerSize).nOut(lstmLayerSize)
                .activation(Activation.TANH).build())
            .layer(2, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT).activation(Activation.SOFTMAX)        //MCXENT + softmax for classification
                .nIn(lstmLayerSize).nOut(iter.totalOutcomes()).build())
            .backpropType(BackpropType.TruncatedBPTT).tBPTTForwardLength(tbpttLength).tBPTTBackwardLength(tbpttLength)
            .pretrain(false).backprop(true)
            .build();

        return new MultiLayerNetwork(conf);

    }

    public static MultiLayerNetwork customModel() {
        /**
         * Use this method to build your own custom model.
         **/
        return null;
    }

    public static void main(String[] args) throws Exception {
        new LinesWithDateClassification().run(args);
    }

}
