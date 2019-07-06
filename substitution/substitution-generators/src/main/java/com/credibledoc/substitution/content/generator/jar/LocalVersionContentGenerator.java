package com.credibledoc.substitution.content.generator.jar;

import com.credibledoc.substitution.core.content.Content;
import com.credibledoc.substitution.core.content.ContentGenerator;
import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.core.placeholder.Placeholder;

import java.io.File;

/**
 * Generates a jar version. Tries to find the current jar in the <b>target</b> directory.
 * <p>
 * Usage:
 * <pre>{@code
 *     &&beginPlaceholder {
 *         "className": "com.credibledoc.substitution.content.generator.jar.LocalVersionContentGenerator",
 *         "description": "Current name of the credible-doc-generator-X.X.X.jar version.",
 *         "parameters": {
 *             "targetDirectoryRelativePath": "target",
 *             "jarNamePrefix": "credible-doc-generator-"
 *         }
 *     } &&endPlaceholder
 * }</pre>
 * @author Kyrylo Semenko
 */
public class LocalVersionContentGenerator implements ContentGenerator {
    private static final String JAR_NAME_PREFIX = "jarNamePrefix";
    private static final String TARGET_DIRECTORY_RELATIVE_PATH = "targetDirectoryRelativePath";
    private static final String DOT_JAR = ".jar";

    @Override
    public Content generate(Placeholder placeholder) {
        try {
            String target = placeholder.getParameters().get(TARGET_DIRECTORY_RELATIVE_PATH);
            if (target == null) {
                target = "target";
            }
            File targetDirectory = new File(target);
            if (!targetDirectory.exists()) {
                throw new SubstitutionRuntimeException("Jar name cannot be found. " +
                    "Target directory does not exists: '" + targetDirectory.getAbsolutePath() +
                    "'. Please run 'mvn install' first");
            }
            File[] files = targetDirectory.listFiles();
            if (files == null) {
                throw new SubstitutionRuntimeException("Local variable 'files' is null. " +
                    "TargetDirectory: " + targetDirectory.getAbsolutePath());
            }
            String jarNamePrefix = placeholder.getParameters().get(JAR_NAME_PREFIX);
            if (jarNamePrefix == null) {
                throw new SubstitutionRuntimeException("The '" + JAR_NAME_PREFIX +
                    "' property is mandatory for this placeholder: " + placeholder);
            }
            for (File file : files) {
                String name = file.getName();
                if (name.startsWith(jarNamePrefix) &&
                        !name.contains("-sources") &&
                        !name.contains("-javadoc") &&
                        name.endsWith(DOT_JAR)) {

                    Content content = new Content();
                    String versionName = name.substring(jarNamePrefix.length(), name.indexOf(DOT_JAR));
                    content.setMarkdownContent(versionName);
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

}
