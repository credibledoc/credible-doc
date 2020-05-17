package com.credibledoc.substitution.content.generator.dependency;

import com.credibledoc.plantuml.sequence.SequenceArrow;
import com.credibledoc.substitution.core.content.Content;
import com.credibledoc.substitution.core.content.ContentGenerator;
import com.credibledoc.substitution.core.context.SubstitutionContext;
import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.core.placeholder.Placeholder;
import com.credibledoc.substitution.core.replacement.ReplacementType;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.utils.Pair;
import com.github.javaparser.utils.SourceRoot;
import com.github.javaparser.utils.SourceZip;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Generates a UML diagram with figured dependencies of a package configured in a {@link #DEPENDANT_PACKAGE} variable on
 * other packages configured in a {@link #DEPENDENCIES_PACKAGES_PIPE_SEPARATED} variable.
 * <p>
 * This information will be parsed from jar files and/or directories with a source code.
 * Path to these jar files is configured in the {@link #JAR_RELATIVE_PATHS_PIPE_SEPARATED} parameter.
 * Path to these directories is configured in the {@link #SOURCE_RELATIVE_PATHS_PIPE_SEPARATED} parameter.
 * <p>
 * If the {@link #IGNORE_INNER_PACKAGES} parameter is 'true', dependencies on inner packages will not be showed.
 * Default value is 'false'.
 * <p>
 * Example of usage:
 * <pre>{@code
 * &&beginPlaceholder {
 *     "className": "com.credibledoc.substitution.content.generator.dependency.PackageDependenciesContentGenerator",
 *     "description": "Dependency of the `com.credibledoc.substitution.doc` package on other classes in the `com
 *     .credibledoc.substitution` package.",
 *     "parameters": {
 *         "jarRelativePathsPipeSeparated": "credible-doc-generator/target/credible-doc-generator-1.0.4-SNAPSHOT-sources.jar",
 *         "sourceRelativePathsPipeSeparated": "credible-doc-generator/src/main/java",
 *         "dependantPackage": "com.credibledoc.substitution.doc",
 *         "dependenciesPackagesPipeSeparated": "com.credibledoc.substitution",
 *         "ignoreInnerPackages": "true"}
 * } &&endPlaceholder
 * }</pre>
 * 
 * See the next examples
 * <pre>
 *                     IF
 *                         First class is com.first.First
 *                             and imports com.second.Second
 *                         Second class is com.second.Second
 *                     THEN
 *                         the First class IS depends on the Second class
 *
 *                     IF
 *                         First class is com.first.First
 *                             and imports com.first.Second
 *                         Second class is com.first.Second
 *                     THEN
 *                         the First class IS NOT depends on the Second class
 *
 *                     IF
 *                         First class is com.first.First
 *                             and imports com.first.second.Second
 *                         Second class is com.first.second.Second
 *                     THEN
 *                         IF ignoreInnerPackages
 *                             the First class IS NOT depends on the Second class
 *                         ELSE
 *                             the First class IS depends on the Second class
 * </pre>
 *
 * @author Kyrylo Semenko
 */
public class PackageDependenciesContentGenerator implements ContentGenerator {

    private static final String INDENTATION = "    ";
    static final String JAR_RELATIVE_PATHS_PIPE_SEPARATED = "jarRelativePathsPipeSeparated";
    static final String DEPENDANT_PACKAGE = "dependantPackage";
    static final String DEPENDENCIES_PACKAGES_PIPE_SEPARATED = "dependenciesPackagesPipeSeparated";
    private static final String LINE_SEPARATOR = System.lineSeparator();
    static final String IGNORE_INNER_PACKAGES = "ignoreInnerPackages";
    private static final String ITALICS_MARKDOWN_MARK = "_";
    static final String SOURCE_RELATIVE_PATHS_PIPE_SEPARATED = "sourceRelativePathsPipeSeparated";
    private static final String SEPARATOR_PIPE = "\\|";
    static final String AT_LEAST_ONE_OF_THESE_PARAMETERS_IS_MANDATORY_FOR_THIS_PLACEHOLDER =
        " At least one of these parameters is mandatory for this placeholder. ";
    static final String THE_FILE_CANNOT_BE_FOUND = "The file cannot be found: '";
    static final String PARAMETER_WITH_NAME = "Parameter with name '";
    static final String NOTE_NO_DEPENDENCIES_FOUND = "header \"No dependencies found.\"";

    private final Map<String, List<Pair<Path, ParseResult<CompilationUnit>>>> cache = new HashMap<>();

    @Override
    public Content generate(Placeholder placeholder, SubstitutionContext substitutionContext) {
        try {
            validateParameters(placeholder);
            String dependantPackage = getDependantPackageName(placeholder);
            String[] dependenciesPackages = getDependenciesPackages(placeholder);
            addToCacheIfNotExists(placeholder);
            String ignoreInnerPackagesString = placeholder.getParameters().get(IGNORE_INNER_PACKAGES);
            boolean ignoreInnerPackages = "true".equals(ignoreInnerPackagesString);

            NodeList<ImportDeclaration> importsNodeList = new NodeList<>();

            String jarsKey = placeholder.getParameters().get(JAR_RELATIVE_PATHS_PIPE_SEPARATED);
            if (jarsKey != null) {
                for (Pair<Path, ParseResult<CompilationUnit>> pair : cache.get(jarsKey)) {
                    addToImportsNodeList(importsNodeList, dependantPackage,
                        dependenciesPackages, ignoreInnerPackages, pair);
                }
            }

            String sourcesKey = placeholder.getParameters().get(SOURCE_RELATIVE_PATHS_PIPE_SEPARATED);
            if (sourcesKey != null) {
                for (Pair<Path, ParseResult<CompilationUnit>> pair : cache.get(sourcesKey)) {
                    addToImportsNodeList(importsNodeList, dependantPackage,
                        dependenciesPackages, ignoreInnerPackages, pair);
                }
            }

            String format = placeholder.getParameters().get(ReplacementType.TARGET_FORMAT);
            if (importsNodeList.isEmpty()) {
                return generateEmptyListContent(placeholder, format);
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
            Content content = new Content();
            content.setPlantUmlContent(stringBuilder.toString());
            if (format == null) {
                setDescriptionToMarkdown(placeholder, content);
            }
            return content;
        } catch (Exception e) {
            throw new SubstitutionRuntimeException(e);
        }
    }

    public Content generateEmptyListContent(Placeholder placeholder, String format) {
        Content content = new Content();
        if (format != null) {
            ReplacementType replacementType = ReplacementType.valueOf(format);
            if (ReplacementType.HTML_EMBEDDED == replacementType) {
                content.setPlantUmlContent(NOTE_NO_DEPENDENCIES_FOUND);
            } else {
                throw new SubstitutionRuntimeException("Unknown " + ReplacementType.class.getSimpleName() + " " +
                    "value " + replacementType);
            } 
        } else {
            content.setPlantUmlContent(NOTE_NO_DEPENDENCIES_FOUND);
            setDescriptionToMarkdown(placeholder, content);
        }
        return content;
    }

    public void setDescriptionToMarkdown(Placeholder placeholder, Content content) {
        content.setMarkdownContent(LINE_SEPARATOR + LINE_SEPARATOR + ITALICS_MARKDOWN_MARK +
            placeholder.getDescription() + ITALICS_MARKDOWN_MARK + LINE_SEPARATOR);
    }

    private void addToImportsNodeList(NodeList<ImportDeclaration> importsNodeList, String dependantPackage,
                                      String[] dependenciesPackages, boolean ignoreInnerPackages, Pair<Path,
        ParseResult<CompilationUnit>> pair) {
        Path nextPath = pair.a;
        ParseResult<CompilationUnit> parseResult = pair.b;
        CompilationUnit compilationUnit =
            parseResult.getResult().orElseThrow(() -> new SubstitutionRuntimeException(
                "CompilationUnit is not available. Path: " + nextPath));
        String packageName = compilationUnit.getPackageDeclaration()
            .orElseThrow(() -> new SubstitutionRuntimeException(
                "Package name cannot be found. CompilationUnit: " + compilationUnit))
            .getNameAsString();
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

    private void addToCacheIfNotExists(Placeholder placeholder) throws IOException {
        Set<File> files = getSourceJarFiles(placeholder);
        String jarsKey = placeholder.getParameters().get(JAR_RELATIVE_PATHS_PIPE_SEPARATED);
        if (jarsKey != null && !cache.containsKey(jarsKey)) {
            List<Pair<Path, ParseResult<CompilationUnit>>> jarsPairs = new ArrayList<>();
            for (File file : files) {
                SourceZip sourceZip = new SourceZip(file.toPath());
                ParserConfiguration parserConfiguration = new ParserConfiguration();
                sourceZip.setParserConfiguration(parserConfiguration);
                jarsPairs.addAll(sourceZip.parse());
            }
            cache.put(jarsKey, jarsPairs);
        }

        Set<File> sourceDirectories = getSourceDirectories(placeholder);
        String sourcesKey = placeholder.getParameters().get(SOURCE_RELATIVE_PATHS_PIPE_SEPARATED);
        if (sourcesKey != null && !cache.containsKey(sourcesKey)) {
            List<Pair<Path, ParseResult<CompilationUnit>>> sourcesPairs = new ArrayList<>();
            for (File directory : sourceDirectories) {
                SourceRoot sourceRoot = new SourceRoot(directory.toPath());
                ParserConfiguration parserConfiguration = new ParserConfiguration();
                sourceRoot.setParserConfiguration(parserConfiguration);
                List<ParseResult<CompilationUnit>> parseResults = sourceRoot.tryToParse();
                if (parseResults.isEmpty()) {
                    throw new SubstitutionRuntimeException("ParseResults is empty. SourceRoot directory: '" +
                        directory.getAbsolutePath() + "'. Probably due to incorrect configuration of Placeholder: " +
                        placeholder);
                }
                for (ParseResult<CompilationUnit> parseResult : parseResults) {
                    Pair<Path, ParseResult<CompilationUnit>> pair = new Pair<>(directory.toPath(), parseResult);
                    sourcesPairs.add(pair);
                }
            }
            cache.put(sourcesKey, sourcesPairs);
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

    private Set<File> getSourceJarFiles(Placeholder placeholder) {
        String jarRelativePathsPipeSeparated = placeholder.getParameters()
            .get(JAR_RELATIVE_PATHS_PIPE_SEPARATED);

        List<String> jarRelativePaths = collectPaths(jarRelativePathsPipeSeparated);

        Set<File> result = new HashSet<>();
        for (String jarRelativePath : jarRelativePaths) {
            File file = new File(jarRelativePath);
            if (!file.exists()) {
                throw new SubstitutionRuntimeException(THE_FILE_CANNOT_BE_FOUND + file.getAbsolutePath() +
                    "'. Its path is defined in the '" + JAR_RELATIVE_PATHS_PIPE_SEPARATED +
                    "' property with value '" + jarRelativePath +
                    "'. This property is a part of the Placeholder: " + placeholder);
            }
            result.add(file);
        }
        return result;
    }

    private Set<File> getSourceDirectories(Placeholder placeholder) {
        String sourceRelativePathsPipeSeparated = placeholder.getParameters()
            .get(SOURCE_RELATIVE_PATHS_PIPE_SEPARATED);

        List<String> sourceRelativePaths = collectPaths(sourceRelativePathsPipeSeparated);

        Set<File> result = new HashSet<>();
        for (String sourceRelativePath : sourceRelativePaths) {
            File file = new File(sourceRelativePath);
            if (!file.exists() || !file.isDirectory()) {
                throw new SubstitutionRuntimeException("The directory cannot be found: '" + file.getAbsolutePath() +
                    "'. Its path is defined in the '" + SOURCE_RELATIVE_PATHS_PIPE_SEPARATED +
                    "' property with value '" + sourceRelativePathsPipeSeparated +
                    "'. This property is a part of the Placeholder: " + placeholder);
            }
            result.add(file);
        }
        return result;
    }

    private List<String> collectPaths(String sourceRelativePaths) {
        List<String> stringList = Collections.emptyList();
        if (sourceRelativePaths != null) {
            stringList = Arrays.asList(sourceRelativePaths.split(SEPARATOR_PIPE));
        }
        return stringList;
    }

    private void validateParameters(Placeholder placeholder) {
        String jarRelativePathsPipeSeparated = placeholder.getParameters().get(JAR_RELATIVE_PATHS_PIPE_SEPARATED);
        String sourceRelativePathsPipeSeparated = placeholder.getParameters().get(SOURCE_RELATIVE_PATHS_PIPE_SEPARATED);
        if (jarRelativePathsPipeSeparated == null && sourceRelativePathsPipeSeparated == null) {
            throw new SubstitutionRuntimeException(
                "Parameter '" + JAR_RELATIVE_PATHS_PIPE_SEPARATED + "' cannot be found." +
                    " Parameter '" + SOURCE_RELATIVE_PATHS_PIPE_SEPARATED + "' cannot be found." +
                    AT_LEAST_ONE_OF_THESE_PARAMETERS_IS_MANDATORY_FOR_THIS_PLACEHOLDER +
                    "Example of usage: '\"" + Placeholder.FIELD_PARAMETERS +
                    "\": {\"" + JAR_RELATIVE_PATHS_PIPE_SEPARATED +
                    "\": \"target/credible-doc-generator-1.0.0-SNAPSHOT-sources.jar\"}" +
                    "'. ");
        }
    }

    private String[] getDependenciesPackages(Placeholder placeholder) {
        String dependenciesPackagesParameter = placeholder.getParameters().get(DEPENDENCIES_PACKAGES_PIPE_SEPARATED);
        if (dependenciesPackagesParameter == null) {
            throw new SubstitutionRuntimeException(PARAMETER_WITH_NAME + DEPENDENCIES_PACKAGES_PIPE_SEPARATED +
                "' cannot be found. It is mandatory for this Placeholder. " +
                "Example of usage: '" + "\"" + Placeholder.FIELD_PARAMETERS +
                "\": {\"" + DEPENDENCIES_PACKAGES_PIPE_SEPARATED +
                "\": \"other.packages.first.package;second.package;third.package\"...'.");
        }
        return dependenciesPackagesParameter.split(SEPARATOR_PIPE);
    }

    private String getDependantPackageName(Placeholder placeholder) {
        String dependantPackage = placeholder.getParameters().get(DEPENDANT_PACKAGE);
        if (dependantPackage == null) {
            throw new SubstitutionRuntimeException(PARAMETER_WITH_NAME + DEPENDANT_PACKAGE +
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
