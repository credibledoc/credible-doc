package com.credibledoc.substitution.content.generator.resource;

import com.credibledoc.substitution.core.content.Content;
import com.credibledoc.substitution.core.content.ContentGenerator;
import com.credibledoc.substitution.core.context.SubstitutionContext;
import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.core.placeholder.Placeholder;
import com.credibledoc.substitution.core.resource.ResourceService;
import com.credibledoc.substitution.core.resource.ResourceType;
import com.credibledoc.substitution.core.resource.TemplateResource;

import java.io.File;
import java.util.List;

/**
 * Generates list of files in resource directory.
 * <p>
 * Optional parameter {@link #ENDS_WITH} filters resource names which ends with a defined string.
 * If the {@link #ENDS_WITH} parameter is not defined, all resource names will be returned.
 * <p>
 * Example of usage:
 * <pre>{@code
 * &&beginPlaceholder {
 *     "className": "com.credibledoc.substitution.content.generator.resource.ResourcesListMarkdownGenerator",
 *     "description": "List of templates used for the documentation.",
 *     "parameters": {"endsWith": ".md"}
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
    private static final String SRC_MAIN_RESOURCES = "src/main/resources";
    private static final String ENDS_WITH = "endsWith";
    private static final String SLASH = "/";

    @Override
    public Content generate(Placeholder placeholder, SubstitutionContext substitutionContext) {
        String templatesResource = substitutionContext.getConfiguration().getTemplatesResource();
        String filterEndsWith = placeholder.getParameters().get(ENDS_WITH);
        List<TemplateResource> resources = ResourceService.getInstance()
            .getResources(filterEndsWith, templatesResource);

        StringBuilder stringBuilder = new StringBuilder();
        for (TemplateResource templateResource : resources) {
            String link = generateLink(templateResource);
            stringBuilder.append(BULLET_POINT).append(link).append(System.lineSeparator());
        }
        Content content = new Content();
        content.setMarkdownContent(stringBuilder.toString());
        return content;
    }

    /**
     * @param templateResource for example /template/doc/README.md
     * @return For example [/template/doc/README.md](src/main/resources/template/doc/README.md)
     */
    private String generateLink(TemplateResource templateResource) {
        if (templateResource.getType() == ResourceType.FILE) {
            File file = templateResource.getFile();
            String parentPath = file.getParentFile().getAbsolutePath();
            String urlPath = parentPath.replaceAll("\\\\", SLASH);
            if (urlPath.contains(SRC_MAIN_RESOURCES)) {
                int beginIndex = urlPath.indexOf(SRC_MAIN_RESOURCES);
                String path = urlPath.substring(beginIndex) + SLASH + file.getName();
                return "[" + path.substring(SRC_MAIN_RESOURCES.length()) + "](" + path + ")";
            }
            return file.getAbsolutePath();
        }
        if (templateResource.getType() == ResourceType.CLASSPATH) {
            return "[" + templateResource.getPath() + "](" + SRC_MAIN_RESOURCES + templateResource.getPath() + ")";
        }
        throw new SubstitutionRuntimeException("Unknown ResourceType " + templateResource.getType());
    }
}
