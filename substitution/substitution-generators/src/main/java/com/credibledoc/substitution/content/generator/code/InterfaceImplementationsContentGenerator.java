package com.credibledoc.substitution.content.generator.code;

import com.credibledoc.substitution.core.content.Content;
import com.credibledoc.substitution.core.content.ContentGenerator;
import com.credibledoc.substitution.core.context.SubstitutionContext;
import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.core.placeholder.Placeholder;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

/**
 * Generates a list of the defined interface implementations.
 * 
 * Example of usage:
 * <pre>{@code
 * 
 * ```
 * &&beginPlaceholder {
 *     "className": "com.credibledoc.substitution.content.generator.code.InterfaceImplementationsContentGenerator",
 *     "description": "All known implementations of the LengthPacker interface",
 *     "parameters": {
 *         "interfaceName": "com.credibledoc.iso8583packer.length.LengthPacker",
 *         "includePackages": "com.credibledoc.*"
 *     }
 * } &&endPlaceholder
 * ```
 * 
 * }</pre>
 *
 * @author Kyrylo Semenko
 */
public class InterfaceImplementationsContentGenerator implements ContentGenerator {

    private static final String INTERFACE_NAME = "interfaceName";
    private static final String INCLUDE_PACKAGES = "includePackages";

    @Override
    public Content generate(Placeholder placeholder, SubstitutionContext substitutionContext) {
        String interfaceName = placeholder.getParameters().get(INTERFACE_NAME);
        validateClassExists(interfaceName, placeholder);
        String includePackages = placeholder.getParameters().get(INCLUDE_PACKAGES);

        StringBuilder stringBuilder = new StringBuilder();
        try (ScanResult scanResult = new ClassGraph().whitelistPackages(includePackages) .enableClassInfo().scan()) {
            for (ClassInfo ci : scanResult.getClassesImplementing(interfaceName)) {
                stringBuilder.append("* ");
                stringBuilder.append(ci.getName());
                stringBuilder.append(System.lineSeparator());
            }
        }

        Content content = new Content();
        content.setMarkdownContent(stringBuilder.toString());
        return content;
    }

    protected void validateClassExists(String interfaceName, Placeholder placeholder) {
        try {
            Class.forName(interfaceName);
        } catch (Exception e) {
            throw new SubstitutionRuntimeException("Cannot find interface '" + interfaceName + "', " +
                "placeholder: " + placeholder);
        }
    }

}
