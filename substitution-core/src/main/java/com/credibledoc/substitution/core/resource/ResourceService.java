package com.credibledoc.substitution.core.resource;

import com.credibledoc.substitution.core.configuration.ConfigurationService;
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
     * @param endsWith          for example '.md'. Can be <b>null</b>. In this case all resources will be returned.
     * @param templatesResource for example {@link ConfigurationService#TEMPLATES_RESOURCE}.
     * @return List of resources from jar file or classpath, for example <b>["/template/doc/README.md"]</b>
     */
    public List<String> getResources(String endsWith, String templatesResource) {
        List<String> result = new ArrayList<>();
        try {
            String locationPath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            logger.trace("Source code location path: '{}'", locationPath);

            if (isLocatedInJar(locationPath)) {
                collectResourcesFromJar(result, endsWith, locationPath, templatesResource);
            } else {
                collectResourcesFromIde(result, endsWith, templatesResource);
            }
            return result;
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
        return locationPath.contains(FILE_PREFIX) &&
            locationPath.contains(BOOT_INF_CLASSES_WITH_EXCLAMATION_MARK);
    }

    private void collectResourcesFromJar(List<String> result,
                                         String endsWith,
                                         String locationPath,
                                         String templatesResource) throws IOException {
        int beginIndex = FILE_PREFIX.length();
        int endIndex = locationPath.indexOf(BOOT_INF_CLASSES_WITH_EXCLAMATION_MARK);
        File file = new File(locationPath.substring(beginIndex, endIndex));
        if (!file.exists()) {
            throw new SubstitutionRuntimeException("The file cannot be found '" + file.getAbsolutePath() + "'");
        }
        // Running from JAR file
        final JarFile jarFile = new JarFile(file);
        final Enumeration<JarEntry> entries = jarFile.entries();
        String prefix = BOOT_INF_CLASSES + SLASH + templatesResource + SLASH;
        while (entries.hasMoreElements()) {
            final String name = entries.nextElement().getName();
            if (name.startsWith(prefix) &&
                    (endsWith == null || name.endsWith(endsWith))) {

                result.add(name.substring(BOOT_INF_CLASSES.length()));
            }
        }
        jarFile.close();
    }

    /**
     * Running from IDE
     *
     * @param result the list for appended resources
     * @param endsWith will bew used as the third argument in the
     * {@link #collectTemplateFilesRecursively(File, List, String)} method.
     * @param templatesResource template path.
     * @throws URISyntaxException in case when templateResource
     * is not a valid {@link java.net.URI}
     */
    private void collectResourcesFromIde(List<String> result,
                                         String endsWith,
                                         String templatesResource) throws URISyntaxException {
        final URL url = getClass().getResource(SLASH + templatesResource);
        if (url != null) {
            final File directory = new File(url.toURI());
            List<File> templateFiles = new ArrayList<>();
            collectTemplateFilesRecursively(directory, templateFiles, endsWith);
            for (File templateFile : templateFiles) {
                String absolutePath = templateFile.getAbsolutePath();
                int index = absolutePath.indexOf(CLASSES);
                if (index == -1) {
                    throw new SubstitutionRuntimeException("Cannot find out '" + CLASSES +
                            "' substring in the string '" + absolutePath + "'");
                }
                String substring = absolutePath.substring(index + CLASSES.length());
                result.add(substring.replaceAll("\\\\", SLASH));
            }
        } else {
            throw new SubstitutionRuntimeException(
                    "Resource of template not found. TemplateResource: '" + templatesResource + "'");
        }
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
}
