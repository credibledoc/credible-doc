package com.credibledoc.substitution.example;

import com.credibledoc.substitution.core.configuration.Configuration;
import com.credibledoc.substitution.core.configuration.ConfigurationService;
import com.credibledoc.substitution.core.context.SubstitutionContext;
import com.credibledoc.substitution.reporting.replacement.ReplacementService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SubstitutionExampleTest {

    private static final Logger logger = LoggerFactory.getLogger(SubstitutionExampleTest.class);

    @Test
    public void testSubstituteHelloWorld() throws IOException {
        logger.info("testSubstituteHelloWorld begin");
        Configuration configuration = new Configuration();

        ConfigurationService configurationService = ConfigurationService.getInstance();
        String configFilePath = "src/test/resources/example/config/substitution.properties";
        configurationService.loadConfiguration(configuration, configFilePath);
        SubstitutionContext substitutionContext = new SubstitutionContext();
        substitutionContext.setConfiguration(configuration);

        ReplacementService replacementService = ReplacementService.getInstance();
        replacementService.replace(substitutionContext);

        File generatedDir = new File(configuration.getTargetDirectory());
        assertTrue(generatedDir.exists());

        File file = new File(generatedDir, "template.txt");
        assertTrue(file.exists());

        String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        assertEquals("Hello, world!", content);
        logger.info("testSubstituteHelloWorld end");
    }
}
