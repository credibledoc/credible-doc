package org.credibledoc.substitution.doc.markdown;

import com.credibledoc.plantuml.svggenerator.SvgGeneratorService;
import com.credibledoc.substitution.core.configuration.Configuration;
import com.credibledoc.substitution.core.configuration.ConfigurationService;
import com.credibledoc.substitution.core.content.ContentGenerator;
import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.core.placeholder.Placeholder;
import com.credibledoc.substitution.core.placeholder.PlaceholderService;
import com.credibledoc.substitution.core.resource.ResourceService;
import com.credibledoc.substitution.core.template.TemplateService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.credibledoc.substitution.doc.json.JsonService;
import org.credibledoc.substitution.doc.report.ReportDocumentCreator;
import org.credibledoc.substitution.doc.reportdocument.ReportDocument;
import org.credibledoc.substitution.doc.reportdocument.ReportDocumentService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This singleton helps to parse *.md templates from the {@link Configuration#getTemplatesResource()} folder, extract
 * contents placed between {@link Configuration#getPlaceholderBegin()} and {@link Configuration#getPlaceholderEnd()}
 * tags, create {@link ReportDocument}s which represent content of the placeholders and generate new documents in the
 * {@link Configuration#getTargetDirectory()} with a new contents instead of placeholders.
 *
 * @author Kyrylo Semenko
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class MarkdownService {
    private static final String SLASH = "/";
    public static final String MARKDOWN_FILE_EXTENSION = ".md";
    private static final String IMAGE_DIRECTORY_NAME = "img";
    private static final String SVG_FILE_EXTENSION = ".svg";
    private static final String SVG_TAG_BEGIN = "![";
    private static final String SVG_TAG_MIDDLE = "](";
    private static final String SVG_TAG_END = "?sanitize=true)";
    public static final String CONTENT_REPLACED = "Content replaced. ";

    /**
     * Cache of beans of the {@link ContentGenerator} type
     * obtained from the Spring container.
     */
    private Map<Class, ContentGenerator> markdownGeneratorsMap = new HashMap<>();

    /**
     * Cache of beans of the {@link ReportDocumentCreator} type
     * obtained from the Spring container.
     */
    private Map<Class, ReportDocumentCreator> reportDocumentCreatorsMap = new HashMap<>();

    /**
     * This map is filled out during preparatory phase, see the
     * {@link #createReportDocumentForPlaceholder(Placeholder, ReportDocumentCreator)} method. And used during
     * generation phase, see the {@link #generateDiagram(Placeholder)} method.
     */
    private Map<Placeholder, ReportDocument> placeholderToReportDocumentMap = new HashMap<>();

    @NonNull
    private final JsonService jsonService;

    @NonNull
    private final ReportDocumentService reportDocumentService;

    @NonNull
    private final List<ContentGenerator> markdownGenerators;

    @NonNull
    private final List<ReportDocumentCreator> reportDocumentCreators;

    private Configuration configuration;

    @PostConstruct
    private void postConstruct() {
        for (ContentGenerator markdownGenerator : markdownGenerators) {
            markdownGeneratorsMap.put(markdownGenerator.getClass(), markdownGenerator);
        }
        for (ReportDocumentCreator reportDocumentCreator : reportDocumentCreators) {
            reportDocumentCreatorsMap.put(reportDocumentCreator.getClass(), reportDocumentCreator);
        }
        configuration = ConfigurationService.getInstance().getConfiguration();
    }

    public void createReportDocuments() {
        String lastTemplateResource = null;
        String lastTemplatePlaceholder = null;
        try {
            String templatesResource = ConfigurationService.getInstance().getConfiguration().getTemplatesResource();
            List<String> resources =
                    ResourceService.getInstance().getResources(MARKDOWN_FILE_EXTENSION, templatesResource);
            log.info("Markdown templates will be loaded from the resources: {}", resources);
            for (String templateResource : resources) {
                lastTemplateResource = templateResource;
                String templateContent = TemplateService.getInstance().getTemplateContent(templateResource);
                List<String> placeholders = parsePlaceholders(templateContent, templateResource);
                int position = 1;
                for (String templatePlaceholder : placeholders) {
                    lastTemplatePlaceholder = templatePlaceholder;
                    Placeholder placeholder = parseJsonFromPlaceholder(templatePlaceholder, templateResource);
                    placeholder.setId(Integer.toString(position++));
                    Class<?> placeholderClass = Class.forName(placeholder.getClassName());
                    if (ReportDocumentCreator.class.isAssignableFrom(placeholderClass)) {
                        ReportDocumentCreator reportDocumentCreator = reportDocumentCreatorsMap.get(placeholderClass);
                        createReportDocumentForPlaceholder(placeholder, reportDocumentCreator);
                    }
                }
            }
            log.info("Report documents created");
        } catch (ClassNotFoundException e) {
            throw new SubstitutionRuntimeException("Class defined in the placeholder cannot be found, " +
                    "templateResource: '" +
                    lastTemplateResource +
                    "', " +
                    "templatePlaceholder: '" +
                    lastTemplatePlaceholder +
                    "'.", e);
        } catch (Exception e) {
            throw new SubstitutionRuntimeException(e);
        }
    }

    /**
     * Prepare relations between {@link Placeholder}s and {@link ReportDocument}s.
     * <p>
     * Create {@link ReportDocument}, see the {@link ReportDocumentCreator#prepareReportDocument()} method.
     * <p>
     * Put the {@link Placeholder} and {@link ReportDocument} to the {@link #placeholderToReportDocumentMap}.
     * <p>
     * Add the {@link ReportDocument} to the {@link ReportDocumentService#getReportDocuments()} list.
     * <p>
     * Add the {@link Placeholder} to the {@link PlaceholderService#getPlaceholders()} list.
     *
     * @param placeholder           for addition
     * @param reportDocumentCreator for addition
     */
    private void createReportDocumentForPlaceholder(Placeholder placeholder, ReportDocumentCreator reportDocumentCreator) {
        ReportDocument reportDocument = reportDocumentCreator.prepareReportDocument();
        placeholderToReportDocumentMap.put(placeholder, reportDocument);
        reportDocumentService.getReportDocuments().add(reportDocument);
        PlaceholderService.getInstance().getPlaceholders().add(placeholder);
    }

    private List<String> parsePlaceholders(String templateContent, String templateResource) {
        List<String> result = new ArrayList<>();
        int index = 0;
        while (true) {
            int beginIndex = templateContent.indexOf(configuration.getPlaceholderBegin(), index);
            if (beginIndex == -1) {
                return result;
            }
            int endIndex = templateContent.indexOf(configuration.getPlaceholderEnd(), beginIndex);
            if (endIndex == -1) {
                endIndex = beginIndex + 30 < templateResource.length() ?
                        beginIndex + 30 : templateResource.length();

                throw new SubstitutionRuntimeException("Cannot find out '" +
                        configuration.getPlaceholderEnd() +
                        "' in the template '" +
                        templateResource +
                        "' which begins from '" +
                        templateContent.substring(beginIndex, endIndex) +
                        "'.");
            }
            result.add(templateContent.substring(beginIndex, endIndex + configuration.getPlaceholderEnd().length()));
            index = endIndex;
        }
    }

    /**
     * Parse {@link Placeholder} from templatePlaceholder
     * @param templatePlaceholder for example
     *                            <pre>&&beginPlaceholder{"className": "org.my.MyContentGenerator"}&&endPlaceholder</pre>
     * @return for example <pre>{"className": "org.my.MyContentGenerator"}</pre>
     */
    private Placeholder parseJsonFromPlaceholder(String templatePlaceholder, String resource) {
        int endIndex = templatePlaceholder.length() - configuration.getPlaceholderEnd().length();
        String json = templatePlaceholder.substring(configuration.getPlaceholderBegin().length(), endIndex);
        Placeholder placeholder = jsonService.readValue(json, Placeholder.class);
        placeholder.setResource(resource);
        return placeholder;
    }

    public void generateMarkdownFromTemplates() {
        try {
            File targetDirectory = new File(configuration.getTargetDirectory());
            if (!targetDirectory.exists()) {
                boolean created = targetDirectory.mkdirs();
                if (!created) {
                    throw new SubstitutionRuntimeException("Cannot create directory '" +
                            targetDirectory.getAbsolutePath() +
                            "'");
                }
                log.info("Target directory created: '{}'", targetDirectory.getAbsolutePath());
            }
            List<String> templateResources =
                    ResourceService.getInstance()
                            .getResources(MARKDOWN_FILE_EXTENSION, configuration.getTemplatesResource());
            for (String templateResource : templateResources) {
                insertMarkdownIntoTemplate(templateResource);
            }
        } catch (Exception e) {
            throw new SubstitutionRuntimeException(e);
        }
    }

    private void insertMarkdownIntoTemplate(String templateResource) throws IOException {
        String templateContent = TemplateService.getInstance().getTemplateContent(templateResource);

        List<String> templatePlaceholders = parsePlaceholders(templateContent, templateResource);

        String replacedContent =
                replacePlaceholdersToGeneratedContent(templateResource, templateContent, templatePlaceholders);

        ResourceService resourceService = ResourceService.getInstance();
        String placeholderResourceRelativePath =
            resourceService.generatePlaceholderResourceRelativePath(templateResource);
        File markdownFile = new File(configuration.getTargetDirectory() + placeholderResourceRelativePath);
        File markdownDirectory = markdownFile.getParentFile();
        createDirectoryIfNotExists(markdownDirectory);
        try (OutputStream outputStream = new FileOutputStream(markdownFile)){
            outputStream.write(replacedContent.getBytes());
        }
    }

    private void createDirectoryIfNotExists(File directory) {
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new SubstitutionRuntimeException("Cannot create a new directory '" +
                        directory.getAbsolutePath() + "'");
            }
            log.info("The new directory created '{}'", directory.getAbsolutePath());
        }
    }

    private String replacePlaceholdersToGeneratedContent(String templateResource,
                                                         String templateContent, List<String> templatePlaceholders) {
        String replacedContent = templateContent;
        int position = 1;
        for (String templatePlaceholder : templatePlaceholders) {
            Placeholder placeholder = parseJsonFromPlaceholder(templatePlaceholder, templateResource);
            placeholder.setId(Integer.toString(position++));
            String contentForReplacement = generateContent(placeholder);
            replacedContent = replacedContent.replace(templatePlaceholder, contentForReplacement);
            String json = jsonService.writeValueAsString(placeholder);
            log.info("{}{}", CONTENT_REPLACED, json);
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
                ContentGenerator markdownGenerator = markdownGeneratorsMap.get(placeholderClass);
                return markdownGenerator.generate(placeholder);
            }
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

                return generateDiagram(nextPlaceholder);
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
     *     <li>Get a {@link ReportDocument} form the {@link #placeholderToReportDocumentMap}</li>
     *     <li>Join lines from the {@link ReportDocument#getCacheLines()} list</li>
     *     <li>And return result of the
     *     {@link #generateSvgFileAndTagForMarkdown(File, File, String, String, String)} method</li>
     * </ul>
     *
     * @param placeholder the state object
     * @return A part of markdown document with link to generated SVG image.
     */
    private String generateDiagram(Placeholder placeholder) {
        ResourceService resourceService = ResourceService.getInstance();
        String placeholderResourceRelativePath =
                resourceService.generatePlaceholderResourceRelativePath(placeholder.getResource());

        File mdFile = new File(configuration.getTargetDirectory() + placeholderResourceRelativePath);
        File directory = mdFile.getParentFile();
        File imageDirectory = new File(directory, IMAGE_DIRECTORY_NAME);
        createDirectoryIfNotExists(imageDirectory);
        ReportDocument reportDocument = placeholderToReportDocumentMap.get(placeholder);
        String plantUml = StringUtils.join(reportDocument.getCacheLines(), System.lineSeparator());

        String placeholderDescription = placeholder.getDescription();
        String nextPlaceholderId = placeholder.getId();

        return generateSvgFileAndTagForMarkdown(
                mdFile,
                imageDirectory,
                plantUml,
                placeholderDescription,
                nextPlaceholderId);
    }

    private String generateSvgFileAndTagForMarkdown(File mdFile,
                                                    File imageDirectory,
                                                    String plantUml,
                                                    String placeholderDescription,
                                                    String nextPlaceholderId) {
        try {
            String svg = SvgGeneratorService.getInstance().generateSvgFromPlantUml(plantUml);

            File svgFile = new File(imageDirectory,
                mdFile.getName() + "_" + nextPlaceholderId + SVG_FILE_EXTENSION);

            try (OutputStream outputStream = new FileOutputStream(svgFile)) {
                outputStream.write(svg.getBytes());
            }
            log.debug("File created: {}", svgFile.getAbsolutePath());
            return SVG_TAG_BEGIN + placeholderDescription + SVG_TAG_MIDDLE + imageDirectory.getName() + SLASH + svgFile.getName() + SVG_TAG_END;
        } catch (Exception e) {
            throw new SubstitutionRuntimeException(e);
        }
    }
}
