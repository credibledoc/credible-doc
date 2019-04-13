package com.credibledoc.substitution.doc.record;

import com.credibledoc.substitution.doc.filesmerger.log.buffered.LogBufferedReader;
import com.credibledoc.substitution.doc.filesmerger.node.file.NodeFileService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

/**
 * A service for working with {@link Record} instances.
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class RecordService {

    @NonNull
    public NodeFileService nodeFileService;

    public Record createRecord(LogBufferedReader logBufferedReader, List<String> multiLine, Date firstLineDate) {
        Record record = new Record(multiLine, firstLineDate);
        record.setNodeFile(nodeFileService.findNodeFile(logBufferedReader));
        return record;
    }
}
