package com.credibledoc.combiner.log.reader;

import com.credibledoc.combiner.CombinerService;
import com.credibledoc.combiner.config.Config;
import com.credibledoc.combiner.config.ConfigService;
import com.credibledoc.combiner.context.CombinerContext;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.state.FilesMergerState;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ReaderServiceTest {

    @Test
    public void findTheOldestTest() throws Exception {
        File configFile = new File("src/test/resources/reader-config/log-combiner.properties");
        Config config = new ConfigService().loadConfig(configFile.getAbsolutePath());
        assertNotNull(config);

        File logDirectory = new File("src/test/resources/reader");
        assertTrue(logDirectory.exists());

        // Contains instances of Tactics, NodeFiles and NodeLogs
        CombinerContext combinerContext = new CombinerContext().init();

        CombinerService combinerService = CombinerService.getInstance();
        combinerService.prepareReader(logDirectory, config, combinerContext);

        FilesMergerState filesMergerState = new FilesMergerState();
        filesMergerState.setNodeFiles(combinerContext.getNodeFileRepository().getNodeFiles());
        ReaderService readerService = ReaderService.getInstance();
        String line = readerService.readLineFromReaders(filesMergerState, combinerContext);
        LogBufferedReader logBufferedReader = filesMergerState.getCurrentNodeFile().getLogBufferedReader();
        List<String> result = new ArrayList<>();
        long before = System.nanoTime();
        while (line != null) {
            List<String> multiline = readerService.readMultiline(line, logBufferedReader, combinerContext);
            result.addAll(multiline);
            line = readerService.readLineFromReaders(filesMergerState, combinerContext);
            logBufferedReader = filesMergerState.getCurrentNodeFile().getLogBufferedReader();
        }
        long after = System.nanoTime();
        long nanoseconds = after - before;
        long milliseconds = nanoseconds / 1000000;
        System.out.println("Nanoseconds: " + nanoseconds + " (milliseconds: " + milliseconds + ")");
        
        File resultFile = new File("src/test/resources/reader-config/result.log");
        assertTrue(resultFile.exists());
        BufferedReader reader = new BufferedReader(new FileReader(resultFile));
        for (String resultLine : result) {
            String expectedLine = reader.readLine();
            assertEquals(expectedLine, resultLine);
        }
        assertNull(reader.readLine());
        reader.close();
}

    public void generateTestResult(List<String> result) throws IOException {
        File file = new File("C:\\Users\\semenko\\git\\credibledoc\\credible-doc\\log-combiner-parent\\log-combiner" +
            "\\src\\test\\resources\\reader-config\\result.log");
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        for (String row : result) {
            writer.write(row + "\n");
        }
        writer.close();
    }

    public void generateTestData() throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        File dir = new File("C:\\Users\\semenko\\git\\credibledoc\\credible-doc\\log-combiner-parent\\log" +
            "-combiner\\src\\test\\resources\\reader");
        Date date = new Date();
        for (int i = 1; i <= 25; i++) {
            File file = new File(dir, String.format("%03d", i) + ".log");
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            for (int k = 0; k < 10; k++) {
                writer.write(dateFormat.format(date) + " - " + file.getName() + "\n");
                if (k % 3 == 0) {
                    writer.write("Second line" + System.lineSeparator());
                }
                date.setTime(date.getTime() + 1);
            }
            writer.close();
            if (i % 2 == 0) {
                date.setTime(date.getTime() - 8);
            }
        }
    }
}
