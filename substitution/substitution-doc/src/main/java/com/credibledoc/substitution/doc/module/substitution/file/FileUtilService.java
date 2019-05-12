package com.credibledoc.substitution.doc.module.substitution.file;

import com.credibledoc.combiner.application.Application;
import com.credibledoc.combiner.application.ApplicationService;
import com.credibledoc.combiner.file.FileService;
import com.credibledoc.combiner.node.applicationlog.ApplicationLog;
import com.credibledoc.combiner.node.file.NodeFileService;
import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class FileUtilService {

    private static final Logger logger = LoggerFactory.getLogger(FileUtilService.class);

    private static final char DOT = '.';

    private static final String REPORT_FOLDER_EXTENSION = ".report";

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
     * Sort files in a directory from the first argument.
     * For each {@link Application} creates its own list of files.
     *
     * @param directory       cannot be 'null'. Can have files from different {@link Application}s.<br>
     *                        Cannot contain other files. But can have directories. These directories
     *                        will be processed recursively.
     * @param applicationLogs at first invocation an empty, and it will be filled with files
     */
    private void collectApplicationLogs(File directory, List<ApplicationLog> applicationLogs) {
        Preconditions.checkNotNull(directory);
        Preconditions.checkState(directory.isDirectory());
        Map<Application, Map<Date, File>> map = new HashMap<>();
        File[] files = Objects.requireNonNull(directory.listFiles());
        for (File file : files) {
            addFileToMap(applicationLogs, map, files, file);
        }
        ApplicationService applicationService = ApplicationService.getInstance();
        for (Entry<Application, Map<Date, File>> appEntry : map.entrySet()) {
            Application application = appEntry.getKey();
            ApplicationLog applicationLog = applicationService.findOrCreate(applicationLogs, application);
            NodeFileService.getInstance().appendToNodeLogs(appEntry.getValue(), applicationLog);
        }
    }

    private void addFileToMap(List<ApplicationLog> applicationLogs, Map<Application,
            Map<Date, File>> map, File[] files, File file) {

        if (file.isFile()) {
            if (file.getName().endsWith(".zip")) {
                file = unzipIfNotExists(file, files);
            }
            Application application = FileService.getInstance().findApplication(file);
            if (!map.containsKey(application)) {
                map.put(application, new TreeMap<>());
            }

            Date date = FileService.getInstance().findDate(file, application);
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
     * If the second argument contains unzipped first argument, do not unzip it.
     * Else unzip it and return a file from this zipFile.
     * @param zipFile zipped log file
     * @param files all files in a directory
     * @return an unzipped file or file from files
     */
    private File unzipIfNotExists(File zipFile, File[] files) {
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
