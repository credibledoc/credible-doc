package org.credibledoc.substitution.doc.placeholder;

import com.credibledoc.substitution.core.configuration.Configuration;
import com.credibledoc.substitution.core.configuration.ConfigurationService;
import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.core.placeholder.Placeholder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.credibledoc.substitution.doc.json.JsonService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Helps to parse JSON to a {@link com.credibledoc.substitution.core.placeholder.Placeholder}
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlaceholderParser {

    @NonNull
    private final JsonService jsonService;

    public List<String> parsePlaceholders(String templateContent, String templateResource) {
        Configuration configuration = ConfigurationService.getInstance().getConfiguration();
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
    public Placeholder parseJsonFromPlaceholder(String templatePlaceholder, String resource) {
        Configuration configuration = ConfigurationService.getInstance().getConfiguration();
        int endIndex = templatePlaceholder.length() - configuration.getPlaceholderEnd().length();
        String json = templatePlaceholder.substring(configuration.getPlaceholderBegin().length(), endIndex);
        Placeholder placeholder = jsonService.readValue(json, Placeholder.class);
        placeholder.setResource(resource);
        return placeholder;
    }

    /**
     * Transform JSON to object.
     * @param json a {@link Placeholder} in a JSON format
     * @return Parsed {@link Placeholder}
     */
    public Placeholder readPlaceholderFromJson(String json) {
        return jsonService.readValue(json, Placeholder.class);
    }

    /**
     * Create JSON from the {@link Placeholder}
     * @param placeholder the source of JSON
     * @return JSON
     */
    public String writePlaceholderToJson(Placeholder placeholder) {
        return jsonService.writeValueAsString(placeholder);
    }
}
