package com.credibledoc.enricher.record;

import com.credibledoc.combiner.context.CombinerContext;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.node.file.NodeFileService;

import java.util.Date;
import java.util.List;

/**
 * A service for working with {@link Record} instances.
 */
public class RecordService {

    /**
     * Singleton.
     */
    private static final RecordService instance = new RecordService();

    /**
     * @return The {@link RecordService} singleton.
     */
    static RecordService getInstance() {
        return instance;
    }

    public Record createRecord(LogBufferedReader logBufferedReader, List<String> multiLine,
                               Date firstLineDate, CombinerContext combinerContext) {
        Record record = new Record(multiLine, firstLineDate);
        record.setNodeFile(NodeFileService.getInstance().findNodeFile(logBufferedReader, combinerContext));
        return record;
    }
}
