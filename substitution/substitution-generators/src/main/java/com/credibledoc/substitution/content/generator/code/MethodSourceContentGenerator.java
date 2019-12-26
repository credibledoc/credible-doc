package com.credibledoc.substitution.content.generator.code;

import com.credibledoc.substitution.core.content.Content;
import com.credibledoc.substitution.core.content.ContentGenerator;
import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.core.placeholder.Placeholder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Generates java method with indentation.
 *
 * @author Kyrylo Semenko
 */
public class MethodSourceContentGenerator implements ContentGenerator {

    private static final String SOURCE_RELATIVE_PATH = "sourceRelativePath";
    private static final String BEGIN_STRING = "beginString";
    private static final String END_STRING = "endString";
    private static final String INDENTATION = "indentation";
    private static final String PARAMETER = "Parameter ";
    private static final String IS_MANDATORY = " is mandatory";

    @Override
    public Content generate(Placeholder placeholder) {
        try {
            validateParameters(placeholder);
            String sourceRelativePath = placeholder.getParameters().get(SOURCE_RELATIVE_PATH);
            String beginString = placeholder.getParameters().get(BEGIN_STRING);
            String endString = placeholder.getParameters().get(END_STRING);
            String indentation = placeholder.getParameters().get(INDENTATION);
            if (indentation == null) {
                indentation = "";
            }

            byte[] encoded = Files.readAllBytes(Paths.get(sourceRelativePath));
            String fileContent = new String(encoded, StandardCharsets.UTF_8);
            int beginIndex = fileContent.indexOf(beginString);
            int endIndex = fileContent.indexOf(endString, beginIndex);
            String methodContent = fileContent.substring(beginIndex, endIndex + endString.length());
            
            String[] lines = methodContent.split("\\r\\n|\\n");
            StringBuilder stringBuilder = new StringBuilder(methodContent.length());
            for (int i = 0; i < lines.length; i++) {
                stringBuilder.append(indentation).append(lines[i]);
                if (i < lines.length - 1) {
                    stringBuilder.append("\r\n");
                }
            }
            
            Content content = new Content();
            content.setMarkdownContent(stringBuilder.toString());
            return content;
        } catch (Exception e) {
            throw new SubstitutionRuntimeException(e);
        }
    }

    private void validateParameters(Placeholder placeholder) {
        String sourceRelativePath = placeholder.getParameters().get(SOURCE_RELATIVE_PATH);
        if (sourceRelativePath == null) {
            throw new SubstitutionRuntimeException(PARAMETER + SOURCE_RELATIVE_PATH + IS_MANDATORY);
        }
        
        String methodName = placeholder.getParameters().get(BEGIN_STRING);
        if (methodName == null) {
            throw new SubstitutionRuntimeException(PARAMETER + BEGIN_STRING + IS_MANDATORY);
        }
        
        String endString = placeholder.getParameters().get(END_STRING);
        if (endString == null) {
            throw new SubstitutionRuntimeException(PARAMETER + END_STRING + IS_MANDATORY);
        }
    }

}
