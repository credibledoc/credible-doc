package com.credibledoc.combiner.file;

import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.log.buffered.LogFileReader;
import com.credibledoc.combiner.log.reader.ReaderService;
import com.credibledoc.combiner.tactic.Tactic;
import com.credibledoc.combiner.tactic.TacticService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This singleton helps to collect log files and contains some util methods.
 *
 * @author Kyrylo Semenko
 */
public class FileService {

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
     * Else unzip it and return a file from this zipFile.
     *
     * @param zipFile         zipped log file. If this zip file contains more then one entry, an exception will be
     *                        thrown.
     * @param targetDirectory where the file will bew unzipped. If 'null', the parent directory of a zip file
     *                        will bew used and the packed file will be unpacked to the zipFile parent directory.
     * @return an unzipped file or found file from files
     */
    public File unzipIfNotExists(File zipFile, File targetDirectory) {
        File target = targetDirectory != null ? targetDirectory : zipFile.getParentFile();
        File[] files = target.listFiles();
        try {
            byte[] buffer = new byte[1024];
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
                ZipEntry zipEntry = zis.getNextEntry();
                if (zis.getNextEntry() != null) {
                    throw new CombinerRuntimeException("Zip file contains more then one zipped files." +
                        " Expected a single zipped file only inside the zip file: " + zipFile.getAbsolutePath());
                }
                if (files != null) {
                    for (File file : files) {
                        if (file.getName().equals(zipEntry.getName())) {
                            return file;
                        }
                    }
                }
                File newFile = newFile(target, zipEntry);
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }

                zis.closeEntry();
                return newFile;
            }
        } catch (Exception e) {
            throw new CombinerRuntimeException("Cannot unzip file: " + zipFile.getAbsolutePath(), e);
        }
    }

    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
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

    private void createTargetDirectoryIfNotExists(File targetDirectory) {
        if (targetDirectory != null && !targetDirectory.exists()) {
            if (targetDirectory.getAbsolutePath().length() > MAX_FILE_NAME_LENGTH_250) {
                throw new CombinerRuntimeException("TargetDirectory name length is greater then " +
                    MAX_FILE_NAME_LENGTH_250 + ". File name: " + targetDirectory.getAbsolutePath());
            }
            boolean created = targetDirectory.mkdirs();
            if (!created) {
                throw new CombinerRuntimeException("Cannot create directory: '" +
                    targetDirectory.getAbsolutePath() + "'");
            }
        }
    }

    private void copyAndCollectFilesRecursively(File fileOrDirectory, boolean unpackFiles,
                                                File targetDirectory, Set<File> result, boolean copyFiles) {
        if (fileOrDirectory.isFile()) {
            unzipAndCopyFile(fileOrDirectory, unpackFiles, targetDirectory, result, copyFiles);
        } else {
            unzipAndCopyFiles(fileOrDirectory, unpackFiles, targetDirectory, result, copyFiles);
        }
    }

    private void unzipAndCopyFiles(File directory, boolean unpackFiles, File targetDirectory, Set<File> result,
                                   boolean copyFiles) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        File innerTargetDirectory = new File(targetDirectory, directory.getName());
        createTargetDirectoryIfNotExists(innerTargetDirectory);
        for (File file : files) {
            if (file.isDirectory()) {
                String name = file.getName();
                innerTargetDirectory = new File(innerTargetDirectory, name);
                createTargetDirectoryIfNotExists(innerTargetDirectory);
            }
            copyAndCollectFilesRecursively(file, unpackFiles, innerTargetDirectory, result, copyFiles);
        }
    }

    private void unzipAndCopyFile(File fileOrDirectory, boolean unpackFiles, File targetDirectory, Set<File> result,
                                  boolean copyFiles) {
        if (fileOrDirectory.getName().endsWith(".zip") && unpackFiles) {
            File file = unzipIfNotExists(fileOrDirectory, targetDirectory);
            result.add(file);
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
                    out.flush();
                }
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
            return collectFiles(logDirectoriesOrFiles, unpackFiles, tempPath.toFile());
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
     * Call the {@link #collectFiles(Set, boolean, File)} method with 'false' in the second argument
     * and the logDirectoryFile as the third argument.
     *
     * @param logDirectoryOrFile see the {@link #collectFiles(Set)} method description.
     * @return See the {@link #collectFiles(Set)} method description.
     */
    public Set<File> collectFiles(File logDirectoryOrFile) {
        Set<File> files = new HashSet<>(Collections.singletonList(logDirectoryOrFile));
        return collectFiles(files, false, logDirectoryOrFile.getParentFile());
    }
}

