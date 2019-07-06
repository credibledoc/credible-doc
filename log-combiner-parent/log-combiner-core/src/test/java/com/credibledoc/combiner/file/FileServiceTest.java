package com.credibledoc.combiner.file;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class FileServiceTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    
    @After
    public void after() {
        temporaryFolder.delete();
    }

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
        File singleZipFile = new File("target/test-classes/files/singleZip/singleFile.zip");
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

        File newFolder = new File(temporaryFolder.getRoot(), "newFolder" + System.currentTimeMillis());
        Set<File> result = fileService.collectFiles(multipleFiles, true, newFolder);
        assertEquals(2, result.size());
        assertTrue(result.iterator().next().getName().endsWith(".txt"));
        String newFolderPath = newFolder.getAbsolutePath() + File.separator + multipleFiles.getName();
        String generatedFolderPath = result.iterator().next().getParentFile().getAbsolutePath();
        assertEquals(newFolderPath, generatedFolderPath);
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
        assertEquals(6, result.size());

        File[] firstResult = tempFolder.listFiles();
        assertNotNull(firstResult);
        assertEquals(4, firstResult.length);
        
        Set<File> secondSource = new HashSet<>(Arrays.asList(firstResult));
        Set<File> secondResult = fileService.collectFiles(secondSource, true, null);
        
        assertEquals(5, secondResult.size());
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
    
}
