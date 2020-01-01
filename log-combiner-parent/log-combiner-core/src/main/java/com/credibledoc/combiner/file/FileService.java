package com.credibledoc.combiner.file;

import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.log.buffered.LogFileReader;
import com.credibledoc.combiner.log.reader.ReaderService;
import com.credibledoc.combiner.tactic.Tactic;
import com.credibledoc.combiner.tactic.TacticService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This singleton helps to collect log files and contains some util methods.
 *
 * @author Kyrylo Semenko
 */
public class FileService {
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    private static final int MAX_FILE_NAME_LENGTH_250 = 250;
    /**
     * Singleton.
     */
    private static FileService instance;

    /**
     * @return The {@link FileService} singleton.
     */
    public static FileService getInstance() {
        if (instance == null) {
            instance = new FileService();
        }
        return instance;
    }

    /**
     * Recognize which {@link Tactic} this file belongs to.
     * @param file the log file
     * @return {@link Tactic} or throw the new {@link CombinerRuntimeException} if the file not recognized
     */
    public Tactic findTactic(File file) {
        TacticService tacticService = TacticService.getInstance();
        try (LogBufferedReader logBufferedReader = new LogBufferedReader(new LogFileReader(file))) {
            String line = logBufferedReader.readLine();
            while (line != null) {
                Tactic tactic = tacticService.findTactic(line, logBufferedReader);
                if (tactic != null) {
                    return tactic;
                }
                line = logBufferedReader.readLine();
            }
            throw new CombinerRuntimeException("Cannot recognize Tactic type for the file: " + file.getAbsolutePath());
        } catch (Exception e) {
            throw new CombinerRuntimeException(e);
        }
    }

    /**
     * Find out date and time of the first line in a file.
     *
     * @param file        a log file
     * @param tactic each {@link Tactic} has its own strategy of date searching
     * @return the most recent date and time
     */
    public Date findDate(File file, Tactic tactic) {
        return tactic.findDate(file);
    }

    /**
     * If the second argument contains unzipped first argument, do not unzip it.
     * Else unzip it and return a file from this zipFile. So existing files will NOT be overwritten.
     *
     * @param zipFile         zipped content.
     * @param targetDirectory where the content will bew unzipped. If 'null', the parent directory of a zip file
     *                        will bew used and the packed content will be unpacked to the zipFile parent directory.
     * @return Unzipped files or found files from the targetDirectory.
     */
    public List<File> unzipIfNotExists(File zipFile, File targetDirectory) {
        List<File> result = new ArrayList<>();
        try (ZipFile file = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = file.entries();

            String targetPath = targetDirectory.getAbsolutePath() + File.separator;

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    File dir = new File(targetPath + entry.getName());
                    if (!dir.exists()) {
                        logger.trace("Creating Directory: '{}'", dir.getAbsolutePath());
                        Files.createDirectories(dir.toPath());
                    }
                }
                else {
                    unzipEntry(result, file, targetPath, entry);
                }
            }
        } catch (IOException e) {
            throw new CombinerRuntimeException("Cannot unzip " + zipFile.getAbsolutePath(), e);
        }
        return result;
    }

    private void unzipEntry(List<File> result, ZipFile file, String targetPath, ZipEntry entry) throws IOException {
        InputStream is = file.getInputStream(entry);
        BufferedInputStream bis = new BufferedInputStream(is);
        File nextFile = new File(targetPath + entry.getName());
        if (!nextFile.exists()) {
            Files.createFile(nextFile.toPath());
            byte[] buffer = new byte[1024];
            try (FileOutputStream fos = new FileOutputStream(nextFile)) {
                int len;
                while ((len = bis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
            }
            
            logger.trace("File unzipped: {}", nextFile.getAbsolutePath());
        } else {
            logger.trace("File already exists: {}", nextFile.getAbsolutePath());
        }
        result.add(nextFile);
    }

    /**
     * Search for files recursively in the source directories defined in the first argument.
     * <p>
     * All found files will be copied to the targetDirectory. Files with the same names will be rewritten.
     * If the targetDirectory the same as the source directory, files will not be copied to the target directory.
     *
     * @param logDirectoriesOrFiles one or more directories with log files generated by some applications. If this
     *                              argument containing multiple directories, a new sub-directory will be created
     *                              for each source directory.
     * @param unpackFiles           if 'true', unzip files to the targetDirectory.
     * @param targetDirectory       the target directory where all files will be copied.
     *                              It can be 'null'. In this case unzipped files
     *                              will be placed to the source directory next to source zip files.
     * @return Set of copied files.
     */
    public Set<File> collectFiles(Set<File> logDirectoriesOrFiles, boolean unpackFiles, File targetDirectory) {
        createTargetDirectoryIfNotExists(targetDirectory);
        Set<File> result = new HashSet<>();
        for (File file : logDirectoriesOrFiles) {
            if (targetDirectory == null) {
                copyAndCollectFilesRecursively(file, unpackFiles, file.getParentFile(), result, false);
            } else {
                copyAndCollectFilesRecursively(file, unpackFiles, targetDirectory, result, true);
            }
        }
        return result;
    }

    /**
     * If a target directory exists, do nothing. Else if the targetDirectory name is larger then
     * {@value #MAX_FILE_NAME_LENGTH_250}, throw an exception. Else create the target directory.
     *
     * @param targetDirectory that will be created.
     */
    public void createTargetDirectoryIfNotExists(File targetDirectory) {
        if (targetDirectory != null && !targetDirectory.exists()) {
            if (targetDirectory.getAbsolutePath().length() > MAX_FILE_NAME_LENGTH_250) {
                throw new CombinerRuntimeException("TargetDirectory name length is greater than " +
                    MAX_FILE_NAME_LENGTH_250 + ". File name: " + targetDirectory.getAbsolutePath());
            }
            boolean created = targetDirectory.mkdirs();
            if (!created) {
                throw new CombinerRuntimeException("Cannot create directory: '" +
                    targetDirectory.getAbsolutePath() + "'");
            }
        }
    }

    private void copyAndCollectFilesRecursively(File sourceFileOrDirectory, boolean unpackFiles,
                                                File targetDirectory, Set<File> result, boolean copyFiles) {
        if (sourceFileOrDirectory.isFile()) {
            unzipAndCopyFile(sourceFileOrDirectory, unpackFiles, targetDirectory, result, copyFiles);
        } else {
            File[] files = sourceFileOrDirectory.listFiles();
            if (files == null) {
                return;
            }
            File innerTargetDirectory = new File(targetDirectory, sourceFileOrDirectory.getName());
            createTargetDirectoryIfNotExists(innerTargetDirectory);
            for (File file : files) {
                copyAndCollectFilesRecursively(file, unpackFiles, innerTargetDirectory, result, copyFiles);
            }
        }
    }

    private void unzipAndCopyFile(File fileOrDirectory, boolean unpackFiles, File targetDirectory, Set<File> result,
                                  boolean copyFiles) {
        if (fileOrDirectory.getName().endsWith(".zip") && unpackFiles) {
            List<File> files = unzipIfNotExists(fileOrDirectory, targetDirectory);
            result.addAll(files);
        } else {
            if (copyFiles) {
                File file = copyFile(fileOrDirectory, targetDirectory);
                result.add(file);
            } else {
                result.add(fileOrDirectory);
            }
        }
    }

    private File copyFile(File file, File targetDirectory) {
        try {
            createTargetDirectoryIfNotExists(targetDirectory);
            File copied = new File(targetDirectory, file.getName());
            try (
                InputStream in = new BufferedInputStream(
                    new FileInputStream(file));
                OutputStream out = new BufferedOutputStream(
                    new FileOutputStream(copied))) {

                byte[] buffer = new byte[1024];
                int lengthRead;
                while ((lengthRead = in.read(buffer)) > 0) {
                    out.write(buffer, 0, lengthRead);
                }
                out.flush();
            }
            return copied;
        } catch (Exception e) {
            throw new CombinerRuntimeException("Cannot copy file '" +
                file.getAbsolutePath() + "' to the directory '" +
                targetDirectory.getAbsolutePath() + "'", e);
        }
    }

    /**
     * Call the {@link #collectFiles(Set, boolean, File)} method with a single item in the first argument.
     *
     * @param logDirectoryOrFile see the {@link #collectFiles(Set, boolean, File)} method description.
     * @param unpackFiles         see the {@link #collectFiles(Set, boolean, File)} method description.
     * @param targetDirectory    see the {@link #collectFiles(Set, boolean, File)} method description.
     * @return See the {@link #collectFiles(Set, boolean, File)} method description.
     */
    public Set<File> collectFiles(File logDirectoryOrFile, boolean unpackFiles, File targetDirectory) {
        Set<File> files = new HashSet<>(Collections.singletonList(logDirectoryOrFile));
        return collectFiles(files, unpackFiles, targetDirectory);
    }

    /**
     * Call the {@link #collectFiles(Set, boolean, File)} method with a system temporary directory as
     * the third argument (File).
     *
     * @param logDirectoriesOrFiles see the {@link #collectFiles(Set, boolean, File)} method description.
     * @param unpackFiles            see the {@link #collectFiles(Set, boolean, File)} method description.
     * @return See the {@link #collectFiles(Set, boolean, File)} method description.
     */
    public Set<File> collectFiles(Set<File> logDirectoriesOrFiles, boolean unpackFiles) {
        try {
            Path tempPath = Files.createTempDirectory(ReaderService.COMBINER_CORE_MODULE_NAME);
            File tempDirectory = tempPath.toFile();
            tempDirectory.deleteOnExit();
            return collectFiles(logDirectoriesOrFiles, unpackFiles, tempDirectory);
        } catch (Exception e) {
            throw new CombinerRuntimeException("Cannot collect files.", e);
        }
    }

    /**
     * Call the {@link #collectFiles(Set, boolean)} method.
     *
     * @param logDirectoryOrFile see the {@link #collectFiles(Set, boolean)} method description.
     * @param unpackFiles         see the {@link #collectFiles(Set, boolean)} method description.
     * @return See the {@link #collectFiles(Set, boolean)} method description.
     */
    public Set<File> collectFiles(File logDirectoryOrFile, boolean unpackFiles) {
        Set<File> files = new HashSet<>(Collections.singletonList(logDirectoryOrFile));
        return collectFiles(files, unpackFiles);
    }

    /**
     * Call the {@link #collectFiles(Set, boolean)} method with the second argument 'false'.
     *
     * @param logDirectoriesOrFiles see the {@link #collectFiles(Set, boolean)} method description.
     * @return See the {@link #collectFiles(Set, boolean)} method description.
     */
    public Set<File> collectFiles(Set<File> logDirectoriesOrFiles) {
        return collectFiles(logDirectoriesOrFiles, false);
    }

    /**
     * Call the {@link #collectFiles(Set, boolean)} method with 'false' in the second argument.
     *
     * @param logDirectoryOrFile see the {@link #collectFiles(Set)} method description.
     * @return See the {@link #collectFiles(Set)} method description.
     */
    public Set<File> collectFiles(File logDirectoryOrFile) {
        Set<File> files = new HashSet<>(Collections.singletonList(logDirectoryOrFile));
        return collectFiles(files, false);
    }
}

