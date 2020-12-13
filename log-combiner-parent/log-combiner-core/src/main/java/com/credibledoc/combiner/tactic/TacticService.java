package com.credibledoc.combiner.tactic;

import com.credibledoc.combiner.context.CombinerContext;
import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.combiner.file.FileService;
import com.credibledoc.combiner.file.FileWithSources;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.log.reader.ReaderService;
import com.credibledoc.combiner.node.file.NodeFile;
import com.credibledoc.combiner.node.file.NodeFileService;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Service for working with {@link Tactic}.
 *
 * @author Kyrylo Semenko
 */
public class TacticService {

    /**
     * Singleton.
     */
    private static final TacticService instance = new TacticService();

    /**
     * @return The {@link TacticService} singleton.
     */
    public static TacticService getInstance() {
        return instance;
    }

    /**
     * Recognize, which {@link Tactic} the line belongs to.
     * @param line the line from the log file
     * @param logBufferedReader the {@link LogBufferedReader} read the line
     * @param combinerContext the current state
     * @return {@link Tactic} or 'null' if not found
     */
    public Tactic findTactic(String line, LogBufferedReader logBufferedReader, CombinerContext combinerContext) {
        Set<Tactic> tactics = combinerContext.getTacticRepository().getTactics();
        if (tactics.isEmpty()) {
            throw new CombinerRuntimeException("TacticRepository is empty.");
        }
        for (Tactic tactic : tactics) {
            if (tactic.identifyApplication(line, logBufferedReader)) {
                return tactic;
            }
        }
        return null;
    }

    /**
     * Recognize, which {@link Tactic} the line belongs to.
     * @param logBufferedReader links to a {@link Tactic}
     * @param combinerContext the current state
     * @return {@link Tactic} or throw exception
     */
    public Tactic findTactic(LogBufferedReader logBufferedReader, CombinerContext combinerContext) {
        for (NodeFile nodeFile : combinerContext.getNodeFileRepository().getNodeFiles()) {
            if (nodeFile.getLogBufferedReader() == logBufferedReader) {
                return nodeFile.getNodeLog().getTactic();
            }
        }
        throw new CombinerRuntimeException("Tactic cannot be found. LogBufferedReader: " + logBufferedReader);
    }

    /**
     * For each file find out its {@link Tactic} by calling the {@link FileService#findTactic(File, CombinerContext)} method.
     * <p>
     * Append this file to {@link com.credibledoc.combiner.node.file.NodeFileRepository} by calling the
     * {@link NodeFileService#appendToNodeLogs(FileWithSources, Date, Tactic, CombinerContext)} method.
     * <p>
     * After all call the {@link ReaderService#prepareBufferedReaders(CombinerContext)} method.
     *
     * @param sources   log files
     * @param combinerContext the actual state of the current application
     */
    public void prepareReaders(List<FileWithSources> sources, CombinerContext combinerContext) {
        NodeFileService nodeFileService = NodeFileService.getInstance();

        for (FileWithSources fileWithSources : sources) {
            File file = fileWithSources.getFile();
            Tactic tactic = FileService.getInstance().findTactic(file, combinerContext);

            Date date = FileService.getInstance().findDate(file, tactic);

            if (date == null) {
                throw new CombinerRuntimeException("Cannot find a date in the file: " + file.getAbsolutePath());
            }
            nodeFileService.appendToNodeLogs(fileWithSources, date, tactic, combinerContext);
        }

        ReaderService readerService = ReaderService.getInstance();
        readerService.prepareBufferedReaders(combinerContext);
    }
}
