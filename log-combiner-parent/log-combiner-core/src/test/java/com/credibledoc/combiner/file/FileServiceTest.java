package com.credibledoc.combiner.file;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.*;

public class FileServiceTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void collectEmpty() {
        FileService fileService = FileService.getInstance();
        Set<File> result = fileService.collectFiles(Collections.<File>emptySet());
        assertEquals(0, result.size());

        File logDirectoryOrFile = new File("src/test/resources/files/empty");
        assertTrue(logDirectoryOrFile.exists());
        Set<File> result2 = fileService.collectFiles(logDirectoryOrFile);
        assertEquals(0, result2.size());
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
        File singleZipFile = new File("src/test/resources/files/singleZip/singleFile.zip");
        assertTrue(singleZipFile.exists());

        Set<File> result = fileService.collectFiles(singleZipFile, true);
        assertEquals(1, result.size());
        assertEquals("singleFile.txt", result.iterator().next().getName());
    }

    @Test
    public void collectMultipleFilesInSingleDirectoryUnpack() {
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
    public void collectMultipleZipInSingleDirectoryUnpack() {
        FileService fileService = FileService.getInstance();
        File multipleFiles = new File("src/test/resources/files/multipleZip");
        assertTrue(multipleFiles.exists());

        Set<File> result = fileService.collectFiles(multipleFiles, true);
        assertEquals(2, result.size());
        assertTrue(result.iterator().next().getName().endsWith(".txt"));
    }

    @Test
    public void collectMultipleZipInSingleDirectory() {
        FileService fileService = FileService.getInstance();
        File multipleFiles = new File("src/test/resources/files/multipleZip");
        assertTrue(multipleFiles.exists());

        Set<File> result = fileService.collectFiles(multipleFiles, false);
        assertEquals(2, result.size());
        assertTrue(result.iterator().next().getName().endsWith(".zip"));
    }

    @Test
    public void collectMultipleZipInSingleDirectoryCopyFiles() {
        FileService fileService = FileService.getInstance();
        File multipleFiles = new File("src/test/resources/files/multipleZip");
        assertTrue(multipleFiles.exists());

        File newFolder = new File(folder.getRoot(), "newFolder" + System.currentTimeMillis());
        Set<File> result = fileService.collectFiles(multipleFiles, true, newFolder);
        assertEquals(2, result.size());
        assertTrue(result.iterator().next().getName().endsWith(".txt"));
        String tempFolderPath = newFolder.getAbsolutePath();
        String generatedFolderPath = result.iterator().next().getParentFile().getAbsolutePath();
        assertEquals(tempFolderPath, generatedFolderPath);
    }

}
