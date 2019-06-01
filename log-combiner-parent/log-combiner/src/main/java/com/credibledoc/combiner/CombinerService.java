package com.credibledoc.combiner;

import com.credibledoc.combiner.application.ApplicationService;
import com.credibledoc.combiner.config.Config;
import com.credibledoc.combiner.config.ConfigService;
import com.credibledoc.combiner.config.TacticConfig;
import com.credibledoc.combiner.date.DateService;
import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.combiner.file.FileService;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.log.reader.ReaderService;
import com.credibledoc.combiner.node.applicationlog.ApplicationLog;
import com.credibledoc.combiner.node.applicationlog.ApplicationLogService;
import com.credibledoc.combiner.node.file.NodeFile;
import com.credibledoc.combiner.node.file.NodeFileService;
import com.credibledoc.combiner.state.FilesMergerState;
import com.credibledoc.combiner.tactic.Tactic;
import com.credibledoc.combiner.tactic.TacticService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * This stateless instance contains methods for launching of
 * the {@link CombinerCommandLineMain#LOG_COMBINER_MODULE_NAME} tool.
 *
 * @author Kyrylo Semenko
 */
public class CombinerService {
    private static final Logger logger = LoggerFactory.getLogger(CombinerService.class);

    private static final String EMPTY_STRING = "";
    public static final String REPORT_FOLDER_EXTENSION = "_generated";
    private static final String NOT_IMPLEMENTED = "Not implemented";

    /**
     * Singleton.
     */
    private static CombinerService instance;

    /**
     * @return The {@link CombinerService} singleton.
     */
    static CombinerService getInstance() {
        if (instance == null) {
            instance = new CombinerService();
        }
        return instance;
    }

    void combine(File folder, String configAbsolutePath) {
        try {
            Config config = ConfigService.getInstance().loadConfig(configAbsolutePath);
            if (config == null) {
                logger.info("Configuration not found. Files will be joined by last modification time.");
                joinFiles(folder);
                return;
            }
            prepareReader(folder, config);
            ApplicationLogService applicationLogService = ApplicationLogService.getInstance();
            List<ApplicationLog> applicationLogs = applicationLogService.getApplicationLogs();
            File targetFile = prepareTargetFile(folder);
            try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile))) {
                ReaderService readerService = ReaderService.getInstance();
                readerService.prepareBufferedReaders(applicationLogs);

                FilesMergerState filesMergerState = new FilesMergerState();
                NodeFileService nodeFileService = NodeFileService.getInstance();
                filesMergerState.setNodeFiles(nodeFileService.getNodeFiles());

                combine(outputStream, filesMergerState);
            }
            logger.info("All files combined to '{}'", targetFile.getAbsolutePath());
        } catch (Exception e) {
            throw new CombinerRuntimeException("Cannot combine files. Folder: '" + folder.getAbsolutePath() +
                "', configAbsolutePath: '" + configAbsolutePath + "'.", e);
        }
    }

    public void combine(OutputStream outputStream, FilesMergerState filesMergerState) {
        ReaderService readerService = ReaderService.getInstance();
        LogBufferedReader logBufferedReader = readerService.getCurrentReader(filesMergerState);
        int currentLineNumber = 0;
        NodeFileService nodeFileService = NodeFileService.getInstance();
        String line = null;
        Config config = ConfigService.getInstance().loadConfig(null);
        try {
            line = readerService.readLineFromReaders(filesMergerState);
            logBufferedReader = readerService.getCurrentReader(filesMergerState);
            String substring = line.substring(0, 35);
            logger.info("The first line read from {}. Line: '{}...'", ReaderService.class.getSimpleName(), substring);
            while (line != null) {
                List<String> multiline = readerService.readMultiline(line, logBufferedReader);

                currentLineNumber = currentLineNumber + multiline.size();
                if (currentLineNumber % 100000 == 0) {
                    logger.debug("{} lines processed", currentLineNumber);
                }

                writeMultiline(config, outputStream, nodeFileService, logBufferedReader, multiline);

                line = readerService.readLineFromReaders(filesMergerState);
                logBufferedReader = readerService.getCurrentReader(filesMergerState);
            }
            logger.debug("{} lines processed (100%)", currentLineNumber);
        } catch (Exception e) {
            String fileName = "null";
            if (logBufferedReader != null) {
                fileName = readerService.getFile(logBufferedReader).getAbsolutePath();
            }
            String message =
                "Creation of reports failed. File: '" + fileName +
                    "', line: '" + line + "'";
            throw new CombinerRuntimeException(message, e);
        }
    }

    /**
     * Create a {@link Tactic} instance for each {@link Config#getTacticConfigs()}.
     * <p>
     * Add created {@link Tactic} instances to the {@link com.credibledoc.combiner.tactic.TacticService}.
     * <p>
     * Add created {@link Tactic} instances to the
     * {@link com.credibledoc.combiner.node.applicationlog.ApplicationLogService}.
     * <p>
     * Call the {@link #collectApplicationLogs(File, List)} method.
     *
     * @param folder the folder with log files
     * @param config contains configuration of {@link Config#getTacticConfigs()}
     */
    public void prepareReader(File folder, Config config) {
        ApplicationLogService applicationLogService = ApplicationLogService.getInstance();
        TacticService tacticService = TacticService.getInstance();
        List<ApplicationLog> applicationLogs = applicationLogService.getApplicationLogs();
        for (final TacticConfig tacticConfig : config.getTacticConfigs()) {
            final Tactic tactic = createTactic(tacticConfig);
            tacticService.getTactics().add(tactic);

            ApplicationLog applicationLog = new ApplicationLog();
            applicationLog.setTactic(tactic);
            applicationLogs.add(applicationLog);
        }
        collectApplicationLogs(folder, applicationLogs);
    }

    private void writeMultiline(Config config, OutputStream outputStream, NodeFileService nodeFileService, LogBufferedReader logBufferedReader, List<String> multiline) throws IOException {
        NodeFile nodeFile = nodeFileService.findNodeFile(logBufferedReader);
        for (String nextLine : multiline) {
            if (config.isPrintNodeName()) {
                outputStream.write(nodeFile.getNodeLog().getName().getBytes());
                outputStream.write(" ".getBytes());
            }

            String shortName = nodeFile.getNodeLog().getApplicationLog().getTactic().getShortName();
            if (!shortName.isEmpty()) {
                outputStream.write(shortName.getBytes());
                outputStream.write(" ".getBytes());
            }

            outputStream.write(nextLine.getBytes());
            outputStream.write(System.lineSeparator().getBytes());
        }
    }

    private void joinFiles(File folder) throws IOException {
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
        File targetFile = prepareTargetFile(folder);
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

    public File prepareTargetFile(File folder) {
        File newFolder = new File(folder.getParent(), folder.getName() + REPORT_FOLDER_EXTENSION);
        boolean created = newFolder.mkdirs();
        if (created) {
            logger.info("New folder created: '{}'", newFolder.getAbsolutePath());
        }
        File newFile = new File(newFolder, "combined.txt");
        logger.info("New file created: '{}'", newFile.getAbsolutePath());
        return newFile;
    }

    private Tactic createTactic(final TacticConfig tacticConfig) {
        return new Tactic() {
                    private SimpleDateFormat simpleDateFormat =
                        new SimpleDateFormat(tacticConfig.getSimpleDateFormat());

                    private Pattern pattern = Pattern.compile(tacticConfig.getRegex());

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

    /**
     * Sort files in a directory from the first argument.
     * For each {@link Tactic} create its own list of files. For each file call the
     * {@link NodeFileService#appendToNodeLogs(Map, ApplicationLog)} method.
     *
     * @param directory       cannot be 'null'. Can have files with different {@link Tactic}s.<br>
     *                        Cannot contain other files. But can have directories. These directories
     *                        will be processed recursively.
     * @param applicationLogs at first invocation an empty, and it will be filled with files
     */
    private void collectApplicationLogs(File directory, List<ApplicationLog> applicationLogs) {
        // TODO Kyrylo Semenko - zde je chyba. Dva soubory mohou mit stejny datum
        Map<Tactic, Map<Date, File>> map = new HashMap<>();
        File[] files = Objects.requireNonNull(directory.listFiles());
        for (File file : files) {
            addFileToMap(applicationLogs, map, file);
        }
        ApplicationService applicationService = ApplicationService.getInstance();
        for (Map.Entry<Tactic, Map<Date, File>> appEntry : map.entrySet()) {
            Tactic tactic = appEntry.getKey();
            ApplicationLog applicationLog = applicationService.findOrCreate(applicationLogs, tactic);
            NodeFileService.getInstance().appendToNodeLogs(appEntry.getValue(), applicationLog);
        }
    }

    private void addFileToMap(List<ApplicationLog> applicationLogs, Map<Tactic, Map<Date, File>> map, File file) {
        // TODO Kyrylo Semenko - zde je chyba. Dva soubory mohou mit stejny datum.
        if (file.isFile()) {
            Tactic tactic = FileService.getInstance().findTactic(file);
            if (!map.containsKey(tactic)) {
                map.put(tactic, new TreeMap<Date, File>());
            }

            Date date = FileService.getInstance().findDate(file, tactic);
            if (date == null) {
                throw new CombinerRuntimeException("Cannot find a date in the file: " + file.getAbsolutePath());
            }
            map.get(tactic).put(date, file);
        } else {
            // directories
            collectApplicationLogs(file, applicationLogs);
        }
    }
}
