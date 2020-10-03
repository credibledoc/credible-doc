package com.credibledoc.substitution.reporting.replacement;

import com.credibledoc.combiner.file.FileService;
import com.credibledoc.substitution.core.configuration.Configuration;
import com.credibledoc.substitution.core.content.Content;
import com.credibledoc.substitution.core.content.ContentGenerator;
import com.credibledoc.substitution.core.content.ContentGeneratorService;
import com.credibledoc.substitution.core.context.SubstitutionContext;
import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.core.pair.Pair;
import com.credibledoc.substitution.core.placeholder.Placeholder;
import com.credibledoc.substitution.core.placeholder.PlaceholderService;
import com.credibledoc.substitution.core.resource.ResourceService;
import com.credibledoc.substitution.core.resource.ResourceType;
import com.credibledoc.substitution.core.resource.TemplateResource;
import com.credibledoc.substitution.core.template.TemplateService;
import com.credibledoc.substitution.core.tracking.Trackable;
import com.credibledoc.substitution.reporting.markdown.MarkdownService;
import com.credibledoc.substitution.reporting.reportdocument.creator.ReportDocumentCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class ReplacementService {
    private static final Logger logger = LoggerFactory.getLogger(ReplacementService.class);
    
    public static final String CONTENT_REPLACED = "Content replaced. ";

    /**
     * Singleton.
     */
    private static ReplacementService instance;

    /**
     * @return The {@link ReplacementService} singleton.
     */
    public static ReplacementService getInstance() {
        if (instance == null) {
            instance = new ReplacementService();
        }
        return instance;
    }

    /**
     * Load template from the {@link TemplateResource}, collect {@link Placeholder}s from the template, replace the
     * {@link Placeholder}s with generated content and write the template with generated content to a target file.
     *
     * @param templateResource source of a template, for example <i>/template/markdown/doc/diagrams.md</i>
     * @param substitutionContext the current state
     */
    public void insertContentIntoTemplate(TemplateResource templateResource, SubstitutionContext substitutionContext) {
        try {
            List<String> templatePlaceholders =
                PlaceholderService.getInstance().parsePlaceholders(templateResource, substitutionContext);

            File generatedFile = getTargetFile(templateResource, substitutionContext);

            if (templatePlaceholders.isEmpty()) {
                Files.copy(templateResource.getFile().toPath(), generatedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return;
            }

            String replacedContent =
                replaceContent(templateResource, templatePlaceholders, substitutionContext);

            try (OutputStream outputStream = new FileOutputStream(generatedFile)){
                outputStream.write(replacedContent.getBytes());
            }
            logger.trace("File generated. '{}'", generatedFile.getAbsolutePath());
        } catch (Exception exception) {
            throw new SubstitutionRuntimeException(exception);
        }
    }

    public File getTargetFile(TemplateResource templateResource, SubstitutionContext substitutionContext) {
        ResourceService resourceService = ResourceService.getInstance();
        String placeholderResourceRelativePath =
            resourceService.generatePlaceholderResourceRelativePath(templateResource, substitutionContext);
        Configuration configuration = substitutionContext.getConfiguration();
        File generatedFile = new File(configuration.getTargetDirectory() + placeholderResourceRelativePath);
        File generatedFileDirectory = generatedFile.getParentFile();
        createDirectoryIfNotExists(generatedFileDirectory);
        return generatedFile;
    }

    private String replaceContent(TemplateResource templateResource,
                                  List<String> templatePlaceholders,
                                  SubstitutionContext substitutionContext) {
        String replacedContent =
            TemplateService.getInstance().getTemplateContent(templateResource, StandardCharsets.UTF_8.name());
        String lineEnding = FileService.findLineEnding(replacedContent);
        int position = 1;
        for (String templatePlaceholder : templatePlaceholders) {
            Placeholder placeholder = PlaceholderService.getInstance()
                .parseJsonFromPlaceholder(templatePlaceholder, templateResource, substitutionContext);
            placeholder.setId(Integer.toString(position++));
            String contentForReplacement = generateContent(placeholder, substitutionContext);
            String normalizedContent = contentForReplacement.replaceAll(FileService.ANY_LINE_ENDING, lineEnding);
            replacedContent = replacedContent.replace(templatePlaceholder, normalizedContent);
            String json = PlaceholderService.getInstance().writePlaceholderToJson(placeholder);
            logger.trace("{}{}", CONTENT_REPLACED, json);
        }
        return replacedContent;
    }

    /**
     * Depends on {@link Placeholder#getClassName()} use its instance for generation
     * of placeholder content and replace it.
     *
     * @param placeholder contains a {@link Placeholder#getClassName()}
     * @param substitutionContext the current state
     * @return in case of {@link ReportDocumentCreator} return for example
     * <pre>![Diagram generated by this application](img/launching.md_1.svg?sanitize=true)</pre>
     * tag.
     * <p>
     * In case of {@link ContentGenerator} return a generated content.
     */
    @SuppressWarnings("unchecked")
    private String generateContent(Placeholder placeholder, SubstitutionContext substitutionContext) {
        try {
            Class<?> placeholderClass = Class.forName(placeholder.getClassName());
            if (ReportDocumentCreator.class.isAssignableFrom(placeholderClass)) {
                String generatedTag = findPlaceholderAndGenerateDiagram(placeholder, substitutionContext);
                if (generatedTag != null) {
                    return generatedTag;
                }
            } else if (ContentGenerator.class.isAssignableFrom(placeholderClass)) {
                return processContentGenerator(placeholder,
                    substitutionContext,
                    (Class<? extends ContentGenerator>) placeholderClass);
            }
        } catch (ClassNotFoundException classNotFoundException) {
            throw new SubstitutionRuntimeException("PlaceholderClass cannot be found." +
                " Placeholder className: '" + placeholder.getClassName() +
                "', placeholder: " + placeholder, classNotFoundException);
        }
        throw new SubstitutionRuntimeException("Cannot generate a content " +
            "for the placeholder: " + placeholder + "'");
    }

    private String processContentGenerator(Placeholder placeholder, SubstitutionContext substitutionContext,
                                           Class<? extends ContentGenerator> placeholderClass) {
        ContentGenerator contentGenerator =
            ContentGeneratorService.getInstance().getContentGenerator(placeholderClass);

        Content content = contentGenerator.generate(placeholder, substitutionContext);

        if (contentGenerator instanceof Trackable && !ResourceService.getInstance().isLocatedInJar()) {
            Trackable trackable = (Trackable) contentGenerator;
            List<Path> paths = trackable.getFragmentPaths();
            for (Path path : paths) {
                Pair<Path, Path> pair = new Pair<>(path, placeholder.getResource().getFile().toPath());
                List<Pair<Path, Path>> pairs = substitutionContext.getTrackableRepository().getPairs();
                if (!pairs.contains(pair)) {
                    pairs.add(pair);
                }
            }
        }

        if (content.getPlantUmlContent() != null) {
            String svg = MarkdownService.getInstance()
                .generateDiagram(placeholder, content.getPlantUmlContent(), substitutionContext);
            if (content.getMarkdownContent() != null) {
                return svg + content.getMarkdownContent();
            }
            return svg;
        } else {
            return content.getMarkdownContent();
        }
    }

    private String findPlaceholderAndGenerateDiagram(Placeholder placeholder, SubstitutionContext substitutionContext) {
        for (Placeholder nextPlaceholder : substitutionContext.getPlaceholderRepository().getPlaceholders()) {
            if (nextPlaceholder.getResource().equals(placeholder.getResource()) &&
                nextPlaceholder.getId().equals(placeholder.getId())) {

                return MarkdownService.getInstance()
                    .generateDiagram(nextPlaceholder, null, substitutionContext);
            }
        }
        return null;
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

    /**
     * Call the {@link #copyResourcesToTargetDirectory(SubstitutionContext)} method and then for every
     * {@link TemplateResource} call the {@link #insertContentIntoTemplate(TemplateResource, SubstitutionContext)} method.
     * @param substitutionContext the current state.
     */
    public void replace(SubstitutionContext substitutionContext) {
        List<TemplateResource> templateResources = copyResourcesToTargetDirectory(substitutionContext);
        for (TemplateResource templateResource : templateResources) {
            insertContentIntoTemplate(templateResource, substitutionContext);
        }
    }

    public List<TemplateResource> copyResourcesToTargetDirectory(SubstitutionContext substitutionContext) {
        try {
            List<TemplateResource> result = new ArrayList<>();
            Configuration configuration = substitutionContext.getConfiguration();
            ResourceService resourceService = ResourceService.getInstance();
            List<TemplateResource> allResources =
                resourceService.getResources(null, configuration.getTemplatesResource());
            TemplateService templateService = TemplateService.getInstance();
            PlaceholderService placeholderService = PlaceholderService.getInstance();
            logger.info("Templates will be copied to the target directory. Templates number: {}. " +
                "Target directory: '{}'.", allResources.size(), configuration.getTargetDirectory());
            for (TemplateResource templateResource : allResources) {
                List<String> placeholders = placeholderService.parsePlaceholders(templateResource, substitutionContext);
                if (!placeholders.isEmpty()) {
                    result.add(templateResource);
                }
                if (templateResource.getType() == ResourceType.FILE) {
                    String targetFileRelativePath =
                        resourceService.generatePlaceholderResourceRelativePath(templateResource, substitutionContext);
                    String targetFileAbsolutePath = configuration.getTargetDirectory() + targetFileRelativePath;
                    logger.trace("Resource will be copied to file. Resource: '{}'. TargetFileAbsolutePath: '{}'",
                        templateResource, targetFileAbsolutePath);
                    Path targetPath = Paths.get(targetFileAbsolutePath);
                    Files.createDirectories(targetPath.getParent());
                    Files.copy(templateResource.getFile().toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                } else if (templateResource.getType() == ResourceType.CLASSPATH) {
                    if (containsDotInName(templateResource.getPath())) {
                        String targetFileRelativePath =
                            resourceService.generatePlaceholderResourceRelativePath(templateResource,
                                substitutionContext);
                        String targetFileAbsolutePath = configuration.getTargetDirectory() + targetFileRelativePath;
                        logger.trace("Resource will be copied to file. Resource: '{}'. TargetFileAbsolutePath: '{}'",
                            templateResource, targetFileAbsolutePath);
                        File file = templateService.exportResource(templateResource.getPath(), targetFileAbsolutePath);
                        logger.trace("Resource copied to file: '{}'", file.getAbsolutePath());
                    }
                } else {
                    throw new IllegalArgumentException("Unknown ResourceType " + templateResource.getType());
                }
            }
            return result;
        } catch (Exception e) {
            throw new SubstitutionRuntimeException(e);
        }
    }

    /**
     * @param resource for example '/template/markdown/' is a directory, and '/template/markdown/README.md' is a file.
     * @return 'False' if this resource is directory
     */
    private boolean containsDotInName(String resource) {
        int index = resource.lastIndexOf('/');
        if (index == -1) {
            index = 0;
        }
        String fileName = resource.substring(index);
        return fileName.contains(".");
    }
}
