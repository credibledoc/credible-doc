package org.credibledoc.substitution.doc.module.substitution.resource;

import com.credibledoc.substitution.configuration.ConfigurationService;
import com.credibledoc.substitution.content.ContentGenerator;
import com.credibledoc.substitution.placeholder.Placeholder;
import com.credibledoc.substitution.resource.ResourceService;
import lombok.RequiredArgsConstructor;
import org.credibledoc.substitution.doc.markdown.MarkdownService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * Generates list of files in resource directory.
 *
 * @author Kyrylo Semenko
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ResourcesListMarkdownGenerator implements ContentGenerator {
    private static final String BULLET_POINT = "* ";
    private static final String CLASSES_PREFIX = "/com/credibledoc";
    private static final String SRC_MAIN_RESOURCES = "src/main/resources";

    @Override
    public String generate(Placeholder placeholder) {
        ConfigurationService configurationService = ConfigurationService.getInstance();
        String templatesResource = configurationService.getConfiguration().getTemplatesResource();
        List<String> resources = ResourceService.getInstance()
            .getResources(MarkdownService.MARKDOWN_FILE_EXTENSION, templatesResource);

        StringBuilder stringBuilder = new StringBuilder();
        for (String resource : resources) {
            if (!resource.startsWith(CLASSES_PREFIX)) {
                String link = generateLink(resource);
                stringBuilder.append(BULLET_POINT).append(link).append(System.lineSeparator());
            }
        }
        return stringBuilder.toString();
    }

    /**
     * @param resourceName for example /template/doc/README.md
     * @return For example [/template/doc/README.md](src/main/resources/template/doc/README.md)
     */
    private String generateLink(String resourceName) {
        return "[" + resourceName + "](" + SRC_MAIN_RESOURCES + resourceName + ")";
    }
}
