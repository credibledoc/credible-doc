package com.credibledoc.combiner;

import com.credibledoc.combiner.context.Context;
import com.credibledoc.combiner.file.FileService;
import com.credibledoc.combiner.state.FilesMergerState;
import com.credibledoc.combiner.tactic.Tactic;
import com.credibledoc.combiner.tactic.TacticService;
import com.credibledoc.tactic.FirstApplicationTactic;
import com.credibledoc.tactic.SecondApplicationTactic;
import com.credibledoc.tactic.SpecialTactic;
import com.credibledoc.tactic.SyslogTactic;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CombinerServiceProgrammableTest {
    private static final Logger logger = LoggerFactory.getLogger(CombinerServiceProgrammableTest.class);
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * https://unix.stackexchange.com/questions/250660/merging-log-files-with-different-timestamp-formats
     */
    @Test
    public void testFilesMergeWithTimestampInDifferentFormats() throws Exception {
        // An example of a first (Syslog) format
        // 5:2015 Jan 16 15:08:01:ABC:foo1:1559: common.c:946:Enabling filter
        // 6:2015 Jan 16 15:08:10:ABC:bar1:1461: api.c:124:Trigger activated
        // 6:2015 Jan 16 16:08:16:BMC:kernel:-:<6>drivers/usb1_1.c:598:Error processing request on endpoint 0
        
        // An example of a second (Special) format. The date time is given in first line of the file.
        // Following lines are "relative" to the first line.
        // 
        // Timestamp H:M:S 15:4:1 D:M:Y 16:1:2015
        // Firmware Version: 121020150140
        // [04:01]------------[ Logs ]------------
        // [04:03]Device Data: -> Supported Attributes -> 0x8033B
        // [04:11]Device Cleanup
        //
        // [04:19]SendClearMsg ...
        // [04:23]Param:GetData failed
        // [04:51]Current Update Count:7
        // [05:01]MODECHK:Normal mode
        
        // a timestamp of 4:01 is 4 minutes, 1 second since 15:4:1 which should be translated as 15:08:2.
        
        File sourceDirectory = new File("src/test/resources/different-formats");
        assertTrue(sourceDirectory.exists());
        
        // Collect source files
        Set<File> sourceFiles = new FileService().collectFiles(sourceDirectory);
        assertEquals(2, sourceFiles.size());
        
        // Contains instances of Tactics, NodeFiles and NodeLogs
        Context context = new Context().init();

        // Instantiate parsers for different log formats
        Set<Tactic> tactics = new HashSet<>();
        tactics.add(new SyslogTactic());
        tactics.add(new SpecialTactic());
        context.getTacticRepository().setTactics(tactics);

        SourceFilesReader sourceFilesReader = new SourceFilesReader();
        sourceFilesReader.setContext(context);
        sourceFilesReader.addSourceFiles(sourceFiles);
        
        List<String> multiLine = sourceFilesReader.read();

        File targetFolder = temporaryFolder.newFolder("different-formats");
        File targetFile = new CombinerService().prepareTargetFile(targetFolder, "combined.txt");
        try (FileWriter fileWriter = new FileWriter(targetFile)) {
            do {
                write(multiLine, fileWriter, sourceFilesReader, context);
                multiLine = sourceFilesReader.read();
            } while (multiLine != null);
        }
        File exemplarFile = new File("src/test/resources/different-formats-expected/expected.txt");
        assertTrue(exemplarFile.exists());

        verifyFilesAreTheSame(exemplarFile, targetFile);
    }

    private void write(List<String> multiLine, FileWriter fileWriter, SourceFilesReader sourceFilesReader,
                       Context context) throws IOException {
        Date date = sourceFilesReader.currentTactic(context).findDate(multiLine.get(0));
        String dateString = date == null ? "" : SIMPLE_DATE_FORMAT.format(date) + " ";
        File file = sourceFilesReader.currentFile(context);
        String fileName = file.getName() + " ";
        for (String line : multiLine) {
            fileWriter.write(dateString);
            fileWriter.write(fileName);
            fileWriter.write(line);
            fileWriter.write(System.lineSeparator());
        }
    }

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

        // Contains instances of Tactics, NodeFiles and NodeLogs
        Context context = new Context().init();

        // Instantiate parsers for different log formats
        Set<Tactic> tactics = new HashSet<>();
        tactics.add(new FirstApplicationTactic());
        tactics.add(new SecondApplicationTactic());
        context.getTacticRepository().setTactics(tactics);

        TacticService.getInstance().prepareReaders(files, context);

        File targetFolder = temporaryFolder.newFolder("generated");
        CombinerService combinerService = CombinerService.getInstance();
        File targetFile = combinerService.prepareTargetFile(targetFolder, "combined-test.txt");

        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile))) {
            FilesMergerState filesMergerState = new FilesMergerState();
            filesMergerState.setNodeFiles(context.getNodeFileRepository().getNodeFiles());

            combinerService.combine(outputStream, filesMergerState, context);
        }
        File exemplarFile = new File("src/test/resources/test-log-files-expected/combined.txt");
        assertTrue(exemplarFile.exists());

        verifyFilesAreTheSame(exemplarFile, targetFile);
    }

    private void verifyFilesAreTheSame(File leftFile, File rightFile) throws IOException {
        assertTrue(leftFile.isFile());
        assertTrue(rightFile.isFile());
        
        byte[] leftBytes = Files.readAllBytes(leftFile.toPath());
        byte[] rightBytes = Files.readAllBytes(rightFile.toPath());
        if (!Arrays.equals(leftBytes, rightBytes)) {
            logger.info("leftBytes.size: {}', rightBytes.size: '{}'", leftBytes.length, rightBytes.length);
            String leftString = new String(leftBytes, StandardCharsets.UTF_8);
            String rightString = new String(rightBytes, StandardCharsets.UTF_8);
            assertEquals(leftString, rightString);
        }
    }

}
