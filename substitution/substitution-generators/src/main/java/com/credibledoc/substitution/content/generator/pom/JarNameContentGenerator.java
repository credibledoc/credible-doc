package com.credibledoc.substitution.content.generator.pom;

import com.credibledoc.substitution.core.content.Content;
import com.credibledoc.substitution.core.content.ContentGenerator;
import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.core.placeholder.Placeholder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates the published jar name or its part, for example <i>plantuml-core-1.0.5</i>.
 * Tries to find out the LATEST version in a Nexus repository, for example from the
 * <a href="https://repo1.maven.org/maven2/com/credibledoc/plantuml-core/maven-metadata.xml">plantuml-core</a>
 * API url. If the LATEST version not found an exception will be thrown.
 * <p>
 * Parameters:
 * <p>
 * Link to the repository API is defined in the {@link #URL} {@link Placeholder#getParameters()} parameter. Mandatory
 * parameter.
 * <p>
 * {@link #NAME_ONLY} is optional. Default value 'false'. Allowed value 'true'. If set, the artifactId of
 * the artifact will be returned, for example <i>plantuml-core</i>.
 * <p>
 * {@link #VERSION_ONLY} is optional. Default value 'false'. Allowed value 'true'. If set, the version of
 * the artifact will be returned, for example <i>1.0.5</i>.
 * <p>
 * {@link #NAME_AND_VERSION_SEPARATOR} is mandatory when {@link #NAME_ONLY} and {@link #VERSION_ONLY} are 'false'.
 * It contains a separator between artifact name and version, for example <b>-</b>.
 * <p>
 *
 * Example
 * <pre>{@code
 *         &&beginPlaceholder {
 *             "className": "com.credibledoc.substitution.content.generator.pom.JarNameContentGenerator",
 *             "description": "Latest plantuml-core.jar name",
 *             "parameters": {
 *                 "url": "https://repo1.maven.org/maven2/com/credibledoc/plantuml-core/maven-metadata.xml",
 *                 "nameOnly": "false",
 *                 "versionOnly": "false",
 *                 "nameAndVersionSeparator": "-"
 *             }
 *         } &&endPlaceholder
 * }</pre>
 *
 * @author Kyrylo Semenko
 */
public class JarNameContentGenerator implements ContentGenerator {
    private static final Logger logger = LoggerFactory.getLogger(JarNameContentGenerator.class);

    private static final String ARTIFACT_ID_BEGIN_TAG = "<artifactId>";
    private static final String ARTIFACT_ID_END_TAG = "</artifactId>";
    private static final String LATEST_BEGIN_TAG = "<latest>";
    private static final String LATEST_END_TAG = "</latest>";
    private static final String URL = "url";
    private static final String NAME_AND_VERSION_SEPARATOR = "nameAndVersionSeparator";
    private static final String NAME_ONLY = "nameOnly";
    private static final String TRUE = "true";
    private static final String VERSION_ONLY = "versionOnly";

    @Override
    public Content generate(Placeholder placeholder) {
        String url = placeholder.getParameters().get(URL);
        if (url == null) {
            throw new SubstitutionRuntimeException("Placeholder parameter '" + URL +
                "' is required, but found 'null'.");
        }
        boolean nameOnly = false;
        if (TRUE.equals(placeholder.getParameters().get(NAME_ONLY))) {
            nameOnly = true;
        }
        boolean versionOnly = false;
        if (TRUE.equals(placeholder.getParameters().get(VERSION_ONLY))) {
            versionOnly = true;
        }
        if (nameOnly && versionOnly) {
            throw new SubstitutionRuntimeException(
                "Only one of the '" + NAME_ONLY + "' and '" + VERSION_ONLY + "' parameters" +
                " can have '" + TRUE + "' value. Placeholder: " + placeholder);
        }
        PomService pomService = PomService.getInstance();
        String xmlString = pomService.loadXmlString(url);
        Content content = new Content();

        String artifactId = pomService.parseTag(xmlString, ARTIFACT_ID_BEGIN_TAG, ARTIFACT_ID_END_TAG);
        if (nameOnly) {
            content.setMarkdownContent(artifactId);
            logger.info("ArtifactId: {}", artifactId);
            return content;
        }

        String latestVersion = pomService.parseTag(xmlString, LATEST_BEGIN_TAG, LATEST_END_TAG);
        if (versionOnly) {
            content.setMarkdownContent(latestVersion);
            logger.info("LatestVersion: {}", latestVersion);
            return content;
        }

        String nameAndVersionSeparator = placeholder.getParameters().get(NAME_AND_VERSION_SEPARATOR);
        if (nameAndVersionSeparator == null) {
            throw new SubstitutionRuntimeException("Placeholder parameter '" + NAME_AND_VERSION_SEPARATOR +
                "' is required, but found 'null'.");
        }

        String result = artifactId + nameAndVersionSeparator + latestVersion;

        content.setMarkdownContent(result);
        logger.info("Result: {}", result);
        return content;
    }
}
