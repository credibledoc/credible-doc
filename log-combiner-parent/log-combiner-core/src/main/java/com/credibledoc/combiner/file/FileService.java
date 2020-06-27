package com.credibledoc.combiner.file;

import com.credibledoc.combiner.context.CombinerContext;
import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.log.buffered.LogFileReader;
import com.credibledoc.combiner.log.reader.ReaderService;
import com.credibledoc.combiner.tactic.Tactic;
import com.credibledoc.combiner.tactic.TacticService;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.utils.IOUtils;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

/**
 * This singleton helps to collect log files and contains some util methods.
 *
 * @author Kyrylo Semenko
 */
public class FileService {
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    private static final int MAX_FILE_NAME_LENGTH_250 = 250;
    private static final String SEVEN_ZIP_7Z = ".7z";
    private static final String GZ = ".gz";
    
    private static final Set<String> extensions;
    
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
    
    static {
        extensions = new HashSet<>();
        extensions.add(".zip");
        extensions.add(".tar");
        extensions.add(".ar");
        extensions.add(".arj");
        extensions.add(".cpio");
        extensions.add(".dump");
    }

    /**
     * Recognize which {@link Tactic} this file belongs to.
     * @param file the log file
     * @param combinerContext the current state
     * @return {@link Tactic} or throw the new {@link CombinerRuntimeException} if the file not recognized
     */
    public Tactic findTactic(File file, CombinerContext combinerContext) {
        TacticService tacticService = TacticService.getInstance();
        try (LogBufferedReader logBufferedReader = new LogBufferedReader(new LogFileReader(file))) {
            String line = logBufferedReader.readLine();
            while (line != null) {
                Tactic tactic = tacticService.findTactic(line, logBufferedReader, combinerContext);
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
     * If the second argument contains un7zipped first argument, do not un7zip it.
     * Else un7zip it and return a file from this 7zipFile. So existing files will NOT be overwritten.
     *
     * @param zipFile         7zipped content for decompression.
     * @param targetDirectory where the content will bew un7zipped. If 'null', the parent directory of a 7z file
     *                        will be used and the packed content will be decompressed to the 7zipFile parent directory.
     * @return Unzipped files or found files from the targetDirectory.
     */
    public List<File> un7zipIfNotExists(File zipFile, File targetDirectory) {
        List<File> result = new ArrayList<>();
        String targetPath = targetDirectory.getAbsolutePath() + File.separator;
        try (SevenZFile sevenZFile = new SevenZFile(zipFile)) {
            SevenZArchiveEntry entry;
            while ((entry = sevenZFile.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    File dir = new File(targetPath + entry.getName());
                    if (!dir.exists()) {
                        logger.trace("Creating directory: '{}'", dir.getAbsolutePath());
                        Files.createDirectories(dir.toPath());
                    }
                } else {
                    byte[] content = new byte[(int) entry.getSize()];
                    sevenZFile.read(content);
                    File nextFile = new File(targetPath + entry.getName());
                    if (!nextFile.exists()) {
                        Files.write(nextFile.toPath(), content);
                        logger.trace("File unzipped: {}", nextFile.getAbsolutePath());
                    } else {
                        logger.trace("File already exists: {}", nextFile.getAbsolutePath());
                    }
                    result.add(nextFile);
                }
            }
        } catch (IOException e) {
            throw new CombinerRuntimeException("Cannot un7zip " + zipFile.getAbsolutePath(), e);
        }
        return result;
    }

    /**
     * If the second argument contains decompressed first argument, do not decompress it.
     * Else uncompress it and return a file from this gz file. So existing files will NOT be overwritten.
     *
     * @param gzFile         the file for decompression.
     * @param targetDirectory where the content will be decompressed. If 'null', the parent directory of a gz file
     *                        will be used and the compressed content will be decompressed to the gz parent directory.
     * @return Uncompressed gz file or existing file from the targetDirectory.
     */
    public List<File> decompressGzIfNotExists(File gzFile, File targetDirectory) {
        List<File> result = new ArrayList<>();
        String targetPath = targetDirectory.getAbsolutePath() + File.separator;
        try (FileInputStream fis = new FileInputStream(gzFile); GZIPInputStream gzipInputStream = new GZIPInputStream(fis)) {
            String oldFileName = gzFile.getName();
            String oldFileNameLower = gzFile.getName().toLowerCase();
            int beginIndex = oldFileNameLower.lastIndexOf(GZ);
            String newFileName = oldFileName.substring(0, beginIndex);
            File decompressedFile = new File(targetPath + newFileName);
            if (!decompressedFile.exists()) {

                copyBytes(gzipInputStream, decompressedFile);

                logger.trace("File .gz decompressed: {}", decompressedFile.getAbsolutePath());
            } else {
                logger.trace("Decompressed file already exists: {}", decompressedFile.getAbsolutePath());
            }
            result.add(decompressedFile);
        } catch (IOException e) {
            throw new CombinerRuntimeException("Cannot un7zip " + gzFile.getAbsolutePath(), e);
        }
        return result;
    }

    public void copyBytes(GZIPInputStream gzipInputStream, File targetFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            IOUtils.copy(gzipInputStream, fos);
        }
    }

    /**
     * If the second argument contains decompressed first argument, do not decompress it.
     * Else decompress it and return a file from this compressedFile. So existing files will NOT be overwritten.
     *
     * @param compressedFile         compressed content.
     * @param targetDirectory where the content will be decompressed. If 'null', the parent directory of a compressed file
     *                        will be used and the packed content will be decompressed to the compressedFile parent directory.
     * @return Decompressed files or found files from the targetDirectory.
     */
    public List<File> decompressIfNotExists(File compressedFile, File targetDirectory) {
        List<File> result = new ArrayList<>();
        String targetPath = targetDirectory.getAbsolutePath() + File.separator;
        try (ArchiveInputStream archiveInputStream =
                 new ArchiveStreamFactory().createArchiveInputStream(
                 new BufferedInputStream(new FileInputStream(compressedFile)))) {

            ArchiveEntry entry;
            while ((entry = archiveInputStream.getNextEntry()) != null) {
                if (!archiveInputStream.canReadEntryData(entry)) {
                    throw new CombinerRuntimeException("Cannot decompress entry '" + entry.getName() +
                        "' from file '" + compressedFile.getAbsolutePath() + "'");
                }
                if (entry.isDirectory()) {
                    File dir = new File(targetPath + entry.getName());
                    mkdirsIfNotExists(dir);
                } else {
                    File nextFile = new File(targetPath + entry.getName());
                    File dir = nextFile.getParentFile();
                    mkdirsIfNotExists(dir);
                    if (!nextFile.exists()) {
                        try (OutputStream o = Files.newOutputStream(nextFile.toPath())) {
                            IOUtils.copy(archiveInputStream, o);
                        }
                        logger.trace("File unzipped: {}", nextFile.getAbsolutePath());
                    } else {
                        logger.trace("File already exists: {}", nextFile.getAbsolutePath());
                    }
                    result.add(nextFile);
                }
            }
        } catch (Exception e) {
            throw new CombinerRuntimeException("Cannot decompress file " + compressedFile.getAbsolutePath(), e);
        }
        return result;
    }

    public void mkdirsIfNotExists(File dir) throws IOException {
        if (!dir.exists()) {
            logger.trace("Directory will be created: '{}'", dir.getAbsolutePath());
            Files.createDirectories(dir.toPath());
        }
    }

    /**
     * Search for files recursively in the source directories defined in the first argument.
     * <p>
     * All found files will be copied to the targetDirectory. Files with the same names will be rewritten.
     * If the targetDirectory is the same as the source directory, the files will not be copied to the target directory.
     *
     * @param logDirectoriesOrFiles one or more directories with log files generated by some applications. If this
     *                              argument containing multiple directories, a new sub-directory will be created
     *                              for each source directory.
     * @param decompressFiles           if 'true', unzip files to the targetDirectory. Accepted formats are zip, 7z.
     * @param targetDirectory       the target directory where all files will be copied.
     *                              It can be 'null'. In this case unzipped files
     *                              will be placed to the source directory next to source zip files.
     * @return Set of copied files.
     */
    public Set<File> collectFiles(Set<File> logDirectoriesOrFiles, boolean decompressFiles, File targetDirectory) {
        createTargetDirectoryIfNotExists(targetDirectory);
        Set<File> result = new TreeSet<>();
        for (File file : logDirectoriesOrFiles) {
            if (targetDirectory == null) {
                copyAndCollectFilesRecursively(file, decompressFiles, file.getParentFile(), result, false);
            } else {
                copyAndCollectFilesRecursively(file, decompressFiles, targetDirectory, result, true);
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

    private void copyAndCollectFilesRecursively(File sourceFileOrDirectory, boolean decompressFiles,
                                                File targetDirectory, Set<File> result, boolean copyFiles) {
        if (sourceFileOrDirectory.isFile()) {
            decompressAndCopyFile(sourceFileOrDirectory, decompressFiles, targetDirectory, result, copyFiles);
        } else {
            File[] files = sourceFileOrDirectory.listFiles();
            if (files == null) {
                return;
            }
            File innerTargetDirectory = new File(targetDirectory, sourceFileOrDirectory.getName());
            createTargetDirectoryIfNotExists(innerTargetDirectory);
            for (File file : files) {
                copyAndCollectFilesRecursively(file, decompressFiles, innerTargetDirectory, result, copyFiles);
            }
        }
    }

    private void decompressAndCopyFile(File fileOrDirectory, boolean decompressFiles, File targetDirectory, Set<File> result,
                                       boolean copyFiles) {
        boolean isFile = fileOrDirectory.isFile();
        boolean shouldBeDecompressed = false;
        if (isFile && decompressFiles) {
            shouldBeDecompressed = canBeDecompressed(fileOrDirectory.getName());
        }
        if (shouldBeDecompressed) {
            try {
                List<File> decompressed = decompress(fileOrDirectory, targetDirectory);
                result.addAll(decompressed);
                for (File file : decompressed) {
                    if (canBeDecompressed(file.getName())) {
                        // recursively
                        decompressAndCopyFile(file, true, file.getParentFile(), result, copyFiles);
                        result.remove(file);
                    }
                }
            } catch (Exception e) {
                logger.error("Cannot decompress file '{}'", fileOrDirectory.getAbsolutePath(), e);
                result.add(fileOrDirectory);
            }
            return;
        }
        
        if (copyFiles) {
            File file = copyFile(fileOrDirectory, targetDirectory);
            result.add(file);
        } else {
            result.add(fileOrDirectory);
        }
    }

    private List<File> decompress(File compressed, File targetDirectory) {
        String lowerCase = compressed.getName().toLowerCase();
        if (lowerCase.endsWith(SEVEN_ZIP_7Z)) {
            return un7zipIfNotExists(compressed, targetDirectory);
        }
        if (lowerCase.endsWith(GZ)) {
            return decompressGzIfNotExists(compressed, targetDirectory);
        }
        for (String extension : extensions) {
            if (lowerCase.endsWith(extension)) {
                return decompressIfNotExists(compressed, targetDirectory);
            }
        }
        throw new CombinerRuntimeException("Cannot decompress file. Unknown file extension. " +
            "File: " + compressed.getAbsolutePath());
    }

    private boolean canBeDecompressed(String name) {
        String lowerCase = name.toLowerCase();
        for (String extension : extensions) {
            if (lowerCase.endsWith(extension)) {
                return true;
            }
        }
        return lowerCase.endsWith(SEVEN_ZIP_7Z) || lowerCase.endsWith(GZ);
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
     * @param decompressFiles         see the {@link #collectFiles(Set, boolean, File)} method description.
     * @param targetDirectory    see the {@link #collectFiles(Set, boolean, File)} method description.
     * @return See the {@link #collectFiles(Set, boolean, File)} method description.
     */
    public Set<File> collectFiles(File logDirectoryOrFile, boolean decompressFiles, File targetDirectory) {
        Set<File> files = new TreeSet<>(Collections.singletonList(logDirectoryOrFile));
        return collectFiles(files, decompressFiles, targetDirectory);
    }

    /**
     * Call the {@link #collectFiles(Set, boolean, File)} method with a system temporary directory as
     * the third argument (File).
     *
     * @param logDirectoriesOrFiles see the {@link #collectFiles(Set, boolean, File)} method description.
     * @param decompressFiles            see the {@link #collectFiles(Set, boolean, File)} method description.
     * @return See the {@link #collectFiles(Set, boolean, File)} method description.
     */
    public Set<File> collectFiles(Set<File> logDirectoriesOrFiles, boolean decompressFiles) {
        try {
            Path tempPath = Files.createTempDirectory(ReaderService.COMBINER_CORE_MODULE_NAME);
            File tempDirectory = tempPath.toFile();
            tempDirectory.deleteOnExit();
            return collectFiles(logDirectoriesOrFiles, decompressFiles, tempDirectory);
        } catch (Exception e) {
            throw new CombinerRuntimeException("Cannot collect files.", e);
        }
    }

    /**
     * Call the {@link #collectFiles(Set, boolean)} method.
     *
     * @param logDirectoryOrFile see the {@link #collectFiles(Set, boolean)} method description.
     * @param decompressFiles         see the {@link #collectFiles(Set, boolean)} method description.
     * @return See the {@link #collectFiles(Set, boolean)} method description.
     */
    public Set<File> collectFiles(File logDirectoryOrFile, boolean decompressFiles) {
        Set<File> files = new TreeSet<>(Collections.singletonList(logDirectoryOrFile));
        return collectFiles(files, decompressFiles);
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
        Set<File> files = new TreeSet<>(Collections.singletonList(logDirectoryOrFile));
        return collectFiles(files, false);
    }
}

