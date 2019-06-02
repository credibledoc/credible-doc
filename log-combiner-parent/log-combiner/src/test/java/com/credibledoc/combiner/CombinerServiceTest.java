package com.credibledoc.combiner;

import com.credibledoc.combiner.config.Config;
import com.credibledoc.combiner.config.ConfigService;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.log.reader.ReaderService;
import com.credibledoc.combiner.node.file.NodeFileService;
import com.credibledoc.combiner.state.FilesMergerState;
import com.credibledoc.combiner.tactic.TacticService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class CombinerServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(CombinerServiceTest.class);

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * Combine log lines from multiple files, order these lines by date and print out to the {@link #logger}.
     */
    @Test
    public void testPrint() {
        File configFile = new File("src/test/resources/test-configuration/log-combiner.properties");
        Config config = ConfigService.getInstance().loadConfig(configFile.getAbsolutePath());
        assertNotNull(config);

        File logDirectory = new File("src/test/resources/test-log-files");
        assertTrue(logDirectory.exists());

        CombinerService combinerService = CombinerService.getInstance();
        combinerService.prepareReader(logDirectory, config);

        FilesMergerState filesMergerState = new FilesMergerState();
        NodeFileService nodeFileService = NodeFileService.getInstance();
        filesMergerState.setNodeFiles(nodeFileService.getNodeFiles());
        ReaderService readerService = ReaderService.getInstance();
        readerService.prepareBufferedReaders(TacticService.getInstance().getTactics());
        int currentLineNumber = 0;
        String line = readerService.readLineFromReaders(filesMergerState);
        LogBufferedReader logBufferedReader = readerService.getCurrentReader(filesMergerState);
        while (line != null) {
            List<String> multiline = readerService.readMultiline(line, logBufferedReader);

            for (String nextLine : multiline) {
                currentLineNumber++;
                logger.debug("{} lines processed. NextLine: {}", currentLineNumber, nextLine);
            }

            line = readerService.readLineFromReaders(filesMergerState);
            logBufferedReader = readerService.getCurrentReader(filesMergerState);
        }
        assertEquals(17, currentLineNumber);
    }

    /**
     * Combine log lines from multiple files, order these lines by date and print out to a new file.
     */
    @Test
    public void testCombine() throws IOException {
        File configFile = new File("src/test/resources/test-configuration/log-combiner.properties");
        Config config = ConfigService.getInstance().loadConfig(configFile.getAbsolutePath());
        assertNotNull(config);

        File logDirectory = new File("src/test/resources/test-log-files");
        assertTrue(logDirectory.exists());

        CombinerService combinerService = CombinerService.getInstance();
        combinerService.prepareReader(logDirectory, config);

        File targetFolder = temporaryFolder.newFolder("generated");
        File targetFile = combinerService.prepareTargetFile(targetFolder, config.getTargetFileName());

        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile))) {
            ReaderService readerService = ReaderService.getInstance();
            readerService.prepareBufferedReaders(TacticService.getInstance().getTactics());

            FilesMergerState filesMergerState = new FilesMergerState();
            NodeFileService nodeFileService = NodeFileService.getInstance();
            filesMergerState.setNodeFiles(nodeFileService.getNodeFiles());

            combinerService.combine(outputStream, filesMergerState);
        }
        File exemplarFile = new File("src/test/resources/test-log-files_generated/combined.txt");
        assertTrue(exemplarFile.exists());

        assertTrue(verifyFilesAreEqual(exemplarFile, targetFile));
    }

    private boolean verifyFilesAreEqual(File leftFile, File rightFile) throws IOException {
        if (leftFile.isFile() && rightFile.isFile()) {
            byte[] leftBytes = Files.readAllBytes(leftFile.toPath());
            byte[] rightBytes = Files.readAllBytes(rightFile.toPath());
            if (!Arrays.equals(leftBytes, rightBytes)) {
                logger.info("leftBytes.size(): {}', rightBytes.size(): '{}'", leftBytes.length, rightBytes.length);
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

}
