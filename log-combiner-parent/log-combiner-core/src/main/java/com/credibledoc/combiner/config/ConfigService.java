package com.credibledoc.combiner.config;

import com.credibledoc.combiner.exception.CombinerRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * This stateful singleton loads configuration parameters from file and provides them to the application.
 *
 * @author Kyrylo Semenko
 */
public class ConfigService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);

    /**
     * Default name of configuration file. It can be found next to jar file.
     */
    private static final String LOG_COMBINER_PROPERTIES = "log-combiner.properties";

    /**
     * {@link Config} loaded from file
     */
    private Config config;

    /**
     * Initial value of this field is 'false'. It set to 'true' when configuration
     * is loaded from file and it does not depend on successfully or not.
     */
    private boolean loadingHasBeenTried;

    /**
     * Singleton.
     */
    private static ConfigService instance;

    /**
     * @return The {@link ConfigService} singleton.
     */
    public static ConfigService getInstance() {
        if (instance == null) {
            instance = new ConfigService();
        }
        return instance;
    }

    /**
     * Try to load {@link Config} from configAbsolutePath. If the file not found throw an exception.
     *
     * @param configAbsolutePath can be 'null'. In this case {@link Config} will be loaded from classpath.
     *                           If it doesn't exists, the default {@link Config} will be returned.
     * @return Configuration of this combiner.
     */
    public Config loadConfig(String configAbsolutePath) {
        if (loadingHasBeenTried) {
            return config;
        }
        loadingHasBeenTried = true;
        config = new Config();
        File propertiesFile;
        if (configAbsolutePath != null) {
            propertiesFile = new File(configAbsolutePath);
            if (!propertiesFile.exists()) {
                throw new CombinerRuntimeException("Configuration file cannot be found. File: '" +
                    propertiesFile.getAbsolutePath() + "'");
            }
            logger.info("Configuration file will be loaded from command-line parameter. File: '{}'",
                propertiesFile.getAbsolutePath());
        } else {
            File jarPath = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
            String nextToJar = jarPath.getParent() + File.separator + LOG_COMBINER_PROPERTIES;
            logger.info("Trying to find configuration file next to jar file: '{}'", nextToJar);
            propertiesFile = new File(nextToJar);
            if (propertiesFile.exists()) {
                logger.info("Configuration file is found next to jar file. File: {}",
                    propertiesFile.getAbsolutePath());
            }
        }
        if (propertiesFile.exists()) {
            loadProperties(propertiesFile);
            logger.info("Configuration loaded: {}", config);
        } else {
            logger.info("Configuration file not found, default Config will be returned.");
        }
        return config;
    }

    private void loadProperties(File propertiesFile) {
        Properties properties;
        try (InputStream input = new FileInputStream(propertiesFile)) {
            properties = new Properties();
            properties.load(input);

            String insertLineSeparatorBetweenFiles = properties.getProperty("insertLineSeparatorBetweenFiles");
            if ("true".equals(insertLineSeparatorBetweenFiles)) {
                config.setInsertLineSeparatorBetweenFiles(true);
            }

            String printNodeName = properties.getProperty("printNodeName");
            if ("false".equals(printNodeName)) {
                config.setPrintNodeName(false);
            }

            loadTacticConfigurations(properties);

        } catch (Exception e) {
            throw new CombinerRuntimeException("Configuration file cannot be loaded. File: '" +
                propertiesFile.getAbsolutePath() + "'", e);
        }
    }

    private void loadTacticConfigurations(Properties properties) {
        int index = 0;
        boolean foundNext = true;
        while (foundNext) {
            String regex = properties.getProperty("regex[" + index + "]");
            String simpleDateFormat = properties.getProperty("simpleDateFormat[" + index + "]");
            if (regex != null && simpleDateFormat != null) {
                TacticConfig tacticConfig = new TacticConfig();
                tacticConfig.setRegex(regex);
                tacticConfig.setSimpleDateFormat(simpleDateFormat);
                String maxIndexEndOfTime = properties.getProperty("maxIndexEndOfTime[" + index + "]");
                tacticConfig.setMaxIndexEndOfTime(Integer.valueOf(maxIndexEndOfTime));
                String applicationName = properties.getProperty("applicationName[" + index + "]");
                tacticConfig.setApplicationName(applicationName);
                config.getTacticConfigs().add(tacticConfig);
                index++;
            } else {
                foundNext = false;
            }
        }
    }
}
