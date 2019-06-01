package com.credibledoc.combiner;

import com.credibledoc.combiner.application.ApplicationService;
import com.credibledoc.combiner.application.identifier.ApplicationIdentifier;
import com.credibledoc.combiner.application.identifier.ApplicationIdentifierService;
import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.combiner.file.FileService;
import com.credibledoc.combiner.log.reader.ReaderService;
import com.credibledoc.combiner.node.applicationlog.ApplicationLog;
import com.credibledoc.combiner.node.applicationlog.ApplicationLogService;
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

        FileService fileService = FileService.getInstance();
        Set<File> files = fileService.collectFiles(logDirectory);

        Set<Tactic> tactics = new HashSet<>();
        tactics.add(new FirstApplicationTactic());
        tactics.add(new SecondApplicationTactic());

        prepareReaders(files, tactics);

        File targetFolder = temporaryFolder.newFolder("generated");
        CombinerService combinerService = CombinerService.getInstance();
        File targetFile = combinerService.prepareTargetFile(targetFolder);

        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile))) {
            FilesMergerState filesMergerState = new FilesMergerState();
            NodeFileService nodeFileService = NodeFileService.getInstance();
            filesMergerState.setNodeFiles(nodeFileService.getNodeFiles());

            combinerService.combine(outputStream, filesMergerState);
        }
        File exampleFile = new File("src/test/resources/test-log-files_generated/combined.txt");
        assertTrue(exampleFile.exists());

        assertTrue(verifyFilesAreEqual(exampleFile, targetFile));
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

    private void prepareReaders(Set<File> files, Set<? extends Object> tactics) {
        TacticService tacticService = TacticService.getInstance();
        tacticService.getTactics().addAll((Collection<? extends Tactic>) tactics);

        ApplicationIdentifierService.getInstance().getApplicationIdentifiers().addAll((Collection<? extends ApplicationIdentifier>) tactics);
        ApplicationLogService applicationLogService = ApplicationLogService.getInstance();
        ApplicationService applicationService = ApplicationService.getInstance();

        Map<Tactic, Map<Date, File>> map = new HashMap<>();
        // TODO Kyrylo Semenko - zde je chyba. Dva soubory mohou mit stejny datum.
        for (File file : files) {
            Tactic tactic = FileService.getInstance().findTactic(file);
            if (!map.containsKey(tactic)) {
                map.put(tactic, new TreeMap<Date, File>());
            }

            Date date = FileService.getInstance().findDate(file, tactic);

            if (date == null) {
                throw new CombinerRuntimeException("Cannot find a date in the file: " + file.getAbsolutePath());
            }
            Map<Date, File> dateFileMap = map.get(tactic);
            while (dateFileMap.containsKey(date)) {
                date.setTime(date.getTime() + 1);
            }
            dateFileMap.put(date, file);
        }

        NodeFileService nodeFileService = NodeFileService.getInstance();

        List<ApplicationLog> applicationLogs = applicationLogService.getApplicationLogs();
        for (Map.Entry<Tactic, Map<Date, File>> entry : map.entrySet()) {
            Tactic tactic = entry.getKey();
            ApplicationLog applicationLog = applicationService.findOrCreate(applicationLogs, tactic);
            nodeFileService.appendToNodeLogs(entry.getValue(), applicationLog);
        }

        ReaderService readerService = ReaderService.getInstance();
        readerService.prepareBufferedReaders(applicationLogs);
    }

}
