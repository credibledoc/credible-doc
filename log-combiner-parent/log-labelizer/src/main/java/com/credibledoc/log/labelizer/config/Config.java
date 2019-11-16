package com.credibledoc.log.labelizer.config;

import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;
import de.flapdoodle.embed.process.collections.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Provides access to configuration parameters.
 * <p>
 * This module contains two configuration files, config.properties and config-user.properties.
 * The file config-user.properties is ignored in .gitignore. It contains user - specific settings. So when the both
 * files contain the same key with different values, the config-user.properties property will be used.
 *
 * @author Kyrylo Semenko
 */
public class Config {
    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    private static final String GITHUB_OAUTH_TOKEN = "GithubOauthToken";
    private static final String DATABASE_DIR = "databaseDir";
    private static final String DATABASE_HOST = "databaseHost";
    private static final String DATABASE_PORT = "databasePort";
    private static final String DATABASE_NAME = "databaseName";
    private static Map<String, String> properties = new HashMap<>();

    private static final String CONFIG_PROPERTIES = "config/config.properties";

    private static final String CONFIG_USER_PROPERTIES = "config/config-user.properties";

    static {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream(CONFIG_PROPERTIES)) {
            if (input == null) {
                throw new LabelizerRuntimeException("Resource cannot be found: " + CONFIG_PROPERTIES);
            }
            logger.info("The configuration will be loaded: '{}'", CONFIG_PROPERTIES);

            Properties prop = new Properties();

            prop.load(input);

            for (Object keyObject : prop.keySet()) {
                String key = (String) keyObject;
                String value = prop.getProperty(key);
                properties.put(key, value);
            }
        } catch (IOException e) {
            throw new LabelizerRuntimeException(e);
        }

        try (InputStream userInput = Config.class.getClassLoader().getResourceAsStream(CONFIG_USER_PROPERTIES)) {
            if (userInput == null) {
                logger.info("Resource cannot be found: {}", CONFIG_USER_PROPERTIES);
            } else {
                logger.info("The configuration will be loaded and overwritten: '{}'", CONFIG_USER_PROPERTIES);
                Properties userProp = new Properties();

                userProp.load(userInput);

                for (Object keyObject : userProp.keySet()) {
                    String key = (String) keyObject;
                    String value = userProp.getProperty(key);
                    properties.put(key, value);
                }
            }
        } catch (IOException e) {
            throw new LabelizerRuntimeException(e);
        }
    }
    
    private Config() {
        throw new LabelizerRuntimeException("Please do not instantiate this static helper.");
    }

    public static String getGithubOauthToken() {
        String token = properties.get(GITHUB_OAUTH_TOKEN);
        if (token == null) {
            throw new LabelizerRuntimeException("Please put the " + GITHUB_OAUTH_TOKEN + " value in " +
                "the config/config-user.properties file. " +
                "The token can be obtained from the https://github.com/settings/tokens page of your account. " +
                "The config/config-user.properties file should be created by yourself " +
                "next to the config/config.properties file.");
        }
        return token;
    }

    public static List<String> getGithubSearchKeywords() {
        String value = properties.get("GithubSearchKeywords");
        return Collections.newArrayList(value.split(","));
    }

    public static String getDatabaseDir() {
        String value = properties.get(DATABASE_DIR);
        if (value == null) {
            throw new LabelizerRuntimeException("Please set the " + DATABASE_DIR + " property in the " +
                "config/config-user.properties file. This directory will be used for MongoDB zip and data files.");
        }
        return value;
    }

    public static String getDatabaseHost() {
        String value = properties.get(DATABASE_HOST);
        if (value == null) {
            throw new LabelizerRuntimeException("Please set the " + DATABASE_HOST + " property in the " +
                "config/config-user.properties file. This host will be used by MongoDB client to connect.");
        }
        return value;
    }

    public static int getDatabasePort() {
        String value = properties.get(DATABASE_PORT);
        if (value == null) {
            throw new LabelizerRuntimeException("Please define the " + DATABASE_PORT + " value in the " +
                "config/config-user.properties file. This port will be used by MongoDB client to connect.");
        }
        return Integer.parseInt(value);
    }

    public static String getDatabaseName() {
        String value = properties.get(DATABASE_NAME);
        if (value == null) {
            throw new LabelizerRuntimeException("Please set the " + DATABASE_NAME + " value in the " +
                "config/config-user.properties file. This name will be used by MongoDB client to connect.");
        }
        return value;
    }
}
