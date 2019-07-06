package com.credibledoc.substitution.content.generator.resource;

import com.credibledoc.substitution.core.configuration.ConfigurationService;
import com.credibledoc.substitution.core.content.Content;
import com.credibledoc.substitution.core.content.ContentGenerator;
import com.credibledoc.substitution.core.placeholder.Placeholder;
import com.credibledoc.substitution.core.resource.ResourceService;

import java.util.List;

/**
 * Generates list of files in resource directory.
 * <p>
 * Optional parameter {@link #END_WITH} will return resource names which end with defined string.
 * If this {@link #END_WITH} parameter is not defined, all resource names will be returned.
 * <p>
 * Example of usage:
 * <pre>{@code
 * &&beginPlaceholder {
 *     "className": "com.credibledoc.substitution.content.generator.resource.ResourcesListMarkdownGenerator",
 *     "description": "List of resources from classpath of the credible-doc-generator application.",
 *     "parameters": {"endWith": ".md"}
 * } &&endPlaceholder
 * }</pre>
 * <p>
 * Example of result:
 * <pre>
 *  * [/template/markdown/doc/log-combiner-core/usage.md](src/main/resources/template/markdown/doc/log-combiner-core/usage.md)
 *  * [/template/markdown/doc/credible-doc-generator/README.md](src/main/resources/template/markdown/doc/credible-doc-generator/README.md)
 * </pre>
 *
 * @author Kyrylo Semenko
 */
public class ResourcesListMarkdownGenerator implements ContentGenerator {
    private static final String BULLET_POINT = "* ";
    private static final String CLASSES_PREFIX = "/com/credibledoc";
    private static final String SRC_MAIN_RESOURCES = "src/main/resources";
    private static final String END_WITH = "endWith";

    @Override
    public Content generate(Placeholder placeholder) {
        ConfigurationService configurationService = ConfigurationService.getInstance();
        String templatesResource = configurationService.getConfiguration().getTemplatesResource();
        String filterEndsWith = placeholder.getParameters().get(END_WITH);
        List<String> resources = ResourceService.getInstance()
            .getResources(filterEndsWith, templatesResource);

        StringBuilder stringBuilder = new StringBuilder();
        for (String resource : resources) {
            if (!resource.startsWith(CLASSES_PREFIX)) {
                String link = generateLink(resource);
                stringBuilder.append(BULLET_POINT).append(link).append(System.lineSeparator());
            }
        }
        Content content = new Content();
        content.setMarkdownContent(stringBuilder.toString());
        return content;
    }

    /**
     * @param resourceName for example /template/doc/README.md
     * @return For example [/template/doc/README.md](src/main/resources/template/doc/README.md)
     */
    private String generateLink(String resourceName) {
        return "[" + resourceName + "](" + SRC_MAIN_RESOURCES + resourceName + ")";
    }
}
