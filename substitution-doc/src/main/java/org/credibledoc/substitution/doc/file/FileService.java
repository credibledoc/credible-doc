package org.credibledoc.substitution.doc.file;

import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.google.common.base.Preconditions;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;
import org.credibledoc.substitution.doc.application.Application;
import org.credibledoc.substitution.doc.application.ApplicationService;
import org.credibledoc.substitution.doc.log.buffered.LogBufferedReader;
import org.credibledoc.substitution.doc.log.buffered.LogFileReader;
import org.credibledoc.substitution.doc.module.tactic.TacticHolder;
import org.credibledoc.substitution.doc.node.applicationlog.ApplicationLog;
import org.credibledoc.substitution.doc.node.file.NodeFileService;
import org.credibledoc.substitution.doc.specific.SpecificTactic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    private static final char DOT = '.';

    public static final String REPORT_FOLDER_EXTENSION = ".report";

    @NonNull
    private final ApplicationService applicationService;

    @NonNull
    private final NodeFileService nodeFileService;

    @NonNull
    private final ApplicationContext applicationContext;

    /**
     * Generate a new file with transformed content of source log files.
     * @param fileNumber an order number. Can be 'null' if a single file
     *                   should be created in the directory. If not 'null',
     *                   this number will be appended to the file name,
     *                   for example index_001.html
     * @param fileName the file name without extension
     * @param reportDirectory where the new file will be created
     * @param fileExtension for example html or txt
     *
     * @return the new file with context transformed from the source log file.
     */
    public File generateFile(Integer fileNumber, String fileName, File reportDirectory, String fileExtension) {
        String num = null;
        if (fileNumber != null) {
            num = String.format("%03d", fileNumber);
        }
        StringBuilder newFileName = new StringBuilder(fileName);
        if (num != null) {
            newFileName.append(DOT).append(num);
        }
        newFileName.append(DOT).append(fileExtension);
        File file = new File(reportDirectory, newFileName.toString());
        logger.info("The new empty file created: '{}'", file.getAbsolutePath());
        return file;
    }

    /**
     * Recognize, which {@link TacticHolder} this file belongs to.
     * @param file the log file
     * @return {@link TacticHolder} or throw the new {@link SubstitutionRuntimeException} if the file not recognized
     */
    public TacticHolder findOutApplicationType(File file) {
        try (LogBufferedReader logBufferedReader = new LogBufferedReader(new LogFileReader(file))) {
            String line = logBufferedReader.readLine();
            while (line != null) {
                TacticHolder application = applicationService.findApplication(line, logBufferedReader);
                if (application != null) {
                    return application;
                }
                line = logBufferedReader.readLine();
            }
            throw new SubstitutionRuntimeException("Cannot recognize application type of the file: " + file.getAbsolutePath());
        } catch (Exception e) {
            throw new SubstitutionRuntimeException(e);
        }
    }

    /**
     * Sort files in a directory from the first argument.
     * For each {@link TacticHolder} creates its own list of files.
     *
     * @param directory       cannot be 'null'. Can have files from different {@link TacticHolder}s.<br>
     *                        Cannot contain other files. But can have directories. These directories
     *                        will be processed recursively.
     * @param applicationLogs at first invocation an empty, and it will be filled with files
     */
    public void collectApplicationLogs(File directory, List<ApplicationLog> applicationLogs) {
        Preconditions.checkNotNull(directory);
        Preconditions.checkState(directory.isDirectory());
        Map<TacticHolder, Map<Date, File>> map = new EnumMap<>(TacticHolder.class);
        File[] files = Objects.requireNonNull(directory.listFiles());
        for (File file : files) {
            addFileToMap(applicationLogs, map, files, file);
        }
        for (Entry<TacticHolder, Map<Date, File>> appEntry : map.entrySet()) {
            TacticHolder tacticHolder = appEntry.getKey();
            ApplicationLog applicationLog = applicationService.findOrCreate(applicationLogs, tacticHolder);
            nodeFileService.appendToNodeLogs(appEntry.getValue(), applicationLog);
        }
    }

    private void addFileToMap(List<ApplicationLog> applicationLogs, Map<TacticHolder,
            Map<Date, File>> map, File[] files, File file) {

        if (file.isFile()) {
            if (file.getName().endsWith(".zip")) {
                file = unzipIfNotExists(file, files);
            }
            TacticHolder application = findOutApplicationType(file);
            if (!map.containsKey(application)) {
                map.put(application, new TreeMap<>());
            }

            Date date = findOutDate(file, application);
            if (date == null) {
                throw new SubstitutionRuntimeException("Cannot find a date in the file: " + file.getAbsolutePath());
            }
            map.get(application).put(date, file);
        } else {
            // directories
            if (!file.getName().endsWith(REPORT_FOLDER_EXTENSION)) {
                collectApplicationLogs(file, applicationLogs);
            }
        }
    }

    /**
     * Find out date and time of the first line in a file.
     *
     * @param file        an application log
     * @param application each {@link TacticHolder} has its own strategy of date searching
     * @return the most recent date and time
     */
    public Date findOutDate(File file, Application application) {
        Class<? extends SpecificTactic> dateFinderStrategyClass = application.getSpecificTacticClass();
        SpecificTactic specificTactic = applicationContext.getBean(dateFinderStrategyClass);
        return specificTactic.findDate(file);
    }

    /**
     * If the second argument contains unzipped first argument, do not unzip it.
     * Else unzip it and return a file from this zipFile.
     * @param zipFile zipped log file
     * @param files all files in a directory
     * @return an unzipped file or file from files
     */
    public File unzipIfNotExists(File zipFile, File[] files) {
        try (
            InputStream is = new FileInputStream(zipFile);
            ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream("zip", is)
        ) {
            ZipArchiveEntry zipArchiveEntry = (ZipArchiveEntry) ais.getNextEntry();
            String zipArchiveEntryName = zipArchiveEntry.getName();
            for (File file : files) {
                if (file.getName().equals(zipArchiveEntryName)) {
                    return file;
                }
            }
            File outputFile = new File(zipFile.getParentFile(), zipArchiveEntryName);
            logger.info("File {}:{} will be decompressed to {}", zipFile.getName(), zipArchiveEntryName, outputFile.getAbsolutePath());
            IOUtils.copy(ais, new FileOutputStream(outputFile));
            return outputFile;
        } catch (Exception e) {
            throw new SubstitutionRuntimeException("Cannot unzipIfNotExists file: " + zipFile.getAbsolutePath(), e);
        }
    }

}

