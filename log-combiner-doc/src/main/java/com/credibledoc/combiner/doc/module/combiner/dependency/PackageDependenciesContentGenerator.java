package com.credibledoc.combiner.doc.module.combiner.dependency;

import com.credibledoc.plantuml.sequence.SequenceArrow;
import com.credibledoc.substitution.core.content.ContentGenerator;
import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.substitution.core.placeholder.Placeholder;
import com.credibledoc.combiner.doc.markdown.MarkdownService;
import com.credibledoc.combiner.doc.module.combiner.exception.SubstitutionDocRuntimeException;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.utils.Pair;
import com.github.javaparser.utils.SourceZip;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Generates a UML diagram with figured dependencies of a package configured in a {@link #DEPENDANT_PACKAGE} variable on
 * other packages configured in a {@link #DEPENDENCIES_PACKAGES} variable.
 * <p>
 * This information will be parsed from a jar file with a source code. Path to this jar file is configured in the
 * {@link #JAR_RELATIVE_PATH} variable.
 *
 * @author Kyrylo Semenko
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PackageDependenciesContentGenerator implements ContentGenerator {

    private static final String INDENTATION = "    ";
    private static final String JAR_RELATIVE_PATH = "jarRelativePath";
    private static final String DEPENDANT_PACKAGE = "dependantPackage";
    private static final String DEPENDENCIES_PACKAGES = "dependenciesPackagesSemicolonSeparated";
    private static final String SEPARATOR = ";";
    private static final String LINE_SEPARATOR = System.lineSeparator();

    @NonNull
    MarkdownService markdownService;

    @Override
    public String generate(Placeholder placeholder) {
        try {
            String dependantPackage = getDependantPackageName(placeholder);
            String[] dependenciesPackages = getDependenciesPackages(placeholder);
            Path path = getSourcesJarPath(placeholder);
            SourceZip sourceZip = new SourceZip(path);
            ParserConfiguration parserConfiguration = new ParserConfiguration();
            sourceZip.setParserConfiguration(parserConfiguration);
            List<Pair<Path, ParseResult<CompilationUnit>>> parsedPairs = sourceZip.parse();

            NodeList<ImportDeclaration> importsNodeList = new NodeList<>();
            for (Pair<Path, ParseResult<CompilationUnit>> pair : parsedPairs) {
                Path nextPath = pair.a;
                ParseResult<CompilationUnit> parseResult = pair.b;
                CompilationUnit compilationUnit =
                    parseResult.getResult().orElseThrow(() -> new SubstitutionDocRuntimeException(
                        "CompilationUnit is not available. Path: " + nextPath));
                String packageName = compilationUnit.getPackageDeclaration()
                    .orElseThrow(() -> new SubstitutionDocRuntimeException(
                        "Package name cannot be found. CompilationUnit: " + compilationUnit))
                    .getNameAsString();
                if (packageName.startsWith(dependantPackage)) {
                    for (ImportDeclaration importDeclaration : compilationUnit.getImports()) {
                        String nextImport = importDeclaration.getNameAsString();
                        if (!nextImport.startsWith(dependantPackage) &&
                            startsWithOneOf(nextImport, dependenciesPackages)) {

                            importsNodeList.add(importDeclaration);
                        }
                    }
                }
            }
            // PlantUML
            StringBuilder stringBuilder = new StringBuilder()
                .append("left to right direction").append(System.lineSeparator())
                .append("hide empty members").append(System.lineSeparator())
                .append(System.lineSeparator())
                .append("skinparam class {").append(System.lineSeparator())
                .append(INDENTATION).append("BackgroundColor PaleGreen").append(System.lineSeparator())
                .append(INDENTATION).append("ArrowColor SeaGreen").append(System.lineSeparator())
                .append(INDENTATION).append("BorderColor SpringGreen").append(System.lineSeparator())
                .append("}").append(System.lineSeparator())
                .append(System.lineSeparator())
                .append("Class ").append(dependantPackage).append(" << (P,PaleGreen) >>")
                .append(System.lineSeparator());

            Set<String> classLines = new HashSet<>();
            for (ImportDeclaration importDeclaration : importsNodeList) {
                classLines.add("Class " + importDeclaration.getNameAsString() +
                    " << (C,YellowGreen) >>");
            }
            stringBuilder.append(String.join(System.lineSeparator(), classLines)).append(LINE_SEPARATOR);

            Set<String> dependencies = new HashSet<>();
            for (ImportDeclaration importDeclaration : importsNodeList) {
                dependencies.add(dependantPackage +
                    SequenceArrow.DEPENDENCY_ARROW.getUml() + importDeclaration.getNameAsString());
            }
            stringBuilder.append(String.join(System.lineSeparator(), dependencies));
            return markdownService.generateDiagram(placeholder, stringBuilder.toString());
        } catch (Exception e) {
            throw new CombinerRuntimeException(e);
        }
    }

    private Path getSourcesJarPath(Placeholder placeholder) {
        String jarRelativePath = placeholder.getParameters().get(JAR_RELATIVE_PATH);
        if (jarRelativePath == null) {
            throw new SubstitutionDocRuntimeException("Parameter '" + JAR_RELATIVE_PATH +
                "' cannot be found. This parameter is mandatory for this placeholder. " +
                "Example of usage: '\"" + Placeholder.FIELD_PARAMETERS +
                "\": {\"" + JAR_RELATIVE_PATH +
                "\": \"target/combiner-doc-1.0.0-SNAPSHOT-sources.jar\"}" +
                "'. ");
        }
        File file = new File(jarRelativePath);
        if (!file.exists()) {
            throw new SubstitutionDocRuntimeException("The file cannot be found: '" + file.getAbsolutePath() +
                "'. Its path is defined in the '" + JAR_RELATIVE_PATH +
                "' property with value '" + jarRelativePath +
                "'. This property is a part of the Placeholder: " + placeholder);
        }
        return file.toPath();
    }

    private String[] getDependenciesPackages(Placeholder placeholder) {
        String dependenciesPackagesParameter = placeholder.getParameters().get(DEPENDENCIES_PACKAGES);
        if (dependenciesPackagesParameter == null) {
            throw new SubstitutionDocRuntimeException("Parameter '" + DEPENDANT_PACKAGE +
                "' cannot be found. It is mandatory for this Placeholder. " +
                "Example of usage: '" + "\"" + Placeholder.FIELD_PARAMETERS +
                "\": {\"" + DEPENDENCIES_PACKAGES +
                "\": \"other.packages.first.package;second.package;third.package\"...'.");
        }
        return dependenciesPackagesParameter.split(SEPARATOR);
    }

    private String getDependantPackageName(Placeholder placeholder) {
        String dependantPackage = placeholder.getParameters().get(DEPENDANT_PACKAGE);
        if (dependantPackage == null) {
            throw new SubstitutionDocRuntimeException("Parameter with name '" + DEPENDANT_PACKAGE +
                "' cannot be found. It is mandatory for this Placeholder. " +
                "Example of usage: '" + "\"" + Placeholder.FIELD_PARAMETERS +
                "\":{\"" + DEPENDANT_PACKAGE +
                "\": \"package.dependent.on.other.packages\"...'.");
        }
        return dependantPackage;
    }

    /**
     * Call the {@link String#startsWith(String)} method to each prefix
     * @param tested to be tested
     * @param prefixes to be matched
     * @return 'true' if the tested string starts with at least one of prefixes
     */
    private boolean startsWithOneOf(String tested, String[] prefixes) {
        for (String prefix : prefixes) {
            if (tested.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

}
