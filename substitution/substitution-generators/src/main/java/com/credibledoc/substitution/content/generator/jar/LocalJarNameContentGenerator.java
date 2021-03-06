package com.credibledoc.substitution.content.generator.jar;

import com.credibledoc.substitution.core.content.Content;
import com.credibledoc.substitution.core.content.ContentGenerator;
import com.credibledoc.substitution.core.context.SubstitutionContext;
import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.core.placeholder.Placeholder;

import java.io.File;

/**
 * Generates a jar name. Tries to find the current jar name in the <b>target</b> directory.
 * <p>
 * Usage:
 * <pre>{@code
 *     &&beginPlaceholder {
 *         "className": "com.credibledoc.substitution.content.generator.jar.LocalJarNameContentGenerator",
 *         "description": "Current name of the credible-doc-generator-X.X.X.jar.",
 *         "parameters": {
 *             "targetDirectoryRelativePath": "credible-doc-generator/target",
 *             "jarNamePrefix": "credible-doc-generator-"
 *         }
 *     } &&endPlaceholder
 * }</pre>
 * @author Kyrylo Semenko
 */
public class LocalJarNameContentGenerator implements ContentGenerator {
    private static final String JAR_NAME_PREFIX = "jarNamePrefix";
    private static final String TARGET_DIRECTORY_RELATIVE_PATH = "targetDirectoryRelativePath";
    public static final String MODULE_NAME = "substitution-generators";
    private static final String DEFAULT_DIRECTORY_NAME = "target";

    @Override
    public Content generate(Placeholder placeholder, SubstitutionContext substitutionContext) {
        try {
            File targetDirectory = getTargetDirectory(placeholder);
            validateTargetDirectoryExists(targetDirectory);
            File[] files = targetDirectory.listFiles();
            validateFilesNotNull(targetDirectory, files);
            String jarNamePrefix = placeholder.getParameters().get(JAR_NAME_PREFIX);
            if (jarNamePrefix == null) {
                throw new SubstitutionRuntimeException("The '" + JAR_NAME_PREFIX +
                    "' property is mandatory for this placeholder with description: " + placeholder.getDescription());
            }
            for (File file : files) {
                String name = file.getName();
                if (name.startsWith(jarNamePrefix) &&
                        !name.contains("-sources") &&
                        !name.contains("-javadoc") &&
                        name.endsWith(".jar")) {

                    Content content = new Content();
                    content.setMarkdownContent(name);
                    return content;
                }
            }
            throw new SubstitutionRuntimeException("Jar name cannot be found. " +
                "Target directory '" + targetDirectory.getAbsolutePath() +
                "' has no jar file with '" + jarNamePrefix + "' prefix. Placeholder: " + placeholder);
        } catch (Exception e) {
            throw new SubstitutionRuntimeException(e);
        }
    }

    private void validateFilesNotNull(File targetDirectory, File[] files) {
        if (files == null) {
            throw new SubstitutionRuntimeException("Local variable 'files' is null. " +
                "TargetDirectory: " + targetDirectory.getAbsolutePath());
        }
    }

    private void validateTargetDirectoryExists(File targetDirectory) {
        if (!targetDirectory.exists()) {
            throw new SubstitutionRuntimeException("Jar name cannot be found. " +
                "Target directory doesn't exist: '" + targetDirectory.getAbsolutePath() +
                "'. Please run 'mvn install' first");
        }
    }

    private File getTargetDirectory(Placeholder placeholder) {
        String target = placeholder.getParameters().get(TARGET_DIRECTORY_RELATIVE_PATH);
        if (target == null) {
            target = DEFAULT_DIRECTORY_NAME;
        }
        return new File(target);
    }

}
