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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FileWithSourcesTest {
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

        FileWithSources source = new FileWithSources();
        source.getSources().add(logDirectoryOrFile);
        List<FileWithSources> result = fileService.collectFiles(source);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getSources().size());
    }

    @Test
    public void collectDirWithSingleFile() {
        FileService fileService = FileService.getInstance();
        File logDirectoryOrFile = new File("src/test/resources/files/singleFile");
        assertTrue(logDirectoryOrFile.exists());

        FileWithSources source = new FileWithSources();
        source.getSources().add(logDirectoryOrFile);
        List<FileWithSources> result = fileService.collectFiles(source);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getSources().size());
    }

    @Test
    public void collectZipFile() {
        FileService fileService = FileService.getInstance();
        File singleZipFile = new File("target/test-classes/files/singleZip/singleFile.zip");
        assertTrue(singleZipFile.exists());

        FileWithSources source = new FileWithSources();
        source.getSources().add(singleZipFile);
        
        List<FileWithSources> result = fileService.collectFiles(source, true);
        assertEquals(1, result.size());
        assertEquals("singleFile.txt", result.iterator().next().getFile().getName());
        assertEquals(singleZipFile.getAbsolutePath(), result.get(0).getSources().get(0).getAbsolutePath());
        assertEquals(1, result.get(0).getSources().size());
    }

    @Test
    public void collectMultipleFilesInSingleDirectoryDecompress() {
        FileService fileService = FileService.getInstance();
        File multipleFiles = new File("src/test/resources/files/multipleFiles");
        assertTrue(multipleFiles.exists());

        FileWithSources source = new FileWithSources();
        source.getSources().add(multipleFiles);

        List<FileWithSources> result = fileService.collectFiles(source, true);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getFile().getName().endsWith(".txt"));
        for (FileWithSources fileWithSources : result) {
            assertEquals(2, fileWithSources.getSources().size());
            assertEquals(fileWithSources.getFile().getName(), fileWithSources.getSources().get(1).getName());
        }
    }

    @Test
    public void collectMultipleZipInSingleDirectoryDecompress() {
        FileService fileService = FileService.getInstance();
        File multipleFiles = new File("src/test/resources/files/multipleZip");
        assertTrue(multipleFiles.exists());

        FileWithSources source = new FileWithSources();
        source.getSources().add(multipleFiles);

        List<FileWithSources> result = fileService.collectFiles(source, true);
        for (FileWithSources fileWithSources : result) {
            assertTrue(fileWithSources.getFile().getName().endsWith(".txt"));
        }
        assertEquals(5, result.size());
    }

    @Test
    public void collectMultipleZipInSingleDirectory() {
        FileService fileService = FileService.getInstance();
        File multipleFiles = new File("src/test/resources/files/multipleZip");
        assertTrue(multipleFiles.exists());

        FileWithSources source = new FileWithSources();
        source.getSources().add(multipleFiles);

        List<FileWithSources> result = fileService.collectFiles(source, false);
        assertEquals(5, result.size());
        for (FileWithSources fileWithSources : result) {
            String name = fileWithSources.getFile().getName();
            assertTrue(name.endsWith(".zip") || name.endsWith(".7z") || name.endsWith(".tar"));
            assertEquals(2, fileWithSources.getSources().size());
        }
    }

    @Test
    public void collectMultipleZipInSingleDirectoryCopyFiles() {
        FileService fileService = FileService.getInstance();
        File multipleFiles = new File("src/test/resources/files/multipleZip");
        assertTrue(multipleFiles.exists());

        FileWithSources source = new FileWithSources();
        source.getSources().add(multipleFiles);

        File newFolder = new File(temporaryFolder.getRoot(), "newFolder" + System.currentTimeMillis());
        List<FileWithSources> result = fileService.collectFiles(source, true, newFolder);
        assertEquals(5, result.size());
        for (FileWithSources fileWithSources : result) {
            assertTrue(fileWithSources.getFile().getName().endsWith(".txt"));
        }
        
        String firstFileName = result.get(0).getFile().getName();
        assertTrue(firstFileName.endsWith(".txt"));
        File decompressedFile = new File(newFolder + "/multipleZip/" + firstFileName);
        assertTrue(decompressedFile.exists());
    }

    @Test
    public void collectMultipleFilesInMultipleDirectoriesCopyFiles() {
        FileService fileService = FileService.getInstance();

        File multipleFiles = new File("src/test/resources/files/multipleFiles");
        assertTrue(multipleFiles.exists());
        FileWithSources source1 = new FileWithSources();
        source1.getSources().add(multipleFiles);

        File multipleZip = new File("src/test/resources/files/multipleZip");
        assertTrue(multipleZip.exists());
        FileWithSources source2 = new FileWithSources();
        source2.getSources().add(multipleZip);

        File singleTxtFile = new File("src/test/resources/files/singleFile/singleFile.txt");
        assertTrue(singleTxtFile.exists());
        FileWithSources source3 = new FileWithSources();
        source3.getSources().add(singleTxtFile);

        File singleZipFile = new File("target/test-classes/files/singleZip/singleFile.zip");
        assertTrue(singleZipFile.exists());
        FileWithSources source4 = new FileWithSources();
        source4.getSources().add(singleZipFile);

        List<FileWithSources> source = new ArrayList<>(Arrays.asList(source1, source2, source3, source4));

        File tempFolder = new File(temporaryFolder.getRoot(), "newFolder_001" + System.currentTimeMillis());
        List<FileWithSources> result = fileService.collectFiles(source, false, tempFolder);
        assertEquals(9, result.size());

        File[] firstResult = tempFolder.listFiles();
        assertNotNull(firstResult);
        assertEquals(4, firstResult.length);
        
        List<FileWithSources> secondSource = new ArrayList<>();
        for (File file : firstResult) {
            FileWithSources fileWithSources = new FileWithSources();
            fileWithSources.getSources().add(file);
            secondSource.add(fileWithSources);
        }
        List<FileWithSources> secondResult = fileService.collectFiles(secondSource, true, null);
        
        assertEquals(9, secondResult.size());
        for (FileWithSources fileWithSources : secondResult) {
            assertNotNull(fileWithSources.getFile());
        }
    }

    @Test
    public void testWrongExtension() throws IOException {
        FileService fileService = FileService.getInstance();
        File multipleFiles = new File("src/test/resources/files/wrong");
        assertTrue(multipleFiles.exists());

        FileWithSources source = new FileWithSources();
        source.getSources().add(multipleFiles);

        File newFolder = new File(temporaryFolder.getRoot(), "newFolder" + System.currentTimeMillis());
        List<FileWithSources> result = fileService.collectFiles(source, true, newFolder);
        assertEquals(1, result.size());
        File file = result.iterator().next().getFile();
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
        
        List<FileWithSources> sources = new ArrayList<>();
        for (File file : files) {
            FileWithSources fileWithSources = new FileWithSources();
            fileWithSources.getSources().add(file);
            sources.add(fileWithSources);
        }

        List<FileWithSources> result = fileService.collectFiles(sources);
        
        assertEquals(5, result.size());
    }

    @Test
    public void collectGzFile() throws IOException {
        FileService fileService = FileService.getInstance();
        File compressedFile = new File("target/test-classes/files/gz/file.txt.gz");
        assertTrue(compressedFile.exists());

        FileWithSources source = new FileWithSources();
        source.getSources().add(compressedFile);

        List<FileWithSources> result = fileService.collectFiles(source, true);
        assertEquals(1, result.size());
        File decompressedFile = result.iterator().next().getFile();
        assertEquals("file.txt", decompressedFile.getName());
        byte[] bytes = IOUtils.toByteArray(new FileInputStream(decompressedFile.getAbsolutePath()));
        String text = new String(bytes, StandardCharsets.UTF_8);
        assertEquals("content", text);
    }

    @Test
    public void sameName() {
        FileService fileService = FileService.getInstance();
        File dir1 = new File("target/test-classes/files/differentFoldersSameFileName/dir1");
        assertTrue(dir1.exists());

        FileWithSources source1 = new FileWithSources();
        source1.getSources().add(dir1);
        
        File dir2 = new File("target/test-classes/files/differentFoldersSameFileName/dir2");
        assertTrue(dir2.exists());

        FileWithSources source2 = new FileWithSources();
        source2.getSources().add(dir2);

        List<FileWithSources> sources = Arrays.asList(source1, source2);

        List<FileWithSources> result = fileService.collectFiles(sources, true);
        assertEquals(2, result.size());
        for (FileWithSources fileWithSources : result) {
            assertEquals(2, fileWithSources.getSources().size());
        }
    }

    @Test
    public void sameFiles() {
        FileService fileService = FileService.getInstance();
        File dir1 = new File("target/test-classes/files/differentFoldersSameFileName/dir1");
        assertTrue(dir1.exists());

        FileWithSources source1 = new FileWithSources();
        source1.getSources().add(dir1);
        
        File dir2 = new File("target/test-classes/files/differentFoldersSameFileName/dir1");
        FileWithSources source2 = new FileWithSources();
        source2.getSources().add(dir2);

        List<FileWithSources> sources = Arrays.asList(source1, source2);

        List<FileWithSources> result = fileService.collectFiles(sources, true);
        assertEquals(2, result.size());
        for (FileWithSources fileWithSources : result) {
            assertEquals(2, fileWithSources.getSources().size());
        }
    }

}
