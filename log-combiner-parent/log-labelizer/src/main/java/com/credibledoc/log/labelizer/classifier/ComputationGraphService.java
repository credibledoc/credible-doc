package com.credibledoc.log.labelizer.classifier;

import com.credibledoc.log.labelizer.date.ProbabilityLabel;
import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;
import com.credibledoc.log.labelizer.iterator.CharIterator;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.graph.MergeVertex;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

/**
 * Helps to create different types of {@link org.deeplearning4j.nn.graph.ComputationGraph}s.
 */
public class ComputationGraphService {
    private ComputationGraphService() {
        throw new LabelizerRuntimeException("Please do not instantiate the static helper.");
    }

    private static ComputationGraphConfiguration createNetInputInput2MergeHiddenOutput(CharIterator charIterator,
                                                                                       int nOut, int lstmLayerSize) {
        return new NeuralNetConfiguration.Builder()
                    .seed(LinesWithDateClassification.SEED_12345)
                    .l2(LinesWithDateClassification.L2_REGULARIZATION_COEFFICIENT_0_00001)
                    .weightInit(WeightInit.XAVIER)
                    .updater(new Adam(LinesWithDateClassification.LEARNING_RATE_0_01))
                    .graphBuilder()
                    
                    .addInputs(LinesWithDateClassification.INPUT_1, LinesWithDateClassification.INPUT_2)
                    
                    .addLayer(LinesWithDateClassification.LAYER_INPUT_1, new LSTM.Builder().nIn(charIterator.inputColumns()).nOut(lstmLayerSize)
                        .activation(Activation.TANH).build(), LinesWithDateClassification.INPUT_1)
                    
                    .addLayer(LinesWithDateClassification.LAYER_INPUT_2, new LSTM.Builder().nIn(charIterator.inputColumns()).nOut(2)
                        .activation(Activation.TANH).build(), LinesWithDateClassification.INPUT_2)
                            
                    .addVertex(LinesWithDateClassification.MERGE_VERTEX, new MergeVertex(), LinesWithDateClassification.LAYER_INPUT_1, LinesWithDateClassification.LAYER_INPUT_2)
    
                    .addLayer(LinesWithDateClassification.HIDDEN_2, new LSTM.Builder().nIn(lstmLayerSize + 2).nOut(lstmLayerSize)
                        .activation(Activation.TANH).build(), LinesWithDateClassification.MERGE_VERTEX)
    
                    .addLayer(LinesWithDateClassification.LAYER_OUTPUT_3, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT).activation(Activation.SOFTMAX)        //MCXENT + softmax for classification
                        .nIn(lstmLayerSize).nOut(nOut).build(), LinesWithDateClassification.HIDDEN_2)
                    
                    .setOutputs(LinesWithDateClassification.LAYER_OUTPUT_3)
                    
                    .backpropType(BackpropType.TruncatedBPTT)
                    .tBPTTForwardLength(LinesWithDateClassification.CHARS_NUM_BACK_PROPAGATION_THROUGH_TIME)
                    .tBPTTBackwardLength(LinesWithDateClassification.CHARS_NUM_BACK_PROPAGATION_THROUGH_TIME)
                    .build();
    }

    private static ComputationGraphConfiguration threeHiddenAndHintToAll(CharIterator charIterator,
                                                                        int labelsNum, int lstmLayerSize) {
        return new NeuralNetConfiguration.Builder()
            .seed(LinesWithDateClassification.SEED_12345)
            .l2(LinesWithDateClassification.L2_REGULARIZATION_COEFFICIENT_0_00001)
            .weightInit(WeightInit.XAVIER)
            .updater(new Adam(LinesWithDateClassification.LEARNING_RATE_0_01))
            .graphBuilder()

            .addInputs(LinesWithDateClassification.INPUT_1, LinesWithDateClassification.INPUT_2)

            .addLayer(LinesWithDateClassification.HIDDEN_1, new LSTM.Builder().nIn(charIterator.inputColumns() + 2).nOut(labelsNum)
                .activation(Activation.TANH).build(), LinesWithDateClassification.INPUT_1, LinesWithDateClassification.INPUT_2)

            .addLayer(LinesWithDateClassification.HIDDEN_2, new LSTM.Builder().nIn(labelsNum + 2).nOut(labelsNum)
                .activation(Activation.TANH).build(), LinesWithDateClassification.HIDDEN_1, LinesWithDateClassification.INPUT_2)

            .addLayer(LinesWithDateClassification.HIDDEN_3, new LSTM.Builder().nIn(labelsNum + 2).nOut(labelsNum)
                .activation(Activation.TANH).build(), LinesWithDateClassification.HIDDEN_2, LinesWithDateClassification.INPUT_2)

            .addLayer(LinesWithDateClassification.LAYER_OUTPUT_3, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT).activation(Activation.SOFTMAX)        //MCXENT + softmax for classification
                .nIn(labelsNum * 3).nOut(labelsNum).build(), LinesWithDateClassification.HIDDEN_1, LinesWithDateClassification.HIDDEN_2, LinesWithDateClassification.HIDDEN_3)

            .setOutputs(LinesWithDateClassification.LAYER_OUTPUT_3)

            .backpropType(BackpropType.TruncatedBPTT)
            .tBPTTForwardLength(LinesWithDateClassification.CHARS_NUM_BACK_PROPAGATION_THROUGH_TIME)
            .tBPTTBackwardLength(LinesWithDateClassification.CHARS_NUM_BACK_PROPAGATION_THROUGH_TIME)
            .build();
    }

    private static ComputationGraphConfiguration inputInputMergeHiddenOutputShorterHidden(CharIterator charIterator,
                                                                                          int labelsNum, int lstmLayerSize) {
        return new NeuralNetConfiguration.Builder()
            .seed(LinesWithDateClassification.SEED_12345)
            .l2(LinesWithDateClassification.L2_REGULARIZATION_COEFFICIENT_0_00001)
            .weightInit(WeightInit.XAVIER)
            .updater(new Adam(LinesWithDateClassification.LEARNING_RATE_0_01))
            .graphBuilder()

            .addInputs(LinesWithDateClassification.INPUT_1, LinesWithDateClassification.INPUT_2)

            .addLayer(LinesWithDateClassification.HIDDEN_2, new LSTM.Builder().nIn(2).nOut(2)
                .activation(Activation.TANH).build(), LinesWithDateClassification.INPUT_2)

            .addVertex(LinesWithDateClassification.MERGE_VERTEX, new MergeVertex(), LinesWithDateClassification.INPUT_1, LinesWithDateClassification.HIDDEN_2)

            .addLayer(LinesWithDateClassification.HIDDEN_3, new LSTM.Builder().nIn(charIterator.inputColumns() + 2).nOut(labelsNum)
                .activation(Activation.TANH).build(), LinesWithDateClassification.MERGE_VERTEX)

            .addLayer(LinesWithDateClassification.LAYER_OUTPUT_3, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT).activation(Activation.SOFTMAX)        //MCXENT + softmax for classification
                .nIn(labelsNum).nOut(labelsNum).build(), LinesWithDateClassification.HIDDEN_3)

            .setOutputs(LinesWithDateClassification.LAYER_OUTPUT_3)

            .backpropType(BackpropType.TruncatedBPTT)
            .tBPTTForwardLength(LinesWithDateClassification.CHARS_NUM_BACK_PROPAGATION_THROUGH_TIME)
            .tBPTTBackwardLength(LinesWithDateClassification.CHARS_NUM_BACK_PROPAGATION_THROUGH_TIME)
            .build();
    }

    static ComputationGraphConfiguration twoHiddenAndHintToBoth(CharIterator charIterator,
                                                                int labelsNum, int lstmLayerSize) {
        return new NeuralNetConfiguration.Builder()
                    .seed(LinesWithDateClassification.SEED_12345)
                    .l2(LinesWithDateClassification.L2_REGULARIZATION_COEFFICIENT_0_00001)
                    .weightInit(WeightInit.XAVIER)
                    .updater(new Adam(LinesWithDateClassification.LEARNING_RATE_0_01))
                    .graphBuilder()
                    
                    .addInputs(LinesWithDateClassification.INPUT_1, LinesWithDateClassification.INPUT_2)
    
                    .addLayer(LinesWithDateClassification.HIDDEN_1, new LSTM.Builder().nIn(charIterator.inputColumns() + 2).nOut(labelsNum)
                        .activation(Activation.TANH).build(), LinesWithDateClassification.INPUT_1, LinesWithDateClassification.INPUT_2)

                    .addLayer(LinesWithDateClassification.HIDDEN_2, new LSTM.Builder().nIn(labelsNum + 2).nOut(labelsNum)
                        .activation(Activation.TANH).build(), LinesWithDateClassification.HIDDEN_1, LinesWithDateClassification.INPUT_2)
    
                    .addLayer(LinesWithDateClassification.LAYER_OUTPUT_3, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT).activation(Activation.SOFTMAX)        //MCXENT + softmax for classification
                        .nIn(labelsNum * 2).nOut(labelsNum).build(), LinesWithDateClassification.HIDDEN_1, LinesWithDateClassification.HIDDEN_2)
                    
                    .setOutputs(LinesWithDateClassification.LAYER_OUTPUT_3)
                    
                    .backpropType(BackpropType.TruncatedBPTT)
                    .tBPTTForwardLength(LinesWithDateClassification.CHARS_NUM_BACK_PROPAGATION_THROUGH_TIME)
                    .tBPTTBackwardLength(LinesWithDateClassification.CHARS_NUM_BACK_PROPAGATION_THROUGH_TIME)
                    .build();
    }

    private static ComputationGraphConfiguration twoHiddenAndHintToSecondHidden(CharIterator charIterator,
                                                                                int labelsNum, int lstmLayerSize) {
        return new NeuralNetConfiguration.Builder()
                    .seed(LinesWithDateClassification.SEED_12345)
                    .l2(LinesWithDateClassification.L2_REGULARIZATION_COEFFICIENT_0_00001)
                    .weightInit(WeightInit.XAVIER)
                    .updater(new Adam(LinesWithDateClassification.LEARNING_RATE_0_01))
                    .graphBuilder()
                    
                    .addInputs(LinesWithDateClassification.INPUT_1, LinesWithDateClassification.INPUT_2)
    
                    .addLayer(LinesWithDateClassification.HIDDEN_1, new LSTM.Builder().nIn(charIterator.inputColumns()).nOut(labelsNum)
                        .activation(Activation.TANH).build(), LinesWithDateClassification.INPUT_1)

                    .addVertex(LinesWithDateClassification.MERGE_VERTEX, new MergeVertex(), LinesWithDateClassification.HIDDEN_1, LinesWithDateClassification.INPUT_2)

                    .addLayer(LinesWithDateClassification.HIDDEN_2, new LSTM.Builder().nIn(labelsNum + 2).nOut(labelsNum)
                        .activation(Activation.TANH).build(), LinesWithDateClassification.MERGE_VERTEX)
    
                    .addLayer(LinesWithDateClassification.LAYER_OUTPUT_3, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT).activation(Activation.SOFTMAX)        //MCXENT + softmax for classification
                        .nIn(labelsNum).nOut(labelsNum).build(), LinesWithDateClassification.HIDDEN_2)
                    
                    .setOutputs(LinesWithDateClassification.LAYER_OUTPUT_3)
                    
                    .backpropType(BackpropType.TruncatedBPTT)
                    .tBPTTForwardLength(LinesWithDateClassification.CHARS_NUM_BACK_PROPAGATION_THROUGH_TIME)
                    .tBPTTBackwardLength(LinesWithDateClassification.CHARS_NUM_BACK_PROPAGATION_THROUGH_TIME)
                    .build();
    }

    private static ComputationGraphConfiguration oneHiddenAndHintToOutput(CharIterator charIterator,
                                                                          int labelsNum, int lstmLayerSize) {
        return new NeuralNetConfiguration.Builder()
                    .seed(LinesWithDateClassification.SEED_12345)
                    .l2(LinesWithDateClassification.L2_REGULARIZATION_COEFFICIENT_0_00001)
                    .weightInit(WeightInit.XAVIER)
                    .updater(new Adam(LinesWithDateClassification.LEARNING_RATE_0_01))
                    .graphBuilder()
                    
                    .addInputs(LinesWithDateClassification.INPUT_1, LinesWithDateClassification.INPUT_2)
    
                    .addLayer(LinesWithDateClassification.HIDDEN_2, new LSTM.Builder().nIn(charIterator.inputColumns()).nOut(ProbabilityLabel.values().length)
                        .activation(Activation.TANH).build(), LinesWithDateClassification.INPUT_1)

                    .addVertex(LinesWithDateClassification.MERGE_VERTEX, new MergeVertex(), LinesWithDateClassification.HIDDEN_2, LinesWithDateClassification.INPUT_2)
    
                    .addLayer(LinesWithDateClassification.LAYER_OUTPUT_3, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT).activation(Activation.SOFTMAX)        //MCXENT + softmax for classification
                        .nIn(ProbabilityLabel.values().length + 2).nOut(labelsNum).build(), LinesWithDateClassification.MERGE_VERTEX)
                    
                    .setOutputs(LinesWithDateClassification.LAYER_OUTPUT_3)
                    
                    .backpropType(BackpropType.TruncatedBPTT)
                    .tBPTTForwardLength(LinesWithDateClassification.CHARS_NUM_BACK_PROPAGATION_THROUGH_TIME)
                    .tBPTTBackwardLength(LinesWithDateClassification.CHARS_NUM_BACK_PROPAGATION_THROUGH_TIME)
                    .build();
    }

    private static ComputationGraphConfiguration inputInputMergeHiddenOutput(CharIterator charIterator,
                                                                             int labelsNum, int lstmLayerSize) {
        int hidden3out = lstmLayerSize / 2;
        return new NeuralNetConfiguration.Builder()
                    .seed(LinesWithDateClassification.SEED_12345)
                    .l2(LinesWithDateClassification.L2_REGULARIZATION_COEFFICIENT_0_00001)
                    .weightInit(WeightInit.XAVIER)
                    .updater(new Adam(LinesWithDateClassification.LEARNING_RATE_0_01))
                    .graphBuilder()
                    
                    .addInputs(LinesWithDateClassification.INPUT_1, LinesWithDateClassification.INPUT_2)
    
                    .addLayer(LinesWithDateClassification.HIDDEN_2, new LSTM.Builder().nIn(charIterator.inputColumns()).nOut(2)
                        .activation(Activation.TANH).build(), LinesWithDateClassification.INPUT_2)
            
                    .addVertex(LinesWithDateClassification.MERGE_VERTEX, new MergeVertex(), LinesWithDateClassification.INPUT_1, LinesWithDateClassification.HIDDEN_2)

                    .addLayer(LinesWithDateClassification.HIDDEN_3, new LSTM.Builder().nIn(charIterator.inputColumns() + 2).nOut(hidden3out)
                        .activation(Activation.TANH).build(), LinesWithDateClassification.MERGE_VERTEX)
    
                    .addLayer(LinesWithDateClassification.LAYER_OUTPUT_3, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT).activation(Activation.SOFTMAX)        //MCXENT + softmax for classification
                        .nIn(hidden3out).nOut(labelsNum).build(), LinesWithDateClassification.HIDDEN_3)
                    
                    .setOutputs(LinesWithDateClassification.LAYER_OUTPUT_3)
                    
                    .backpropType(BackpropType.TruncatedBPTT)
                    .tBPTTForwardLength(LinesWithDateClassification.CHARS_NUM_BACK_PROPAGATION_THROUGH_TIME)
                    .tBPTTBackwardLength(LinesWithDateClassification.CHARS_NUM_BACK_PROPAGATION_THROUGH_TIME)
                    .build();
    }

    private static ComputationGraphConfiguration skipConnection(CharIterator charIterator,
                                                                int nOut, int lstmLayerSize) {
        return new NeuralNetConfiguration.Builder()
            .seed(LinesWithDateClassification.SEED_12345)
            .l2(LinesWithDateClassification.L2_REGULARIZATION_COEFFICIENT_0_00001)
            .weightInit(WeightInit.XAVIER)
            .updater(new Adam(LinesWithDateClassification.LEARNING_RATE_0_01))
            .graphBuilder()
            .addInputs(LinesWithDateClassification.INPUT_1, LinesWithDateClassification.INPUT_2)
            
            .addLayer(LinesWithDateClassification.LAYER_INPUT_1, new LSTM.Builder().nIn(charIterator.inputColumns() * 2).nOut(lstmLayerSize)
                .activation(Activation.TANH).build(), LinesWithDateClassification.INPUT_1, LinesWithDateClassification.INPUT_2)
            
            .addLayer(LinesWithDateClassification.LAYER_INPUT_2, new LSTM.Builder().nIn(lstmLayerSize).nOut(lstmLayerSize)
                .activation(Activation.TANH).build(), LinesWithDateClassification.LAYER_INPUT_1)
            
            .addLayer(LinesWithDateClassification.LAYER_OUTPUT_3, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                .activation(Activation.SOFTMAX)
                .nIn(2*lstmLayerSize).nOut(nOut).build(), LinesWithDateClassification.LAYER_INPUT_1, LinesWithDateClassification.LAYER_INPUT_2)
            .setOutputs(LinesWithDateClassification.LAYER_OUTPUT_3)
            .backpropType(BackpropType.TruncatedBPTT).tBPTTForwardLength(nOut).tBPTTBackwardLength(nOut)
            .build();
    }

    static ComputationGraphConfiguration encoderDecoder(CharIterator charIterator, int labelsNum, int lstmLayerSize) {
        return new NeuralNetConfiguration.Builder()
            .seed(LinesWithDateClassification.SEED_12345)
            .l2(LinesWithDateClassification.L2_REGULARIZATION_COEFFICIENT_0_00001)
            .weightInit(WeightInit.XAVIER)
            .updater(new Adam(LinesWithDateClassification.LEARNING_RATE_0_01))
            .graphBuilder()

            .addInputs(LinesWithDateClassification.INPUT_1, LinesWithDateClassification.INPUT_2)

            .addLayer(LinesWithDateClassification.HIDDEN_2, new LSTM.Builder().nIn(2).nOut(2)
                .activation(Activation.TANH).build(), LinesWithDateClassification.INPUT_2)

            .addVertex(LinesWithDateClassification.MERGE_VERTEX, new MergeVertex(), LinesWithDateClassification.INPUT_1, LinesWithDateClassification.HIDDEN_2)

            .addLayer(LinesWithDateClassification.HIDDEN_3, new LSTM.Builder().nIn(charIterator.inputColumns() + 2).nOut(5)
                .activation(Activation.TANH).build(), LinesWithDateClassification.MERGE_VERTEX)

            .addLayer(LinesWithDateClassification.HIDDEN_4, new LSTM.Builder().nIn(5).nOut(5)
                .activation(Activation.TANH).build(), LinesWithDateClassification.HIDDEN_3)

            .addLayer(LinesWithDateClassification.HIDDEN_5, new LSTM.Builder().nIn(5).nOut(100)
                .activation(Activation.TANH).build(), LinesWithDateClassification.HIDDEN_4)

            .addLayer(LinesWithDateClassification.LAYER_OUTPUT_3, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT).activation(Activation.SOFTMAX)        //MCXENT + softmax for classification
                .nIn(100).nOut(labelsNum).build(), LinesWithDateClassification.HIDDEN_5)

            .setOutputs(LinesWithDateClassification.LAYER_OUTPUT_3)

            .backpropType(BackpropType.TruncatedBPTT)
            .tBPTTForwardLength(LinesWithDateClassification.CHARS_NUM_BACK_PROPAGATION_THROUGH_TIME)
            .tBPTTBackwardLength(LinesWithDateClassification.CHARS_NUM_BACK_PROPAGATION_THROUGH_TIME)
            .build();
    }
}
