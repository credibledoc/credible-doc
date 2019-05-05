package com.credibledoc.substitution.content.generator.dependency;

import com.credibledoc.plantuml.sequence.SequenceArrow;
import com.credibledoc.substitution.core.content.Content;
import com.credibledoc.substitution.core.content.ContentGenerator;
import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.core.placeholder.Placeholder;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.utils.Pair;
import com.github.javaparser.utils.SourceZip;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Generates a UML diagram with figured dependencies of a package configured in a {@link #DEPENDANT_PACKAGE} variable on
 * other packages configured in a {@link #DEPENDENCIES_PACKAGES} variable.
 * <p>
 * This information will be parsed from a jar file with a source code. Path to this jar file is configured in the
 * {@link #JAR_RELATIVE_PATH} variable.
 * <p>
 * If the {@link #IGNORE_INNER_PACKAGES} parameter is 'true', dependencies on inner packages will not be showed.
 * Default value is 'false'.
 *
 * @author Kyrylo Semenko
 */
public class PackageDependenciesContentGenerator implements ContentGenerator {

    private static final String INDENTATION = "    ";
    private static final String JAR_RELATIVE_PATH = "jarRelativePath";
    private static final String DEPENDANT_PACKAGE = "dependantPackage";
    private static final String DEPENDENCIES_PACKAGES = "dependenciesPackagesSemicolonSeparated";
    private static final String SEPARATOR = ";";
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String IGNORE_INNER_PACKAGES = "ignoreInnerPackages";
    private static final String ITALICS_MARKDOWN_MARK = "_";

    private Map<String, List<Pair<Path, ParseResult<CompilationUnit>>>> cache = new HashMap<>();

    @Override
    public Content generate(Placeholder placeholder) {
        try {
            String dependantPackage = getDependantPackageName(placeholder);
            String[] dependenciesPackages = getDependenciesPackages(placeholder);
            String jarRelativePath = placeholder.getParameters().get(JAR_RELATIVE_PATH);
            addToCacheIfNotExists(placeholder, jarRelativePath);
            String ignoreInnerPackagesString = placeholder.getParameters().get(IGNORE_INNER_PACKAGES);
            boolean ignoreInnerPackages = "true".equals(ignoreInnerPackagesString);

            NodeList<ImportDeclaration> importsNodeList = new NodeList<>();
            for (Pair<Path, ParseResult<CompilationUnit>> pair : cache.get(jarRelativePath)) {
                Path nextPath = pair.a;
                ParseResult<CompilationUnit> parseResult = pair.b;
                CompilationUnit compilationUnit =
                    parseResult.getResult().orElseThrow(() -> new SubstitutionRuntimeException(
                        "CompilationUnit is not available. Path: " + nextPath));
                String packageName = compilationUnit.getPackageDeclaration()
                    .orElseThrow(() -> new SubstitutionRuntimeException(
                        "Package name cannot be found. CompilationUnit: " + compilationUnit))
                    .getNameAsString();
                /*
                    For example:
                    IF
                        First class is com.first.First
                            and imports com.second.Second
                        Second class is com.second.Second
                    THEN
                        the First class IS depend on the Second class

                    IF
                        First class is com.first.First
                            and imports com.first.Second
                        Second class is com.first.Second
                    THEN
                        the First class IS NOT depend on the Second class

                    IF
                        First class is com.first.First
                            and imports com.first.second.Second
                        Second class is com.first.second.Second
                    THEN
                        IF ignoreInnerPackages
                            the First class IS NOT depend on the Second class
                        ELSE
                            the First class IS depend on the Second class
                 */
                if (packageName.startsWith(dependantPackage) && !containsOneOf(packageName, dependenciesPackages)) {
                    for (ImportDeclaration importDeclaration : compilationUnit.getImports()) {
                        String nextImport = importDeclaration.getNameAsString();
                        if (startsWithOneOf(nextImport, dependenciesPackages) &&
                                !classBelongsToPackage(nextImport, dependantPackage, ignoreInnerPackages)) {

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
                .append("Class ").append(dependantPackage).append(" << (P,LightSeaGreen) >>")
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
            Content result = new Content();
            result.setPlantUmlContent(stringBuilder.toString());
            result.setMarkdownContent(LINE_SEPARATOR + LINE_SEPARATOR + ITALICS_MARKDOWN_MARK +
                placeholder.getDescription() + ITALICS_MARKDOWN_MARK + LINE_SEPARATOR);
            return result;
        } catch (Exception e) {
            throw new SubstitutionRuntimeException(e);
        }
    }

    private void addToCacheIfNotExists(Placeholder placeholder, String jarRelativePath) throws IOException {
        if (!cache.containsKey(jarRelativePath)) {
            Path path = getSourcesJarPath(placeholder);
            SourceZip sourceZip = new SourceZip(path);
            ParserConfiguration parserConfiguration = new ParserConfiguration();
            sourceZip.setParserConfiguration(parserConfiguration);
            List<Pair<Path, ParseResult<CompilationUnit>>> parsedPairs = sourceZip.parse();
            cache.put(jarRelativePath, parsedPairs);
        }
    }

    private boolean classBelongsToPackage(String className, String packageName, boolean ignoreInnerPackages) {
        int index = className.indexOf(packageName);
        if (index == -1) {
            return false;
        }
        String suffix = className.substring(packageName.length());
        if (ignoreInnerPackages) {
            return suffix.startsWith(".");
        }
        return suffix.startsWith(".") && suffix.split("\\.").length < 3;
    }

    private boolean containsOneOf(String packageName, String[] packageNames) {
        for (String nextPackageName : packageNames) {
            if (packageName.equals(nextPackageName)) {
                return true;
            }
        }
        return false;
    }

    private Path getSourcesJarPath(Placeholder placeholder) {
        String jarRelativePath = placeholder.getParameters().get(JAR_RELATIVE_PATH);
        if (jarRelativePath == null) {
            throw new SubstitutionRuntimeException("Parameter '" + JAR_RELATIVE_PATH +
                "' cannot be found. This parameter is mandatory for this placeholder. " +
                "Example of usage: '\"" + Placeholder.FIELD_PARAMETERS +
                "\": {\"" + JAR_RELATIVE_PATH +
                "\": \"target/substitution-doc-1.0.0-SNAPSHOT-sources.jar\"}" +
                "'. ");
        }
        File file = new File(jarRelativePath);
        if (!file.exists()) {
            throw new SubstitutionRuntimeException("The file cannot be found: '" + file.getAbsolutePath() +
                "'. Its path is defined in the '" + JAR_RELATIVE_PATH +
                "' property with value '" + jarRelativePath +
                "'. This property is a part of the Placeholder: " + placeholder);
        }
        return file.toPath();
    }

    private String[] getDependenciesPackages(Placeholder placeholder) {
        String dependenciesPackagesParameter = placeholder.getParameters().get(DEPENDENCIES_PACKAGES);
        if (dependenciesPackagesParameter == null) {
            throw new SubstitutionRuntimeException("Parameter '" + DEPENDANT_PACKAGE +
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
            throw new SubstitutionRuntimeException("Parameter with name '" + DEPENDANT_PACKAGE +
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
