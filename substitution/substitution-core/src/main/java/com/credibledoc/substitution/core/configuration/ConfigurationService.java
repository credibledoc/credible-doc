package com.credibledoc.substitution.core.configuration;

import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * This stateless object provides services for this domain.
 *
 * @author Kyrylo Semenko
 */
public class ConfigurationService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);
    
    /**
     * Default value of the {@link #TEMPLATES_RESOURCE_KEY}.
     */
    public static final String TEMPLATES_RESOURCE = "template/doc";

    /**
     * Configuration key.
     * The value contains a relative path to templates in a jar file
     * or in a file system. Default value is {@link #TEMPLATES_RESOURCE}.
     */
    public static final String TEMPLATES_RESOURCE_KEY = "substitution.templates.resource";

    /**
     * Default value of the {@link #PLACEHOLDER_BEGIN_KEY}.
     */
    public static final String PLACEHOLDER_BEGIN = "&&beginPlaceholder";

    /**
     * Configuration key. Default value is {@link #PLACEHOLDER_BEGIN}.
     * <p>
     * Its value contains a flag that marks beginning of content that will be
     * substituted.
     */
    static final String PLACEHOLDER_BEGIN_KEY = "substitution.placeholder.begin";

    /**
     * Default value of the {@link #PLACEHOLDER_END_KEY}.
     */
    public static final String PLACEHOLDER_END = "&&endPlaceholder";

    /**
     * Configuration key. Default value is {@link #PLACEHOLDER_END}.
     * <p>
     * Its value contains a flag that marks end of content that will be
     * substituted.
     */
    static final String PLACEHOLDER_END_KEY = "substitution.placeholder.end";

    /**
     * Default value of the {@link #TARGET_DIRECTORY_KEY}.
     */
    static final String TARGET_DIRECTORY = "target/generated/doc";

    /**
     * Configuration key. Default value is {@link #TARGET_DIRECTORY}.
     * <p>
     * Its value contains a relative or absolute path to a folder where
     * generated files will be placed.
     */
    static final String TARGET_DIRECTORY_KEY = "substitution.target.directory";

    private static final String SUBSTITUTION_PROPERTIES_FILE_PATH = "substitution.properties.file.path";

    private static final String SUBSTITUTION_PROPERTIES_RESOURCE_NAME = "substitution.properties";
    
    public static final String PROPERTIES_LOADED_BY_CLASS_LOADER_FROM_THE_RESOURCE =
        "Properties loaded by ClassLoader from the resource: ";

    /**
     * Singleton.
     */
    private static ConfigurationService instance;

    /**
     * Empty constructor.
     */
    private ConfigurationService() {
        // empty
    }

    /**
     * @return The {@link ConfigurationService} singleton.
     */
    public static ConfigurationService getInstance() {
        if (instance == null) {
            instance = new ConfigurationService();
        }
        return instance;
    }

    /**
     * At first load default values to a {@link Configuration} object.
     * <p>
     * Then in case the {@link System#getProperty(String)} with a key {@link #SUBSTITUTION_PROPERTIES_FILE_PATH}
     * exists, load properties from this file to the {@link Configuration} object.
     * <p>
     * Else in case when a {@link #SUBSTITUTION_PROPERTIES_RESOURCE_NAME} file exists in classpath,
     * then load its properties to the {@link Configuration} object.
     * <p>
     * Log out all properties and they origins.
     */
    public void loadConfiguration(Configuration configuration) {
        Map<String, String> map = new TreeMap<>();

        loadDefaultPropertiesAndCompleteTheMap(map, configuration);

        String propertiesFilePath = System.getProperty(SUBSTITUTION_PROPERTIES_FILE_PATH);
        if (propertiesFilePath != null) {
            loadExternalPropertiesAndCompleteTheMap(map, propertiesFilePath, configuration);
        } else {
            loadClasspathPropertiesAndCompleteTheMap(map, configuration);
        }
        for (Field field : configuration.getClass().getDeclaredFields()) {
            ConfigurationProperty configurationProperty = field.getAnnotation(ConfigurationProperty.class);
            String key = configurationProperty.key();
            String origin = map.get(key);
            String value = getValue(field, configuration);
            logger.info("Configuration property key: '{}', value: '{}', origin: {}", key, value, origin);
        }
    }

    /**
     * Obtain value of the {@link Configuration} by invoking its getter.
     *
     * @param field the {@link Configuration} field this getter belongs to.
     * @return The value returned by a getter of the {@link Field}.
     */
    private String getValue(Field field, Configuration configuration) {
        try {
            String getterName = "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
            Method method = configuration.getClass().getDeclaredMethod(getterName);
            return (String) method.invoke(configuration);
        } catch (Exception e) {
            throw new SubstitutionRuntimeException(e);
        }
    }

    /**
     * Load {@link #SUBSTITUTION_PROPERTIES_RESOURCE_NAME} resource from jar and append its properties
     * to the {@link Configuration}.
     *
     * @param map this map will be completed by loaded properties for logging purposes.
     */
    private void loadClasspathPropertiesAndCompleteTheMap(Map<String, String> map, Configuration configuration) {
        Properties properties = new Properties();
        URL url = getClass().getClassLoader().getResource(SUBSTITUTION_PROPERTIES_RESOURCE_NAME);
        if (url == null) {
            logger.info("ClassLoader cannot find the resource: {}", SUBSTITUTION_PROPERTIES_RESOURCE_NAME);
            return;
        }
        try (InputStream inputStream = url.openStream()) {
            properties.load(inputStream);
            for (Field field : Configuration.class.getDeclaredFields()) {
                ConfigurationProperty configurationProperty = field.getAnnotation(ConfigurationProperty.class);
                if (properties.containsKey(configurationProperty.key())) {
                    setValue(field, properties.getProperty(configurationProperty.key()), configuration);
                    map.put(configurationProperty.key(), "classpath:" + SUBSTITUTION_PROPERTIES_RESOURCE_NAME);
                }
            }
            logger.info("{}{}", PROPERTIES_LOADED_BY_CLASS_LOADER_FROM_THE_RESOURCE, url);
        } catch (Exception e) {
            throw new SubstitutionRuntimeException("ClassLoader cannot find the resource: " +
                SUBSTITUTION_PROPERTIES_RESOURCE_NAME, e);
        }
    }

    /**
     * Load properties from a file to the {@link Configuration} object.
     *
     * @param map this map will be completed by loaded properties for logging purposes.
     * @param propertiesFilePath the data source
     */
    private void loadExternalPropertiesAndCompleteTheMap(Map<String, String> map, String propertiesFilePath, Configuration configuration) {
        try {
            Properties properties = new Properties();
            File propertiesFile = new File(propertiesFilePath);
            if (!propertiesFile.exists()) {
                throw new SubstitutionRuntimeException("File cannot be found. " +
                        SUBSTITUTION_PROPERTIES_FILE_PATH + "=" + propertiesFilePath);
            }
            properties.load(new FileInputStream(propertiesFile));
            String fileAbsolutePath = propertiesFile.getAbsolutePath();
            for (Field field : Configuration.class.getDeclaredFields()) {
                ConfigurationProperty configurationProperty = field.getAnnotation(ConfigurationProperty.class);
                if (properties.containsKey(configurationProperty.key())) {
                    setValue(field, properties.getProperty(configurationProperty.key()), configuration);
                    map.put(configurationProperty.key(), fileAbsolutePath);
                }
            }
            logger.info("Loaded properties file: {}", fileAbsolutePath);
        } catch (IOException e) {
            throw new SubstitutionRuntimeException(e);
        }
    }

    /**
     * Load default constants to the {@link Configuration} object.
     *
     * @param map this map will be completed by loaded properties for logging purposes.
     */
    private void loadDefaultPropertiesAndCompleteTheMap(Map<String, String> map, Configuration configuration) {
        for (Field field : Configuration.class.getDeclaredFields()) {
            ConfigurationProperty configurationProperty = field.getAnnotation(ConfigurationProperty.class);
            if (configurationProperty == null) {
                throw new SubstitutionRuntimeException(
                        "Field " + field.getName() + " of class " + Configuration.class.getCanonicalName() +
                        " does not contains a " + ConfigurationProperty.class.getSimpleName() + " annotation.");
            }
            setValue(field, configurationProperty.defaultValue(), configuration);
            map.put(configurationProperty.key(), configurationProperty.defaultValue());
        }
    }

    /**
     * Set a value to the {@link Configuration} object.
     *
     * @param field will bew used for searching of getter method.
     * @param value will be placed to the {@link Configuration} object field.
     */
    private void setValue(Field field, String value, Configuration configuration) {
        try {
            String fieldNameWithFirstUpperCase =
                    field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
            Method method = Configuration.class.getDeclaredMethod("set" + fieldNameWithFirstUpperCase,
                        String.class);
            method.invoke(configuration, value);
        } catch (Exception e) {
            throw new SubstitutionRuntimeException(e);
        }
    }

}
