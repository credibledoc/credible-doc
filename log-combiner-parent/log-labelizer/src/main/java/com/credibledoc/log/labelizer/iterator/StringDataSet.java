package com.credibledoc.log.labelizer.iterator;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;

import java.util.List;

/**
 * This extension of {@link DataSet} contains {@link #rawData} for better understanding of underlying {@link #DataSet}.
 * 
 * @author Kyrylo Semenko
 */
public class StringDataSet extends DataSet {
    /**
     * Source data. For debugging and documentation purposes only.
     */
    private List<String> rawData;

    public StringDataSet() {
        super();
    }

    /**
     * Get the {@link #rawData} and call the parent {@link DataSet#DataSet(INDArray, INDArray)} constructor.
     */
    public StringDataSet(INDArray first, INDArray second, List<String> rawData) {
        super(first, second);
        this.rawData = rawData;
    }

    /**
     * Get the {@link #rawData} and call the parent {@link DataSet#DataSet(INDArray, INDArray, INDArray, INDArray)}
     * constructor.
     */
    public StringDataSet(INDArray features, INDArray labels, INDArray featuresMask, INDArray labelsMask, List<String> rawData) {
        super(features, labels, featuresMask, labelsMask);
        this.rawData = rawData;
    }

    /**
     * @return The {@link #rawData} field value.
     */
    public List<String> getRawData() {
        return rawData;
    }

    /**
     * @param rawData see the {@link #rawData} field description.
     */
    public void setRawData(List<String> rawData) {
        this.rawData = rawData;
    }
}
