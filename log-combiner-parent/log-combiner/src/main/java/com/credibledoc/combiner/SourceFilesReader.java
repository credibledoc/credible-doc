package com.credibledoc.combiner;

import com.credibledoc.combiner.context.CombinerContext;
import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.log.reader.ReaderService;
import com.credibledoc.combiner.node.file.NodeFileService;
import com.credibledoc.combiner.state.FilesMergerState;
import com.credibledoc.combiner.tactic.Tactic;
import com.credibledoc.combiner.tactic.TacticService;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Stateful instance with {@link CombinerContext} and methods for reading lines from parsed files.
 */
public class SourceFilesReader {
    
    private Set<File> files;
    
    private FilesMergerState filesMergerState;
    
    private LogBufferedReader logBufferedReader;
    
    private CombinerContext combinerContext;

    public void addSourceFiles(Set<File> sourceFiles) {
        this.files = sourceFiles;
    }

    public List<String> read() {
        if (filesMergerState == null) {
            if (files.isEmpty()) {
                throw new CombinerRuntimeException("The source files collection is empty.");
            }
            TacticService.getInstance().prepareReaders(files, combinerContext);
            
            filesMergerState = new FilesMergerState();
            filesMergerState.setNodeFiles(combinerContext.getNodeFileRepository().getNodeFiles());
        }
        String line = ReaderService.getInstance().readLineFromReaders(filesMergerState);
        logBufferedReader = filesMergerState.getCurrentNodeFile().getLogBufferedReader();
        if (line == null) {
            return null;
        }
        if (logBufferedReader.isNotClosed()) {
            return ReaderService.getInstance().readMultiline(line, logBufferedReader, combinerContext);
        }
        return null;
    }

    public File currentFile(CombinerContext combinerContext) {
        return NodeFileService.getInstance().findNodeFile(logBufferedReader, combinerContext).getFile();
    }

    public Tactic currentTactic(CombinerContext combinerContext) {
        return TacticService.getInstance().findTactic(logBufferedReader, combinerContext);
    }

    public void setCombinerContext(CombinerContext combinerContext) {
        this.combinerContext = combinerContext;
    }
}
