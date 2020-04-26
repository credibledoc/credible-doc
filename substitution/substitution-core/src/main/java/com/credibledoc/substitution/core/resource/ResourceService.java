package com.credibledoc.substitution.core.resource;

import com.credibledoc.substitution.core.configuration.Configuration;
import com.credibledoc.substitution.core.configuration.ConfigurationService;
import com.credibledoc.substitution.core.context.SubstitutionContext;
import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * <p>
 * This service contains methods for accessing to local resource files,
 * for example java source files or templates in the /resource folder.
 *
 * <p>
 * In case when the application is launched from IDE (for example Eclipse
 * or Idea) these resources should be loaded in a different manner
 * then in case where the application is launched from a jar file.
 *
 * @author Kyrylo Semenko
 */
public class ResourceService {
    public static final String SUBSTITUTION_CORE_MODULE_NAME = "substitution-core";
    private static final Logger logger = LoggerFactory.getLogger(ResourceService.class);
    private static final String FILE_PREFIX = "file:/";
    private static final String BOOT_INF_CLASSES_WITH_EXCLAMATION_MARK = "!/BOOT-INF/";
    private static final String CLASSES = "classes";
    private static final String BOOT_INF_CLASSES = "BOOT-INF/classes";
    private static final String SLASH = "/";
    private static final String JAVA_FILE_EXTENSION = ".java";

    /**
     * Singleton.
     */
    private static ResourceService instance;

    private ResourceService() {
        // empty
    }

    /**
     * @return The {@link ResourceService} singleton.
     */
    public static ResourceService getInstance() {
        if (instance == null) {
            instance = new ResourceService();
        }
        return instance;
    }

    /**
     * Find resources in IDE directory or jar file, depends
     * on runtime environment.
     *
     * @param endsWith          for example '.md' or '.html'. Can be <b>null</b>. In this case all resources will be returned.
     * @param templatesResource for example {@link ConfigurationService#TEMPLATES_RESOURCE}.
     * @return List of resources from jar file or classpath, for example <b>["/template/markdown/README.md", "/template/site/main.html"]</b>
     */
    public List<TemplateResource> getResources(String endsWith, String templatesResource) {
        try {
            String locationPath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            logger.trace("Source code location path: '{}'", locationPath);

            if (isLocatedInJar(locationPath)) {
                return collectResourcesFromJar(endsWith, locationPath, templatesResource);
            } else {
                return collectResourcesFromIde(endsWith, templatesResource);
            }
        } catch (Exception e) {
            throw new SubstitutionRuntimeException(e);
        }
    }

    /**
     * Find resource of resourceClass in a file system or jar file, depends
     * on runtime environment.
     *
     * @param resourceClass some Class from this application
     * @return resource relative path, for example
     * <pre>/com/credibledoc/substitution/resource/ResourceService.java</pre>
     * in IDE environment.
     * <p>
     * And for jar file environment it will be
     * <pre>/BOOT-INF/classes/com/credibledoc/substitution/resource/ResourceService.java</pre>
     */
    public String getResource(Class<?> resourceClass) {
        String locationPath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        if (isLocatedInJar(locationPath)) {
            return SLASH + BOOT_INF_CLASSES + SLASH +
                    resourceClass.getCanonicalName().replaceAll("\\.", SLASH) + JAVA_FILE_EXTENSION;
        } else {
            return SLASH +
                    resourceClass.getCanonicalName().replaceAll("\\.", SLASH) + JAVA_FILE_EXTENSION;
        }
    }

    private boolean isLocatedInJar(String locationPath) {
        boolean found = locationPath.contains(FILE_PREFIX) &&
            locationPath.contains(BOOT_INF_CLASSES_WITH_EXCLAMATION_MARK);
        if (found) {
            logger.info("Resource found in a jar file. LocationPath: '{}'", locationPath);
        } else {
            logger.info("Resource cannot be found in a jar file. LocationPath: '{}'", locationPath);
        }
        return found;
    }

    private List<TemplateResource> collectResourcesFromJar(String endsWith,
                                         String locationPath,
                                         String templatesResource) throws IOException {
        List<TemplateResource> result = new ArrayList<>();
        int beginIndex = FILE_PREFIX.length() - 1;
        int endIndex = locationPath.indexOf(BOOT_INF_CLASSES_WITH_EXCLAMATION_MARK);
        File file = new File(locationPath.substring(beginIndex, endIndex));
        if (!file.exists()) {
            throw new SubstitutionRuntimeException("LocationPath: '" + locationPath +
                "'. The file cannot be found '" + file.getAbsolutePath() + "'");
        }
        // Running from JAR file
        final JarFile jarFile = new JarFile(file);
        final Enumeration<JarEntry> entries = jarFile.entries();
        String prefix = BOOT_INF_CLASSES + SLASH + templatesResource + SLASH;
        while (entries.hasMoreElements()) {
            final String name = entries.nextElement().getName();
            if (name.startsWith(prefix) &&
                    (endsWith == null || name.endsWith(endsWith))) {

                TemplateResource templateResource = new TemplateResource();
                templateResource.setType(ResourceType.CLASSPATH);
                String path = name.substring(BOOT_INF_CLASSES.length());
                templateResource.setPath(path);
                result.add(templateResource);
            }
        }
        jarFile.close();
        return result;
    }

    /**
     * Running from IDE
     *
     * @param endsWith will bew used as the third argument in the
     * {@link #collectTemplateFilesRecursively(File, List, String)} method.
     * @param templatesResource template path.
     * @throws URISyntaxException in case when templateResource
     * is not a valid {@link java.net.URI}
     */
    private List<TemplateResource> collectResourcesFromIde(String endsWith,
                                         String templatesResource) throws URISyntaxException {
        // absolute path
        File dir = new File(templatesResource);
        if (dir.exists()) {
            return getResources(endsWith, dir);
        }
        // relative path
        final URL url = getClass().getResource(SLASH + templatesResource);
        if (url != null) {
            final File classesDirectory = new File(url.toURI());
            String from = "target" + File.separator + CLASSES + File.separator + templatesResource;
            String to = "src" + File.separator + "main" + File.separator + "resources" + File.separator + templatesResource;
            File directory = new File(classesDirectory.getAbsolutePath().replace(from, to));
            if (!directory.exists()) {
                directory = classesDirectory;
            }
            return getResources(endsWith, directory);
        } else {
            String directoryString = new File(templatesResource).getAbsolutePath();

            throw new SubstitutionRuntimeException(
                    "Resource of template not found. TemplateResource: '" + templatesResource + "'." +
                        " Directory: '" + directoryString + "'." +
                        " This resource can be configured" +
                        " with '" + ConfigurationService.TEMPLATES_RESOURCE_KEY + "' key" +
                        " or directly set by calling for example" +
                        " 'ConfigurationService.getInstance().getConfiguration()." +
                        "setTemplatesResource(\"resource/in/classpath\");'.");
        }
    }

    private List<TemplateResource> getResources(String endsWith,  File directory) {
        List<TemplateResource> result = new ArrayList<>();
        logger.info("Resource has been found in the directory: '{}'", directory.getAbsolutePath());
        List<File> templateFiles = new ArrayList<>();
        collectTemplateFilesRecursively(directory, templateFiles, endsWith);
        for (File templateFile : templateFiles) {
            TemplateResource templateResource = new TemplateResource();
            templateResource.setType(ResourceType.FILE);
            templateResource.setFile(templateFile);
            result.add(templateResource);
        }
        return result;
    }

    private void collectTemplateFilesRecursively(File directory, List<File> templateFiles, String fileExtension) {
        File[] files = directory.listFiles();
        if (files == null) {
            throw new SubstitutionRuntimeException("Files == null. Directory: " + directory.getAbsolutePath());
        }
        for (File file : files) {
            if (file.isFile()) {
                if (fileExtension == null || file.getName().endsWith(fileExtension)) {
                    templateFiles.add(file);
                }
            } else {
                collectTemplateFilesRecursively(file, templateFiles, fileExtension);
            }
        }
    }

    /**
     * Generate a relative path of the file that will be created from the resource defined in the argument.
     *
     * @param templateResource for example /template/markdown/doc/diagrams.md
     * @return For example /markdown/doc/diagrams.md
     */
    public String generatePlaceholderResourceRelativePath(TemplateResource templateResource, SubstitutionContext substitutionContext) {
        Configuration configuration = substitutionContext.getConfigurationService().getConfiguration();
        String configTemplatesPath = configuration.getTemplatesResource();
        String configPathNormalized = configTemplatesPath.replaceAll("\\\\", SLASH);
        String path;
        if (templateResource.getType() == ResourceType.FILE) {
            File file = templateResource.getFile();
            String parentPath = file.getParentFile().getAbsolutePath();
            String parentPathNormalized = parentPath.replaceAll("\\\\", SLASH);
            if (!parentPathNormalized.contains(configPathNormalized)) {
                throw new SubstitutionRuntimeException("Expected replacedPath with substring '" +
                    configPathNormalized + "' but found '" + parentPathNormalized + "'.");
            }
            int startIndex = parentPathNormalized.indexOf(configPathNormalized) + configPathNormalized.length();
            return parentPathNormalized.substring(startIndex) + SLASH + file.getName();
        } else if (templateResource.getType() == ResourceType.CLASSPATH) {
            path = templateResource.getPath();
        } else {
            throw new SubstitutionRuntimeException("Unknown ResourceType " + templateResource.getType());
        }
        return path.substring(configPathNormalized.length() + 1);
    }
}
