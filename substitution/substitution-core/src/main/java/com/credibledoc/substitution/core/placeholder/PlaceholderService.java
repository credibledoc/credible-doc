package com.credibledoc.substitution.core.placeholder;

import com.credibledoc.substitution.core.configuration.Configuration;
import com.credibledoc.substitution.core.context.SubstitutionContext;
import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.core.json.JsonService;
import com.credibledoc.substitution.core.resource.TemplateResource;
import com.credibledoc.substitution.core.template.TemplateService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * This service contains methods for working with {@link Placeholder}s.
 *
 * @author Kyrylo Semenko
 */
public class PlaceholderService {

    /**
     * Singleton.
     */
    private static PlaceholderService instance;

    private PlaceholderService() {
        // empty
    }

    /**
     * @return The {@link PlaceholderService} singleton.
     */
    public static PlaceholderService getInstance() {
        if (instance == null) {
            instance = new PlaceholderService();
        }
        return instance;
    }

    public List<String> parsePlaceholders(TemplateResource templateResource, SubstitutionContext substitutionContext) {
        // TODO Kyrylo Semenko - charset from configuration
        String templateContent =
            TemplateService.getInstance().getTemplateContent(templateResource, StandardCharsets.UTF_8.name());
        Configuration configuration = substitutionContext.getConfiguration();
        List<String> result = new ArrayList<>();
        int index = 0;
        while (true) {
            int beginIndex = templateContent.indexOf(configuration.getPlaceholderBegin(), index);
            if (beginIndex == -1) {
                return result;
            }
            int endIndex = templateContent.indexOf(configuration.getPlaceholderEnd(), beginIndex);
            if (endIndex == -1) {
                endIndex = Math.min(beginIndex + 30, templateContent.length());

                throw new SubstitutionRuntimeException("Cannot find '" +
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
     *
     * @param templatePlaceholder for example
     *                            <pre>{@code
     *                                &&beginPlaceholder{"className": "org.my.MyContentGenerator"}&&endPlaceholder
     *                            }</pre>
     * @param resource            for example <i>/template/markdown/doc/diagrams.md</i>
     * @param substitutionContext the current state
     * @return For example <pre>{"className": "org.my.MyContentGenerator"}</pre>
     */
    public Placeholder parseJsonFromPlaceholder(String templatePlaceholder, TemplateResource resource, SubstitutionContext substitutionContext) {
        Configuration configuration = substitutionContext.getConfiguration();
        int endIndex = templatePlaceholder.length() - configuration.getPlaceholderEnd().length();
        String json = templatePlaceholder.substring(configuration.getPlaceholderBegin().length(), endIndex);
        Placeholder placeholder = JsonService.getInstance().readValue(json, Placeholder.class);
        placeholder.setResource(resource);
        return placeholder;
    }

    /**
     * Transform JSON to object.
     * @param json a {@link Placeholder} in a JSON format
     * @return Parsed {@link Placeholder}
     */
    public Placeholder readPlaceholderFromJson(String json) {
        return JsonService.getInstance().readValue(json, Placeholder.class);
    }

    /**
     * Create JSON from the {@link Placeholder}
     * @param placeholder the source of JSON
     * @return JSON
     */
    public String writePlaceholderToJson(Placeholder placeholder) {
        return JsonService.getInstance().writeValueAsString(placeholder);
    }
}
