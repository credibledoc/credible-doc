package com.credibledoc.combiner;

import com.credibledoc.combiner.config.Config;
import com.credibledoc.combiner.config.ConfigService;
import com.credibledoc.combiner.config.TacticConfig;
import com.credibledoc.combiner.context.CombinerContext;
import com.credibledoc.combiner.date.DateService;
import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.combiner.file.FileService;
import com.credibledoc.combiner.file.FileWithSources;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.log.reader.ReaderService;
import com.credibledoc.combiner.node.file.NodeFile;
import com.credibledoc.combiner.node.file.NodeFileService;
import com.credibledoc.combiner.state.FilesMergerState;
import com.credibledoc.combiner.tactic.Tactic;
import com.credibledoc.combiner.tactic.TacticService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This stateless instance contains methods for launching
 * the {@link CombinerCommandLineMain#LOG_COMBINER_MODULE_NAME} tool.
 *
 * @author Kyrylo Semenko
 */
public class CombinerService {
    private static final Logger logger = LoggerFactory.getLogger(CombinerService.class);

    private static final String EMPTY_STRING = "";
    private static final String NOT_IMPLEMENTED = "Not implemented";

    /**
     * Singleton.
     */
    private static final CombinerService instance = new CombinerService();

    /**
     * @return The {@link CombinerService} singleton.
     */
    public static CombinerService getInstance() {
        return instance;
    }

    /**
     * Load configuration by calling the {@link ConfigService#loadConfig(String)} method.
     * <p>
     * If the configuration have no {@link Config#getTacticConfigs()} defined, all log files will be
     * joined by calling the {@link #joinFiles(File, String)} method.
     * <p>
     * Else prepare a log files reader by calling the {@link #prepareReader(File, Config, CombinerContext)} method.
     * <p>
     * And finally combine files line by line by calling the {@link #combine(OutputStream, FilesMergerState, CombinerContext)} method.
     *
     * @param sourceFolder a folder with log files
     * @param configAbsolutePath this configuration file will be used for filling out a {@link Config} instance
     * @param combinerContext the current state
     */
    public void combine(File sourceFolder, String configAbsolutePath, CombinerContext combinerContext) {
        try {
            Config config = new ConfigService().loadConfig(configAbsolutePath);
            if (config.getTacticConfigs().isEmpty()) {
                logger.info("Configuration not found. Files will be joined by last modification time.");
                joinFiles(sourceFolder, config.getTargetFileName());
                return;
            }
            prepareReader(sourceFolder, config, combinerContext);
            File targetFile = prepareTargetFile(sourceFolder, config.getTargetFileName());
            try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile))) {
                ReaderService readerService = ReaderService.getInstance();
                readerService.prepareBufferedReaders(combinerContext);

                FilesMergerState filesMergerState = new FilesMergerState();
                filesMergerState.setNodeFiles(combinerContext.getNodeFileRepository().getNodeFiles());

                combine(outputStream, filesMergerState, combinerContext);
            }
            logger.info("All files combined to '{}'", targetFile.getAbsolutePath());
        } catch (Exception e) {
            throw new CombinerRuntimeException("Cannot combine files. Folder: '" + sourceFolder.getAbsolutePath() +
                "', configAbsolutePath: '" + configAbsolutePath + "'.", e);
        }
    }

    /**
     * Merge files with default {@link Config}.
     * @param outputStream target stream for merged lines
     * @param filesMergerState state object of the merge process 
     * @param combinerContext state object of the current repositories
     */
    public void combine(OutputStream outputStream, FilesMergerState filesMergerState, CombinerContext combinerContext) {
        ReaderService readerService = ReaderService.getInstance();
        if (filesMergerState.getCurrentNodeFile() == null) {
            filesMergerState.setCurrentNodeFile(readerService.findTheOldest(filesMergerState));
        }
        LogBufferedReader logBufferedReader = filesMergerState.getCurrentNodeFile().getLogBufferedReader();
        int currentLineNumber = 0;
        NodeFileService nodeFileService = NodeFileService.getInstance();
        String line = null;
        Config config = new ConfigService().loadConfig(null);
        try {
            line = readerService.readLineFromReaders(filesMergerState);
            logBufferedReader = filesMergerState.getCurrentNodeFile().getLogBufferedReader();
            int endIndex = Math.max(line.length(), 35);
            String substring = line.substring(0, endIndex);
            logger.trace("The first line is read from {}. Line: '{}...'", getClass().getSimpleName(), substring);
            while (line != null) {
                List<String> multiline = readerService.readMultiline(line, logBufferedReader, combinerContext);

                currentLineNumber = currentLineNumber + multiline.size();
                if (currentLineNumber % 100000 == 0) {
                    logger.debug("{} lines processed", currentLineNumber);
                }

                writeMultiline(config, outputStream, nodeFileService, logBufferedReader, multiline, combinerContext);

                line = readerService.readLineFromReaders(filesMergerState);
                logBufferedReader = filesMergerState.getCurrentNodeFile().getLogBufferedReader();
            }
            logger.debug("{} lines processed (100%)", currentLineNumber);
        } catch (Exception e) {
            String fileName = "null";
            if (logBufferedReader != null) {
                fileName = readerService.getFile(logBufferedReader).getAbsolutePath();
            }
            String message =
                "Reports creation failed. File: '" + fileName +
                    "', line: '" + line + "'";
            throw new CombinerRuntimeException(message, e);
        }
    }

    /**
     * Create a {@link Tactic} instance for each {@link Config#getTacticConfigs()}.
     * <p>
     * Add created {@link Tactic} instances to the {@link com.credibledoc.combiner.tactic.TacticService}.
     * <p>
     * Call the {@link TacticService#prepareReaders(List, CombinerContext)} method.
     *
     * @param folder the folder with log files
     * @param config contains configuration of {@link Config#getTacticConfigs()}
     * @param combinerContext the current state
     */
    public void prepareReader(File folder, Config config, CombinerContext combinerContext) {
        TacticService tacticService = TacticService.getInstance();
        List<TacticConfig> tacticConfigs = config.getTacticConfigs();
        if (tacticConfigs.isEmpty()) {
            throw new CombinerRuntimeException("TacticConfig is empty");
        }
        for (final TacticConfig tacticConfig : tacticConfigs) {
            final Tactic tactic = createTactic(tacticConfig);
            combinerContext.getTacticRepository().getTactics().add(tactic);
        }
        FileWithSources source = new FileWithSources();
        source.getSources().add(folder);
        List<FileWithSources> files = FileService.getInstance().collectFiles(source);

        tacticService.prepareReaders(files, combinerContext);
    }

    private void writeMultiline(Config config, OutputStream outputStream, NodeFileService nodeFileService,
                                LogBufferedReader logBufferedReader, List<String> multiline,
                                CombinerContext combinerContext) throws IOException {
        NodeFile nodeFile = nodeFileService.findNodeFile(logBufferedReader, combinerContext);
        for (String nextLine : multiline) {
            if (config.isPrintNodeName()) {
                outputStream.write(nodeFile.getNodeLog().getName().getBytes());
                outputStream.write(" ".getBytes());
            }

            String shortName = nodeFile.getNodeLog().getTactic().getShortName();
            if (shortName != null && !shortName.isEmpty()) {
                outputStream.write(shortName.getBytes());
                outputStream.write(" ".getBytes());
            }

            outputStream.write(nextLine.getBytes());
            outputStream.write(System.lineSeparator().getBytes());
        }
    }

    private void joinFiles(File folder, String targetFileName) throws IOException {
        List<File> files = new ArrayList<>();
        collectFilesRecursively(folder, files);
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File left, File right) {
                if (left.lastModified() == right.lastModified()) {
                    return 0;
                }
                return left.lastModified() > right.lastModified() ? 1 : -1;
            }
        });
        File targetFile = prepareTargetFile(folder, targetFileName);
        byte[] buffer = new byte[1024];
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile))) {
            for (File file : files) {
                try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                    int lengthRead;
                    while ((lengthRead = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, lengthRead);
                    }
                    if (file != files.get(files.size() - 1)) {
                        outputStream.write(System.lineSeparator().getBytes());
                        logger.info("File combined and line separator appended: '{}'", file.getAbsolutePath());
                    } else {
                        logger.info("File combined: '{}'", file.getAbsolutePath());
                    }
                    outputStream.flush();
                }
            }
        }
        logger.info("All files combined to '{}'", targetFile.getAbsolutePath());
    }

    private void collectFilesRecursively(File folder, List<File> collectedFiles) {
        File[] files = folder.listFiles();
        if (files == null) {
            throw new CombinerRuntimeException("The file is not a folder. File: '" + folder.getAbsolutePath() + "'");
        }
        for (File file : files) {
            if (file.isFile()) {
                collectedFiles.add(file);
            } else {
                collectFilesRecursively(file, collectedFiles);
            }
        }
    }

    /**
     * Create target folder if doesn't exist and log the information.
     * @param targetFolder for example path/folder
     * @param targetFileName for example file.txt
     * @return The target file, for example path/folder/file.txt
     */
    public File prepareTargetFile(File targetFolder, String targetFileName) {
        File newFolder = new File(targetFolder.getParent(), targetFolder.getName());
        boolean created = newFolder.mkdirs();
        if (created) {
            logger.info("New folder created: '{}'", newFolder.getAbsolutePath());
        }
        File newFile = new File(newFolder, targetFileName);
        logger.info("New file created: '{}'", newFile.getAbsolutePath());
        return newFile;
    }

    private Tactic createTactic(final TacticConfig tacticConfig) {
        return new Tactic() {
                    private final SimpleDateFormat simpleDateFormat =
                        new SimpleDateFormat(tacticConfig.getSimpleDateFormat());

                    private final Pattern pattern = Pattern.compile(tacticConfig.getRegex());

                    @Override
                    public Date findDate(File file) {
                        Date date = DateService.getInstance()
                            .findDateInFile(file, simpleDateFormat, pattern, tacticConfig.getMaxIndexEndOfTime());
                        if (date == null) {
                            throw new CombinerRuntimeException("Cannot recognize some line with Date pattern " +
                                tacticConfig.getSimpleDateFormat() +
                                " in file: " + file.getAbsolutePath());
                        }
                        return date;
                    }

                    @Override
                    public Date findDate(String line, NodeFile nodeFile) {
                        int maxIndex = tacticConfig.getMaxIndexEndOfTime() == null ?
                            line.length() : tacticConfig.getMaxIndexEndOfTime();

                        return DateService.getInstance().parseDateTimeFromLine(
                            line, simpleDateFormat, pattern, maxIndex);
                    }

                    @Override
                    public boolean containsDate(String line) {
                        return findDate(line) != null;
                    }

                    @Override
                    public String parseDateStingFromLine(String line) {
                        throw new CombinerRuntimeException(NOT_IMPLEMENTED);
                    }

                    @Override
                    public String findThreadName(String line) {
                        throw new CombinerRuntimeException(NOT_IMPLEMENTED);
                    }

                    @Override
                    public Date findDate(String line) {
                        return findDate(line, null);
                    }

                    @Override
                    public String getShortName() {
                        return tacticConfig.getApplicationName() != null ?
                            tacticConfig.getApplicationName() : EMPTY_STRING;
                    }

                    @Override
                    public boolean identifyApplication(String line, LogBufferedReader logBufferedReader) {
                        return containsDate(line);
                    }
        };
    }

}
