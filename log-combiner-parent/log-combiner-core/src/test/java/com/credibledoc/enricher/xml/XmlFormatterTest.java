package com.credibledoc.enricher.xml;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class XmlFormatterTest {
    private static final Logger log = LoggerFactory.getLogger(XmlFormatterTest.class);
    private final File dir;

    /**
     * Constructs an instance of the class.
     * @param dir a directory with a single scenario data
     */
    public XmlFormatterTest(File dir, String dirName) {
        this.dir = dir;
        log.info("Test finished: [{}]", dirName);
    }

    @Parameterized.Parameters(name = "{1}")
    public static Collection<Object> getTestDirs() {
        File[] dirs = new File("src/test/resources/xml").listFiles();
        assertNotNull(dirs);
        Collection<Object> result = new ArrayList<>();
        for (File dir : dirs) {
            result.add(new Object[]{dir, dir.getName()});
        }
        return result;
    }
    
    @Test
    public void getPrettyString() throws IOException {
        File expectedFile = new File(dir, "expected.xml");
        assertNotNull(expectedFile);
        File sourceFile = new File(dir, "source.xml");
        assertNotNull(sourceFile);
        
        String expected = new String(Files.readAllBytes(expectedFile.toPath()), StandardCharsets.UTF_8);
        String source = new String(Files.readAllBytes(sourceFile.toPath()), StandardCharsets.UTF_8);

        String formattedXml = XmlFormatter.getPrettyString(source);
        Assert.assertEquals(expected, formattedXml);
    }
    
}
