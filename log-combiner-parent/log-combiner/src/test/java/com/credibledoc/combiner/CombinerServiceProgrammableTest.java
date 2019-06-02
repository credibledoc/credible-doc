package com.credibledoc.combiner;

import com.credibledoc.combiner.file.FileService;
import com.credibledoc.combiner.node.file.NodeFileService;
import com.credibledoc.combiner.state.FilesMergerState;
import com.credibledoc.combiner.tactic.Tactic;
import com.credibledoc.combiner.tactic.TacticService;
import com.credibledoc.tactic.FirstApplicationTactic;
import com.credibledoc.tactic.SecondApplicationTactic;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static org.junit.Assert.assertTrue;

public class CombinerServiceProgrammableTest {
    private static final Logger logger = LoggerFactory.getLogger(CombinerServiceProgrammableTest.class);

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * Combine log lines from multiple files, order these lines by date and print out them to a new file.
     */
    @Test
    public void testCombine() throws IOException {
        File logDirectory = new File("src/test/resources/test-log-files");
        assertTrue(logDirectory.exists());

        // Collect log files from all directories recursively
        FileService fileService = FileService.getInstance();
        Set<File> files = fileService.collectFiles(logDirectory);

        // Instantiate parsers for different log formats
        Set<Tactic> tactics = new HashSet<>();
        tactics.add(new FirstApplicationTactic());
        tactics.add(new SecondApplicationTactic());

        TacticService.getInstance().prepareReaders(files, tactics);

        File targetFolder = temporaryFolder.newFolder("generated");
        CombinerService combinerService = CombinerService.getInstance();
        File targetFile = combinerService.prepareTargetFile(targetFolder, "combined-test.txt");

        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile))) {
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
