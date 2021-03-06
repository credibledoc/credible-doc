package com.credibledoc.combiner;

import com.credibledoc.combiner.config.Config;
import com.credibledoc.combiner.config.ConfigService;
import com.credibledoc.combiner.context.CombinerContext;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.log.reader.ReaderService;
import com.credibledoc.combiner.state.FilesMergerState;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
        Config config = new ConfigService().loadConfig(configFile.getAbsolutePath());
        assertNotNull(config);

        File logDirectory = new File("src/test/resources/test-log-files");
        assertTrue(logDirectory.exists());

        // Contains instances of Tactics, NodeFiles and NodeLogs
        CombinerContext combinerContext = new CombinerContext().init();

        CombinerService combinerService = CombinerService.getInstance();
        combinerService.prepareReader(logDirectory, config, combinerContext);

        FilesMergerState filesMergerState = new FilesMergerState();
        filesMergerState.setNodeFiles(combinerContext.getNodeFileRepository().getNodeFiles());
        ReaderService readerService = ReaderService.getInstance();
        int currentLineNumber = 0;
        String line = readerService.readLineFromReaders(filesMergerState);
        LogBufferedReader logBufferedReader = filesMergerState.getCurrentNodeFile().getLogBufferedReader();
        while (line != null) {
            List<String> multiline = readerService.readMultiline(line, logBufferedReader, combinerContext);

            for (String nextLine : multiline) {
                currentLineNumber++;
                logger.debug("{} lines processed. NextLine: {}", currentLineNumber, nextLine);
            }

            line = readerService.readLineFromReaders(filesMergerState);
            logBufferedReader = filesMergerState.getCurrentNodeFile().getLogBufferedReader();
        }
        assertEquals(17, currentLineNumber);
    }

    /**
     * Combine log lines from multiple files, order these lines by date and print out to a new file.
     */
    @Test
    public void testCombine() throws IOException {
        File configFile = new File("src/test/resources/test-configuration/log-combiner.properties");
        Config config = new ConfigService().loadConfig(configFile.getAbsolutePath());
        assertNotNull(config);

        File logDirectory = new File("src/test/resources/test-log-files");
        assertTrue(logDirectory.exists());

        // Contains instances of Tactics, NodeFiles and NodeLogs
        CombinerContext combinerContext = new CombinerContext().init();
        
        CombinerService combinerService = CombinerService.getInstance();
        combinerService.prepareReader(logDirectory, config, combinerContext);

        File targetFolder = temporaryFolder.newFolder("generated-combine");
        File targetFile = combinerService.prepareTargetFile(targetFolder, config.getTargetFileName());

        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile))) {
            FilesMergerState filesMergerState = new FilesMergerState();
            filesMergerState.setNodeFiles(combinerContext.getNodeFileRepository().getNodeFiles());

            combinerService.combine(outputStream, filesMergerState, combinerContext);
        }
        File exemplarFile = new File("src/test/resources/test-log-files-expected/combined.txt");
        assertTrue(exemplarFile.exists());

        assertTrue(verifyFilesAreEqual(exemplarFile, targetFile));
    }

    private boolean verifyFilesAreEqual(File leftFile, File rightFile) throws IOException {
        if (leftFile.isFile() && rightFile.isFile()) {
            byte[] leftBytes = Files.readAllBytes(leftFile.toPath());
            byte[] rightBytes = Files.readAllBytes(rightFile.toPath());
            if (!Arrays.equals(leftBytes, rightBytes)) {
                logger.error("leftBytes.size(): {}', rightBytes.size(): '{}'", leftBytes.length, rightBytes.length);
                logger.error("Left:\n{}", new String(leftBytes));
                logger.error("Right:\n{}", new String(rightBytes));
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

}
