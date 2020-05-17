package com.credibledoc.substitution.content.generator.resource;

import com.credibledoc.substitution.core.content.Content;
import com.credibledoc.substitution.core.content.ContentGenerator;
import com.credibledoc.substitution.core.context.SubstitutionContext;
import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.core.tracking.Trackable;
import com.credibledoc.substitution.core.placeholder.Placeholder;
import com.eclipsesource.json.JsonObject;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates a fragment from an existing template.
 * <p>
 * Mandatory parameter {@link #FRAGMENT_RELATIVE_PATH} defines the source template relatively to
 * the directory where the application is launched.
 * <p>
 * Optional parameter {@link #INDENTATION} is used for indent all template lines.
 * <p>
 * Optional parameter {@link #CHARSET} is used for reading the template. Default is UTF-8.
 * <p>
 * Optional parameter {@link Placeholder#getJsonObject()} is used for replacing values enclosed
 * to {@link #PLACEHOLDER_BEGIN} and {@link #PLACEHOLDER_END} tags.
 * It can be used for adjusting relative links and for other parameterization. For example:
 * <pre>
 *     placeholder parameter: "jsonObject": {"linkPrefix": "../"}
 *     template contains link: img src="${linkPrefix}img/logo.gif"
 *     generated content will be the following: img src="../img/logo.gif"
 * </pre>
 * <p>
 * Example of usage:
 * <pre>{@code
 * &&beginPlaceholder {
 *     "className": "com.credibledoc.substitution.content.generator.resource.FragmentGenerator",
 *     "description": "Inserts 'header' fragment to a html page.",
 *     "parameters": {
 *         "fragmentRelativePath": "credible-doc-generator/src/main/resources/fragment/header.html",
 *         "indentation": "   ",
 *         "charset": "ISO-8859-1"
 *     },
 *     "jsonObject": {
 *         "title": "Credible documentation - main page",
 *         "linkPrefix": "../" 
 *     }
 * } &&endPlaceholder
 * }</pre>
 * <p>
 *
 * @author Kyrylo Semenko
 */
public class FragmentGenerator implements ContentGenerator, Trackable {
    private static final String PLACEHOLDER_BEGIN = "${";
    private static final String PLACEHOLDER_END = "}";
    /**
     * Contains a file for tracking with {@link Trackable};
     */
    private Path fragmentPath;
    
    private static final String FRAGMENT_RELATIVE_PATH = "fragmentRelativePath";
    private static final String INDENTATION = "indentation";
    private static final String CHARSET = "charset";

    @Override
    public Content generate(Placeholder placeholder, SubstitutionContext substitutionContext) {
        try {
            String indentation = placeholder.getParameters().get(INDENTATION);
            if (indentation == null) {
                indentation = "";
            }
            
            String fragmentRelativePath = placeholder.getParameters().get(FRAGMENT_RELATIVE_PATH);
            if (fragmentRelativePath == null) {
                throw new SubstitutionRuntimeException("Parameter '" + FRAGMENT_RELATIVE_PATH + "' is mandatory " +
                    "for the Placeholder " + placeholder);
            }
            
            String charsetName = placeholder.getParameters().get(CHARSET);
            if (charsetName == null) {
                charsetName = "UTF-8";
            }

            JsonObject jsonObject = placeholder.getJsonObject();
            Map<String, String> substitutionMap = new HashMap<>();
            if (jsonObject != null) {
                for (String name : jsonObject.names()) {
                    substitutionMap.put(name, jsonObject.getString(name, "error_value_is_empty"));
                }
            }

            Path path = Paths.get(fragmentRelativePath);
            fragmentPath = path.toAbsolutePath();
            if (!fragmentPath.toFile().exists()) {
                throw new SubstitutionRuntimeException("Fragment file not found '" + fragmentPath.toString() +
                    "', Placeholder: " + placeholder);
            }
            byte[] encoded = Files.readAllBytes(path);
            String templateContent = new String(encoded, Charset.forName(charsetName));

            for (Map.Entry<String, String> entry : substitutionMap.entrySet()) {
                String target = PLACEHOLDER_BEGIN + entry.getKey() + PLACEHOLDER_END;
                templateContent = templateContent.replace(target, entry.getValue());
            }

            int beginIndex = templateContent.indexOf(PLACEHOLDER_BEGIN);
            if (beginIndex > -1) {
                int endIndex = templateContent.indexOf(PLACEHOLDER_END, beginIndex);
                if (endIndex > -1) {
                    String variable = templateContent.substring(beginIndex, endIndex + PLACEHOLDER_END.length());
                    throw new SubstitutionRuntimeException("Cannot substitute '" + variable +
                        "'. Placeholder: " + placeholder);
                }
            }

            StringBuilder stringBuilder = new StringBuilder();
            String[] lines = templateContent.split("\r\n|\n");

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                stringBuilder.append(indentation).append(line);
                if (i < lines.length - 1) {
                    stringBuilder.append(System.lineSeparator());
                }
            }

            Content content = new Content();
            content.setMarkdownContent(stringBuilder.toString());
            return content;
        } catch (Exception e) {
            throw new SubstitutionRuntimeException(e);
        }
    }

    @Override
    public List<Path> getFragmentPaths() {
        List<Path> paths = new ArrayList<>();
        paths.add(fragmentPath);
        return paths;
    }
}
