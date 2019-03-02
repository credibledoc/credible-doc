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
    private static final String FOUR_SPACES = "    ";
    private static final String CLASSES_PREFIX = "/com/credibledoc";

    @Override
    public String generate(Placeholder placeholder) {
        ConfigurationService instance = ConfigurationService.getInstance();
        List<String> resources = ResourceService.getInstance()
            .getResources(MarkdownService.MARKDOWN_FILE_EXTENSION, instance.getConfiguration().getTemplatesResource());

        StringBuilder stringBuilder = new StringBuilder();
        for (String resource : resources) {
            if (!resource.startsWith(CLASSES_PREFIX)) {
                stringBuilder.append(FOUR_SPACES).append(resource).append(System.lineSeparator());
            }
        }
        return stringBuilder.toString();
    }
}
