package com.credibledoc.combiner.tactic;

import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.combiner.file.FileService;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.log.reader.ReaderService;
import com.credibledoc.combiner.node.file.NodeFileService;
import com.credibledoc.combiner.node.log.NodeLog;
import com.credibledoc.combiner.node.log.NodeLogService;

import java.io.File;
import java.util.*;

/**
 * Service for working with {@link Tactic}.
 *
 * @author Kyrylo Semenko
 */
public class TacticService {

    /**
     * Singleton.
     */
    private static TacticService instance;

    /**
     * @return The {@link TacticService} singleton.
     */
    public static TacticService getInstance() {
        if (instance == null) {
            instance = new TacticService();
        }
        return instance;
    }

    /**
     * @return All {@link Tactic} instances from the {@link TacticRepository}.
     */
    public Set<Tactic> getTactics() {
        return TacticRepository.getInstance().getTactics();
    }

    /**
     * Recognize, which {@link Tactic} the line belongs to.
     * @param line the line from the log file
     * @param logBufferedReader the {@link LogBufferedReader} read the line
     * @return {@link Tactic} or 'null' if not found
     */
    public Tactic findTactic(String line, LogBufferedReader logBufferedReader) {
        TacticService tacticService = getInstance();
        for (Tactic tactic : tacticService.getTactics()) {
            if (tactic.identifyApplication(line, logBufferedReader)) {
                return tactic;
            }
        }
        return null;
    }

    /**
     * Recognize, which {@link Tactic} the line belongs to.
     * @param logBufferedReader links to a {@link Tactic}
     * @return {@link Tactic} or throw exception
     */
    public Tactic findTactic(LogBufferedReader logBufferedReader) {
        for (Tactic tactic : getInstance().getTactics()) {
            for (NodeLog nodeLog : NodeLogService.getInstance().findNodeLogs(tactic)) {
                if (nodeLog.getLogBufferedReader() == logBufferedReader) {
                    return tactic;
                }
            }
        }
        throw new CombinerRuntimeException("Tactic cannot be found. LogBufferedReader: " + logBufferedReader);
    }

    /**
     * For each file find out its {@link Tactic} by calling the {@link FileService#findTactic(File)} method.
     * <p>
     * Append this file to {@link com.credibledoc.combiner.node.file.NodeFileRepository} by calling the
     * {@link NodeFileService#appendToNodeLogs(File, Date, Tactic)} method.
     * <p>
     * After all call the {@link ReaderService#prepareBufferedReaders(Set)} method.
     *
     * @param files   log files
     * @param tactics parsers for log files
     */
    public void prepareReaders(Set<File> files, Set<Tactic> tactics) {
        TacticService tacticService = getInstance();
        tacticService.getTactics().addAll(tactics);

        NodeFileService nodeFileService = NodeFileService.getInstance();

        for (File file : files) {
            Tactic tactic = FileService.getInstance().findTactic(file);

            Date date = FileService.getInstance().findDate(file, tactic);

            if (date == null) {
                throw new CombinerRuntimeException("Cannot find a date in the file: " + file.getAbsolutePath());
            }
            nodeFileService.appendToNodeLogs(file, date, tactic);
        }

        ReaderService readerService = ReaderService.getInstance();
        readerService.prepareBufferedReaders(tacticService.getTactics());
    }
}
