package com.credibledoc.substitution.reporting.markdown;

import com.credibledoc.plantuml.exception.PlantumlRuntimeException;
import com.credibledoc.plantuml.svggenerator.SvgGeneratorService;
import com.credibledoc.substitution.core.configuration.Configuration;
import com.credibledoc.substitution.core.configuration.ConfigurationService;
import com.credibledoc.substitution.core.content.Content;
import com.credibledoc.substitution.core.content.ContentGenerator;
import com.credibledoc.substitution.core.content.ContentGeneratorService;
import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.core.placeholder.Placeholder;
import com.credibledoc.substitution.core.placeholder.PlaceholderService;
import com.credibledoc.substitution.core.resource.ResourceService;
import com.credibledoc.substitution.core.template.TemplateService;
import com.credibledoc.substitution.reporting.placeholder.PlaceholderToReportDocumentService;
import com.credibledoc.substitution.reporting.reportdocument.ReportDocument;
import com.credibledoc.substitution.reporting.reportdocument.creator.ReportDocumentCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * This singleton helps to parse *.md templates from the {@link Configuration#getTemplatesResource()} folder, extract
 * contents placed between {@link Configuration#getPlaceholderBegin()} and {@link Configuration#getPlaceholderEnd()}
 * tags, create {@link ReportDocument}s which represent content of the placeholders and generate new documents in the
 * {@link Configuration#getTargetDirectory()} with a new contents instead of placeholders.
 *
 * @author Kyrylo Semenko
 */
public class MarkdownService {
    private static final Logger logger = LoggerFactory.getLogger(MarkdownService.class);
    private static final String SLASH = "/";
    public static final String MARKDOWN_FILE_EXTENSION = ".md";
    private static final String IMAGE_DIRECTORY_NAME = "img";
    private static final String SVG_FILE_EXTENSION = ".svg";
    private static final String SVG_TAG_BEGIN = "![";
    private static final String SVG_TAG_MIDDLE = "](";
    private static final String SVG_TAG_END = "?sanitize=true)";
    public static final String CONTENT_REPLACED = "Content replaced. ";
    private static final String SYNTAX_ERROR_GENERATED_KEYWORD = "Syntax Error?";
    private static final String IGNORE_SYNTAX_ERROR_PLACEHOLDER_PARAMETER = "ignoreSyntaxError";

    private Configuration configuration;

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
            instance.postConstruct();
        }
        return instance;
    }

    private void postConstruct() {
        configuration = ConfigurationService.getInstance().getConfiguration();
    }

    /**
     * Find out a target directory where a generated documents will be placed, iterate template resources and
     * for each template resource generate content for its {@link Placeholder}s. Then replace the {@link Placeholder}s
     * with generated content. And finally write out generated documents to files.
     */
    public void generateContentFromTemplates() {
        try {
            File targetDirectory = new File(configuration.getTargetDirectory());
            if (!targetDirectory.exists()) {
                boolean created = targetDirectory.mkdirs();
                if (!created) {
                    throw new SubstitutionRuntimeException("Cannot create directory '" +
                        targetDirectory.getAbsolutePath() +
                        "'");
                }
                logger.info("Target directory created: '{}'", targetDirectory.getAbsolutePath());
            }
            List<String> templateResources =
                ResourceService.getInstance()
                    .getResources(MARKDOWN_FILE_EXTENSION, configuration.getTemplatesResource());
            for (String templateResource : templateResources) {
                insertContentIntoTemplate(templateResource);
            }
        } catch (Exception exception) {
            throw new SubstitutionRuntimeException(exception);
        }
    }

    /**
     * Load template from the templateResource, collect {@link Placeholder}s from the template, replace the
     * {@link Placeholder}s with generated content and write the template with generated content to a target file.
     *
     * @param templateResource source of a template, for example <i>/template/markdown/doc/diagrams.md</i>
     * @throws IOException in case of problem with writing of template with generated content to a target file.
     */
    private void insertContentIntoTemplate(String templateResource) throws IOException {
        String templateContent = TemplateService.getInstance().getTemplateContent(templateResource);

        List<String> templatePlaceholders =
            PlaceholderService.getInstance().parsePlaceholders(templateContent, templateResource);

        String replacedContent =
                replacePlaceholdersWithGeneratedContent(templateResource, templateContent, templatePlaceholders);

        ResourceService resourceService = ResourceService.getInstance();
        String placeholderResourceRelativePath =
            resourceService.generatePlaceholderResourceRelativePath(templateResource);
        File generatedFile = new File(configuration.getTargetDirectory() + placeholderResourceRelativePath);
        File generatedFileDirectory = generatedFile.getParentFile();
        createDirectoryIfNotExists(generatedFileDirectory);
        try (OutputStream outputStream = new FileOutputStream(generatedFile)){
            outputStream.write(replacedContent.getBytes());
        }
        logger.info("File is generated '{}'", generatedFile.getAbsolutePath());
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

    private String replacePlaceholdersWithGeneratedContent(String templateResource,
                                                           String templateContent, List<String> templatePlaceholders) {
        String replacedContent = templateContent;
        int position = 1;
        for (String templatePlaceholder : templatePlaceholders) {
            Placeholder placeholder =
                PlaceholderService.getInstance().parseJsonFromPlaceholder(templatePlaceholder, templateResource);
            placeholder.setId(Integer.toString(position++));
            String contentForReplacement = generateContent(placeholder);
            replacedContent = replacedContent.replace(templatePlaceholder, contentForReplacement);
            String json = PlaceholderService.getInstance().writePlaceholderToJson(placeholder);
            logger.info("{}{}", CONTENT_REPLACED, json);
        }
        return replacedContent;
    }

    /**
     * Depends on {@link Placeholder#getClassName()} use its instance for generation
     * of placeholder content and replace it.
     *
     * @param placeholder contains a {@link Placeholder#getClassName()}
     * @return in case of {@link ReportDocumentCreator} return for example
     * <pre>![Diagram generated by this application](img/launching.md_1.svg?sanitize=true)</pre>
     * tag.
     * <p>
     * In case of {@link ContentGenerator} return a markdown code.
     */
    private String generateContent(Placeholder placeholder) {
        try {
            Class placeholderClass = Class.forName(placeholder.getClassName());
            if (ReportDocumentCreator.class.isAssignableFrom(placeholderClass)) {
                String generatedTag = findPlaceholderAndGenerateDiagram(placeholder);
                if (generatedTag != null) {
                    return generatedTag;
                }
            } else if (ContentGenerator.class.isAssignableFrom(placeholderClass)) {
                @SuppressWarnings("unchecked")
                ContentGenerator markdownGenerator =
                    ContentGeneratorService.getInstance().getContentGenerator(placeholderClass);
                Content content = markdownGenerator.generate(placeholder);
                if (content.getPlantUmlContent() != null) {
                    String linkToDiagram = generateDiagram(placeholder, content.getPlantUmlContent());
                    return linkToDiagram + content.getMarkdownContent();
                } else {
                    return content.getMarkdownContent();
                }
            }
        } catch (ClassNotFoundException classNotFoundException) {
            throw new SubstitutionRuntimeException("PlaceholderClass cannot be found." +
                " Placeholder className: '" + placeholder.getClassName() +
                "', placeholder: " + placeholder, classNotFoundException);
        } catch (Exception e) {
            throw new SubstitutionRuntimeException(e);
        }
        throw new SubstitutionRuntimeException("Cannot find out generated content " +
                "for the placeholder id: " + placeholder.getId() +
                ", placeholder resource: '" + placeholder.getResource() + "'");
    }

    private String findPlaceholderAndGenerateDiagram(Placeholder placeholder) {
        for (Placeholder nextPlaceholder : PlaceholderService.getInstance().getPlaceholders()) {
            if (nextPlaceholder.getResource().equals(placeholder.getResource()) &&
                    nextPlaceholder.getId().equals(placeholder.getId())) {

                return generateDiagram(nextPlaceholder, null);
            }
        }
        return null;
    }

    /**
     * Generate SVG image file and a link to this file.
     * <ul>
     *     <li>Load template from the {@link Placeholder#getResource()} field</li>
     *     <li>Create a new file from the template in the {@link Configuration#getTargetDirectory()} directory</li>
     *     <li>Create a new {@link #IMAGE_DIRECTORY_NAME} directory</li>
     *     <li>Get a {@link ReportDocument} form the {@link PlaceholderToReportDocumentService}</li>
     *     <li>Join lines from the {@link ReportDocument#getCacheLines()} list</li>
     *     <li>And return result of the
     *     {@link #generateSvgFileAndTagForMarkdown(File, File, String, Placeholder)} method</li>
     * </ul>
     *
     * @param placeholder the state object
     * @param plantUml PlantUML source notation. If the value is 'null', the value will be obtained from
     *                 {@link ReportDocument} which is stored in
     *                 {@link PlaceholderToReportDocumentService#getReportDocument(Placeholder)}.
     * @return A part of markdown document with link to generated SVG image.
     */
    private String generateDiagram(Placeholder placeholder, String plantUml) {
        ResourceService resourceService = ResourceService.getInstance();
        String placeholderResourceRelativePath =
                resourceService.generatePlaceholderResourceRelativePath(placeholder.getResource());

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

        return generateSvgFileAndTagForMarkdown(
                mdFile,
                imageDirectory,
                plantUml,
                placeholder);
    }

    private String generateSvgFileAndTagForMarkdown(File mdFile,
                                                    File imageDirectory,
                                                    String plantUml,
                                                    Placeholder placeholder) {
        try {
            String svg = SvgGeneratorService.getInstance().generateSvgFromPlantUml(plantUml);

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
            return SVG_TAG_BEGIN + placeholder.getDescription() + SVG_TAG_MIDDLE + imageDirectory.getName() +
                SLASH + svgFile.getName() + SVG_TAG_END;
        } catch (Exception e) {
            throw new SubstitutionRuntimeException(e);
        }
    }
}
