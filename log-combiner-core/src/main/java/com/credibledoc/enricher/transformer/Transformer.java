package com.credibledoc.enricher.transformer;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.enricher.deriving.Deriving;

import java.util.List;

/**
 * Transforms strings, see the
 * {@link #transform(Deriving, List, LogBufferedReader)} method.
 * @author Kyrylo Semenko
 */
public interface Transformer {

    /**
     * Transform log lines to some form, for example to a PlantUML, HTML or plain text element.
     *
     * @param deriving    a state of the document where the transformed line will be placed
     * @param multiLine         one or more lines from the {@link LogBufferedReader}
     *                          data source. Only the first line contains a time stamp and thread name.
     *                          Other lines can have additional information, for example formatted requests.
     * @param logBufferedReader the data source created from log files
     * @return transformed string without a line separator at the end.
     * It will be printed out immediately to the {@link Deriving#getPrintWriter()}.
     */
    String transform(Deriving deriving, List<String> multiLine, LogBufferedReader logBufferedReader);

}
