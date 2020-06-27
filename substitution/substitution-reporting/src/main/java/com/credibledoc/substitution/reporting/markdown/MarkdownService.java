package com.credibledoc.substitution.reporting.markdown;

import com.credibledoc.plantuml.exception.PlantumlRuntimeException;
import com.credibledoc.plantuml.svggenerator.SvgGeneratorService;
import com.credibledoc.substitution.core.configuration.Configuration;
import com.credibledoc.substitution.core.context.SubstitutionContext;
import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.core.placeholder.Placeholder;
import com.credibledoc.substitution.core.resource.ResourceService;
import com.credibledoc.substitution.reporting.placeholder.PlaceholderToReportDocumentService;
import com.credibledoc.substitution.core.replacement.ReplacementType;
import com.credibledoc.substitution.reporting.reportdocument.ReportDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * This singleton helps to parse templates from the {@link Configuration#getTemplatesResource()} folder, extract
 * {@link Placeholder}s between {@link Configuration#getPlaceholderBegin()} and {@link Configuration#getPlaceholderEnd()}
 * tags, create {@link ReportDocument}s which represent a content of the placeholders and generate new documents in the
 * {@link Configuration#getTargetDirectory()} with the new content instead of placeholders.
 *
 * @author Kyrylo Semenko
 */
public class MarkdownService {
    private static final Logger logger = LoggerFactory.getLogger(MarkdownService.class);
    private static final String SLASH = "/";
    private static final String IMAGE_DIRECTORY_NAME = "img";
    private static final String SVG_FILE_EXTENSION = ".svg";
    private static final String SVG_TAG_BEGIN = "![";
    private static final String SVG_TAG_MIDDLE = "](";
    private static final String SVG_TAG_END = "?sanitize=true)";
    private static final String SYNTAX_ERROR_GENERATED_KEYWORD = "Syntax Error?";
    private static final String IGNORE_SYNTAX_ERROR_PLACEHOLDER_PARAMETER = "ignoreSyntaxError";

    /**
     * Singleton.
     */
    private static MarkdownService instance;

    /**
     * @return The {@link MarkdownService} singleton.
     */
    public static MarkdownService getInstance() {
        if (instance == null) {
            instance = new MarkdownService();
        }
        return instance;
    }

    /**
     * Generate SVG image file and a link to the file.
     * <ul>
     *     <li>Load template from the {@link Placeholder#getResource()} field</li>
     *     <li>Create a new file from the template in the {@link Configuration#getTargetDirectory()} directory</li>
     *     <li>Create a new {@link #IMAGE_DIRECTORY_NAME} directory</li>
     *     <li>Get a {@link ReportDocument} form the {@link PlaceholderToReportDocumentService}</li>
     *     <li>Join lines from the {@link ReportDocument#getCacheLines()} list</li>
     *     <li>And return result of the
     *     {@link #generateSvgFileAndTagForMarkdown(File, File, String, Placeholder, boolean)} method</li>
     * </ul>
     *
     * @param placeholder the state object
     * @param plantUml PlantUML source notation. If the value is 'null', the value will be obtained from
     *                 {@link ReportDocument} which is stored in
     *                 {@link PlaceholderToReportDocumentService#getReportDocument(Placeholder)}.
     * @param substitutionContext the current state
     * @return A part of markdown document with link to generated SVG image.
     */
    public String generateDiagram(Placeholder placeholder, String plantUml, SubstitutionContext substitutionContext) {
        ResourceService resourceService = ResourceService.getInstance();
        String placeholderResourceRelativePath =
                resourceService.generatePlaceholderResourceRelativePath(placeholder.getResource(), substitutionContext);

        Configuration configuration = substitutionContext.getConfiguration();
        File mdFile = new File(configuration.getTargetDirectory() + placeholderResourceRelativePath);
        File directory = mdFile.getParentFile();
        File imageDirectory = new File(directory, IMAGE_DIRECTORY_NAME);
        createDirectoryIfNotExists(imageDirectory);
        if (plantUml == null) {
            ReportDocument reportDocument = PlaceholderToReportDocumentService.getInstance()
                .getReportDocument(placeholder);
            plantUml = String.join(System.lineSeparator(), reportDocument.getCacheLines());
        }
        String placeholderDescription = placeholder.getDescription();

        if (plantUml.isEmpty()) {
            return "Cannot generate diagram because source content not found. " +
                "PlaceholderDescription: '" + placeholderDescription + "'.";
        }

        boolean replaceFilterId = "true".equals(substitutionContext.getConfiguration().getReplaceFilterId());
        return generateSvgFileAndTagForMarkdown(
                mdFile,
                imageDirectory,
                plantUml,
                placeholder,
                replaceFilterId
            );
    }

    private String generateSvgFileAndTagForMarkdown(File mdFile,
                                                    File imageDirectory,
                                                    String plantUml,
                                                    Placeholder placeholder,
                                                    boolean replaceFilterId) {
        try {
            String svg = SvgGeneratorService.getInstance().generateSvgFromPlantUml(plantUml);
            
            if (replaceFilterId) {
                svg = replaceFilterId(svg);
            }

            File svgFile = new File(imageDirectory,
                mdFile.getName() + "_" + placeholder.getId() + SVG_FILE_EXTENSION);

            try (OutputStream outputStream = new FileOutputStream(svgFile)) {
                outputStream.write(svg.getBytes());
            }
            logger.debug("File created: {}", svgFile.getAbsolutePath());
            
            boolean ignoreSyntaxError = placeholder.getParameters()
                .containsKey(IGNORE_SYNTAX_ERROR_PLACEHOLDER_PARAMETER) &&
                placeholder.getParameters().get(IGNORE_SYNTAX_ERROR_PLACEHOLDER_PARAMETER).equals("false");
            
            if (!ignoreSyntaxError && svg.contains(SYNTAX_ERROR_GENERATED_KEYWORD)) {
                throw new PlantumlRuntimeException("SVG contains '" + SYNTAX_ERROR_GENERATED_KEYWORD
                    + "' substring. SVG: '" + svg
                    + "'. " + System.lineSeparator()
                    + placeholder);
            }
            if (placeholder.getParameters().get(ReplacementType.TARGET_FORMAT) != null) {
                ReplacementType replacementType =
                    ReplacementType.valueOf(placeholder.getParameters().get(ReplacementType.TARGET_FORMAT));
                if (ReplacementType.HTML_EMBEDDED == replacementType) {
                    return svg;
                }
                throw new SubstitutionRuntimeException("Unknown " + ReplacementType.class.getSimpleName() + " " +
                    "value " + replacementType);
            } else {
                return SVG_TAG_BEGIN + placeholder.getDescription() + SVG_TAG_MIDDLE + imageDirectory.getName() +
                    SLASH + svgFile.getName() + SVG_TAG_END;
            }
        } catch (Exception e) {
            throw new SubstitutionRuntimeException(e);
        }
    }

    /**
     * Replace all occurrences of the generated ids with constants. Example of filter line:
     * <pre>{@code
     * <filter height="300%" id="f10gnta8ifhhre" width="300%" x="-1" y="-1">
     * }</pre>
     * @param svg for replacing
     * @return The svg with replaced ids, for example id="f10gnta8ifhhre" will be replaced with id="1"
     */
    private String replaceFilterId(String svg) {
        int beginFilter = svg.indexOf("<filter ");
        if (beginFilter == -1) {
            return svg;
        }
        String beginIdPattern = " id=\"";
        int beginId = svg.indexOf(beginIdPattern, beginFilter);
        if (beginId == -1) {
            return svg;
        }
        int endId = svg.indexOf("\" ", beginId + beginIdPattern.length());
        String oldId = svg.substring(beginId + beginIdPattern.length(), endId);

        return svg.replace(oldId, "1");
    }

    private void createDirectoryIfNotExists(File directory) {
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new SubstitutionRuntimeException("Cannot create a new directory '" +
                    directory.getAbsolutePath() + "'");
            }
            logger.info("The new directory created '{}'", directory.getAbsolutePath());
        }
    }
}
