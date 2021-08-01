package com.credibledoc.substitution.doc.module.substitution.markdown.table;

import com.credibledoc.substitution.core.resource.ResourceService;
import com.credibledoc.substitution.core.resource.ResourceType;
import com.credibledoc.substitution.core.resource.TemplateResource;
import com.credibledoc.substitution.core.template.TemplateService;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.javadoc.Javadoc;
import lombok.RequiredArgsConstructor;
import net.steppschuh.markdowngenerator.table.Table;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;

/**
 * This service generates markdown tables, see the {@link #createMarkdownTableFromEnum(Table.Builder, Class)} method.
 *
 * @author Kyrylo Semenko
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TableService {

    private static final String EMPTY_STRING = "";
    private static final String ONE_SPACE = " ";
    private static final String NEW_LINE = "\\r\\n|\\n";

    /**
     * Generates content of a table with two columns from {@link Enum} fields.
     * <p>
     * Example of usage:
     * <pre>
     *     public String generate() {
     *         Table.Builder tableBuilder = new Table.Builder()
     *                 .addRow("Application name", "Description");
     *
     *         return tableService.createMarkdownTableFromEnum(tableBuilder, TacticHolder.class);
     *     }
     * </pre>
     *
     * @param tableBuilder en empty table with header
     * @param enumClass    the data source
     * @return the first column with enum fields and a second column with
     * fields JavaDoc
     */
    public String createMarkdownTableFromEnum(Table.Builder tableBuilder, Class<?> enumClass) {
        String resourceRelativePath = ResourceService.getInstance().getResource(enumClass);

        TemplateResource templateResource = new TemplateResource();
        templateResource.setType(ResourceType.CLASSPATH);
        templateResource.setPath(resourceRelativePath);

        String sourceCode = TemplateService.getInstance().getTemplateContent(templateResource, StandardCharsets.UTF_8.name());

        CompilationUnit compilationUnit = StaticJavaParser.parse(sourceCode);

        EnumDeclaration enumDeclaration = (EnumDeclaration) compilationUnit.getTypes().get(0);
        for (EnumConstantDeclaration enumConstantDeclaration : enumDeclaration.getEntries()) {
            String name = enumConstantDeclaration.getName().asString();
            String javadocString = EMPTY_STRING;
            Javadoc javadoc = enumConstantDeclaration.getJavadoc().orElse(null);
            if (javadoc != null) {
                javadocString = javadoc.toText().replaceAll(NEW_LINE, ONE_SPACE);
            }
            tableBuilder.addRow(name, javadocString);
        }
        return tableBuilder.build().toString();
    }
}
