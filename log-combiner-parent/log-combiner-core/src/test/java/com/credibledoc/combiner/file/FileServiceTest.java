package com.credibledoc.combiner.file;

import org.apache.commons.compress.utils.IOUtils;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FileServiceTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    
    @After
    public void after() {
        temporaryFolder.delete();
    }

    @Test
    public void collectSingleFile() {
        FileService fileService = FileService.getInstance();
        File logDirectoryOrFile = new File("src/test/resources/files/singleFile/singleFile.txt");
        assertTrue(logDirectoryOrFile.exists());

        Set<File> result = fileService.collectFiles(logDirectoryOrFile);
        assertEquals(1, result.size());
    }

    @Test
    public void collectZipFile() {
        FileService fileService = FileService.getInstance();
        File singleZipFile = new File("target/test-classes/files/singleZip/singleFile.zip");
        assertTrue(singleZipFile.exists());

        Set<File> result = fileService.collectFiles(singleZipFile, true);
        assertEquals(1, result.size());
        assertEquals("singleFile.txt", result.iterator().next().getName());
    }

    @Test
    public void collectMultipleFilesInSingleDirectoryDecompress() {
        FileService fileService = FileService.getInstance();
        File multipleFiles = new File("src/test/resources/files/multipleFiles");
        assertTrue(multipleFiles.exists());

        Set<File> result = fileService.collectFiles(multipleFiles, true);
        assertEquals(2, result.size());
        assertTrue(result.iterator().next().getName().endsWith(".txt"));
    }

    @Test
    public void collectMultipleFilesInSingleDirectory() {
        FileService fileService = FileService.getInstance();
        File multipleFiles = new File("src/test/resources/files/multipleFiles");
        assertTrue(multipleFiles.exists());

        Set<File> result = fileService.collectFiles(multipleFiles, false);
        assertEquals(2, result.size());
        assertTrue(result.iterator().next().getName().endsWith(".txt"));
    }

    @Test
    public void collectMultipleZipInSingleDirectoryDecompress() {
        FileService fileService = FileService.getInstance();
        File multipleFiles = new File("src/test/resources/files/multipleZip");
        assertTrue(multipleFiles.exists());

        Set<File> result = fileService.collectFiles(multipleFiles, true);
        for (File file : result) {
            assertTrue(file.getName().endsWith(".txt"));
        }
        assertEquals(5, result.size());
    }

    @Test
    public void collectMultipleZipInSingleDirectory() {
        FileService fileService = FileService.getInstance();
        File multipleFiles = new File("src/test/resources/files/multipleZip");
        assertTrue(multipleFiles.exists());

        Set<File> result = fileService.collectFiles(multipleFiles, false);
        assertEquals(5, result.size());
        for (File file : result) {
            String name = file.getName();
            assertTrue(name.endsWith(".zip") || name.endsWith(".7z") || name.endsWith(".tar"));
        }
    }

    @Test
    public void collectMultipleZipInSingleDirectoryCopyFiles() {
        FileService fileService = FileService.getInstance();
        File multipleFiles = new File("src/test/resources/files/multipleZip");
        assertTrue(multipleFiles.exists());

        File newFolder = new File(temporaryFolder.getRoot(), "newFolder" + System.currentTimeMillis());
        Set<File> result = fileService.collectFiles(multipleFiles, true, newFolder);
        assertEquals(5, result.size());
        assertTrue(result.iterator().next().getName().endsWith(".txt"));
        File decompressedFile = new File(newFolder + "/" + "multipleZip/01.txt");
        assertTrue(decompressedFile.exists());
    }

    @Test
    public void collectMultipleFilesInMultipleDirectoriesCopyFiles() {
        FileService fileService = FileService.getInstance();

        File multipleFiles = new File("src/test/resources/files/multipleFiles");
        assertTrue(multipleFiles.exists());

        File multipleZip = new File("src/test/resources/files/multipleZip");
        assertTrue(multipleZip.exists());

        File singleTxtFile = new File("src/test/resources/files/singleFile/singleFile.txt");
        assertTrue(singleTxtFile.exists());

        File singleZipFile = new File("target/test-classes/files/singleZip/singleFile.zip");
        assertTrue(singleZipFile.exists());

        Set<File> source = new HashSet<>(Arrays.asList(multipleFiles, multipleZip, singleTxtFile, singleZipFile));

        File tempFolder = new File(temporaryFolder.getRoot(), "newFolder_001" + System.currentTimeMillis());
        Set<File> result = fileService.collectFiles(source, false, tempFolder);
        assertEquals(9, result.size());

        File[] firstResult = tempFolder.listFiles();
        assertNotNull(firstResult);
        assertEquals(4, firstResult.length);
        
        Set<File> secondSource = new HashSet<>(Arrays.asList(firstResult));
        Set<File> secondResult = fileService.collectFiles(secondSource, true, null);
        
        assertEquals(8, secondResult.size());
    }

    @Test
    public void collectDirectoryTreeToSameDirectories() {
        FileService fileService = FileService.getInstance();

        File multipleFiles = new File("src/test/resources/files/tree");
        assertTrue(multipleFiles.exists());

        File tempFolder = new File(temporaryFolder.getRoot(), "collectDirectoryTree");

        // copy
        Set<File> result = fileService.collectFiles(multipleFiles, false, tempFolder);
        assertEquals(6, result.size());
        assertNotNull(tempFolder.listFiles());
        assertEquals(1, tempFolder.listFiles().length);
        File treeFolder = new File(tempFolder, "tree");
        assertTrue(treeFolder.exists());
        assertNotNull(treeFolder.listFiles());
        assertEquals(2, treeFolder.listFiles().length);
        
        File dir01 = new File(treeFolder, "dir_01");
        assertTrue(dir01.exists());
        assertNotNull(dir01.listFiles());
        assertEquals(3, dir01.listFiles().length);

        File dir01a = new File(dir01, "dir_01_a");
        assertTrue(dir01a.exists());
        assertNotNull(dir01a.listFiles());
        assertEquals(2, dir01a.listFiles().length);

        File dir02 = new File(treeFolder, "dir_02");
        assertTrue(dir02.exists());
        assertNotNull(dir02.listFiles());
        assertEquals(2, dir02.listFiles().length);
        
        // unzip
        Set<File> secondResult = fileService.collectFiles(tempFolder, true, null);
        
        assertEquals(6, secondResult.size());
        assertNotNull(tempFolder.listFiles());
        assertEquals(1, tempFolder.listFiles().length);
        treeFolder = new File(tempFolder, "tree");
        assertTrue(treeFolder.exists());
        assertNotNull(treeFolder.listFiles());
        assertEquals(2, treeFolder.listFiles().length);
        
        dir01 = new File(treeFolder, "dir_01");
        assertTrue(dir01.exists());
        assertNotNull(dir01.listFiles());
        assertEquals(4, dir01.listFiles().length);

        dir01a = new File(dir01, "dir_01_a");
        assertTrue(dir01a.exists());
        assertNotNull(dir01a.listFiles());
        assertEquals(3, dir01a.listFiles().length);

        dir02 = new File(treeFolder, "dir_02");
        assertTrue(dir02.exists());
        assertNotNull(dir02.listFiles());
        assertEquals(4, dir02.listFiles().length);
    }

    @Test
    public void testWrongExtension() throws IOException {
        FileService fileService = FileService.getInstance();
        File multipleFiles = new File("src/test/resources/files/wrong");
        assertTrue(multipleFiles.exists());

        File newFolder = new File(temporaryFolder.getRoot(), "newFolder" + System.currentTimeMillis());
        Set<File> result = fileService.collectFiles(multipleFiles, true, newFolder);
        assertEquals(1, result.size());
        File file = result.iterator().next();
        assertTrue(file.getName().endsWith(".dump"));
        byte[] bytes = IOUtils.toByteArray(new FileInputStream(file.getAbsolutePath()));
        String text = new String(bytes, StandardCharsets.UTF_8);
        assertEquals("content", text);
    }

    @Test
    public void testCollectFiles() {
        FileService fileService = FileService.getInstance();
        File multipleFiles = new File("src/test/resources/files/multipleZip");
        Set<File> set = new HashSet<>();
        File[] files = multipleFiles.listFiles();
        assertNotNull(files);
        Collections.addAll(set, files);
        assertEquals(5, set.size());

        Set<File> result = fileService.collectFiles(set);
        
        assertEquals(5, result.size());
    }

    @Test
    public void collectGzFile() throws IOException {
        FileService fileService = FileService.getInstance();
        File compressedFile = new File("target/test-classes/files/gz/file.txt.gz");
        assertTrue(compressedFile.exists());

        Set<File> result = fileService.collectFiles(compressedFile, true);
        assertEquals(1, result.size());
        File decompressedFile = result.iterator().next();
        assertEquals("file.txt", decompressedFile.getName());
        byte[] bytes = IOUtils.toByteArray(new FileInputStream(decompressedFile.getAbsolutePath()));
        String text = new String(bytes, StandardCharsets.UTF_8);
        assertEquals("content", text);
    }

    @Test
    public void findLineEndingWindowsIfExists() {
        File windowsFile = new File("target/test-classes/line-ending/windows.txt");
        assertTrue(windowsFile.exists());
        assertEquals(FileService.WINDOWS_LINE_ENDING, FileService.findLineEndingIfExists(windowsFile));
    }

    @Test
    public void findLineEndingMacIfExists() {
        File macFile = new File("target/test-classes/line-ending/mac.txt");
        assertTrue(macFile.exists());
        assertEquals(FileService.MAC_LINE_ENDING, FileService.findLineEndingIfExists(macFile));
    }

    @Test
    public void findLineEndingLinuxIfExists() {
        File linuxFile = new File("target/test-classes/line-ending/linux.txt");
        assertTrue(linuxFile.exists());
        assertEquals(FileService.LINUX_LINE_ENDING, FileService.findLineEndingIfExists(linuxFile));
    }

    @Test
    public void findLineEndingLinux() {
        File linuxFile = new File("target/test-classes/line-ending/linux.txt");
        assertTrue(linuxFile.exists());
        assertEquals(FileService.LINUX_LINE_ENDING, FileService.findLineEnding(linuxFile));
    }

    @Test
    public void findLineEndingEmpty() {
        File emptyFile = new File("target/test-classes/line-ending/empty.txt");
        assertTrue(emptyFile.exists());
        assertEquals(System.lineSeparator(), FileService.findLineEnding(emptyFile));
    }

    @Test
    public void findLineEndingWithout() {
        File emptyFile = new File("target/test-classes/line-ending/without.txt");
        assertTrue(emptyFile.exists());
        assertEquals(System.lineSeparator(), FileService.findLineEnding(emptyFile));
    }

    @Test
    public void findLineEndingNull() {
        File file = null;
        assertEquals(System.lineSeparator(),  FileService.findLineEnding(file));
    }

    @Test
    public void findLineEndingNonExisting() {
        File file = new File("non-existing-path");
        assertEquals(System.lineSeparator(),  FileService.findLineEnding(file));
    }

    @Test
    public void findLineEnding() {
        assertEquals(FileService.WINDOWS_LINE_ENDING,  FileService.findLineEnding(FileService.WINDOWS_LINE_ENDING));
    }
}
