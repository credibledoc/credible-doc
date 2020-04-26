package com.credibledoc.substitution.content.generator.dependency;

import com.credibledoc.plantuml.svggenerator.SvgGeneratorService;
import com.credibledoc.substitution.core.content.Content;
import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.core.placeholder.Placeholder;
import junit.framework.AssertionFailedError;
import org.junit.Test;

import static org.junit.Assert.*;

public class PackageDependenciesContentGeneratorTest {

    private static final String DESCRIPTION = "description";
    private static final String EXPECTED_STRING_NOT_FOUND = "Expected string not found: '";
    private static final String CANNOT_BE_FOUND_IN_THE_CONTENT = "' cannot be found in the content.";
    private static final String CONSTANT = "Constant '";
    private static final String COM_CREDIBLEDOC_PLANTUML_SVGGENERATOR = "com.credibledoc.plantuml.svggenerator";
    private static final String COM_CREDIBLEDOC_PLANTUML_EXCEPTION = "com.credibledoc.plantuml.exception";
    private static final String EXPECTED_UML_PART = "com.credibledoc.plantuml.svggenerator ..> com.credibledoc.plantuml.exception.PlantumlRuntimeException";
    private static final String TEXT = "Text '";
    private static final String CONTENT = " Content: '";
    private static final String EXAMPLE_JAR = "target/test-classes/jars/example.jar";
    private static final String TARGET_TEST_CLASSES_PATHS = "target/test-classes/paths";

    @Test
    public void testValidateParameters() {
        PackageDependenciesContentGenerator generator = new PackageDependenciesContentGenerator();
        Placeholder placeholder = new Placeholder();
        try {
            generator.generate(placeholder, null);
        } catch (SubstitutionRuntimeException e) {
            assertTrue(e.getMessage().contains(
                PackageDependenciesContentGenerator
                    .AT_LEAST_ONE_OF_THESE_PARAMETERS_IS_MANDATORY_FOR_THIS_PLACEHOLDER));
            return;
        }
        throw new AssertionFailedError(EXPECTED_STRING_NOT_FOUND +
            PackageDependenciesContentGenerator
                .AT_LEAST_ONE_OF_THESE_PARAMETERS_IS_MANDATORY_FOR_THIS_PLACEHOLDER +
            "'");
    }

    @Test
    public void testDependantPackageExists() {
        PackageDependenciesContentGenerator generator = new PackageDependenciesContentGenerator();
        Placeholder placeholder = new Placeholder();
        placeholder.getParameters().put(PackageDependenciesContentGenerator.JAR_RELATIVE_PATHS_PIPE_SEPARATED,
            "irrelevantDependencies");
        String message = EXPECTED_STRING_NOT_FOUND +
            PackageDependenciesContentGenerator.PARAMETER_WITH_NAME +
            PackageDependenciesContentGenerator.DEPENDANT_PACKAGE + "'";
        try {
            generator.generate(placeholder, null);
        } catch (SubstitutionRuntimeException e) {
            assertTrue(message, e.getMessage().contains(
                PackageDependenciesContentGenerator.PARAMETER_WITH_NAME +
                PackageDependenciesContentGenerator.DEPENDANT_PACKAGE));
            return;
        }
        throw new AssertionFailedError(message);
    }

    @Test
    public void testDependenciesPackagesExists() {
        PackageDependenciesContentGenerator generator = new PackageDependenciesContentGenerator();
        Placeholder placeholder = new Placeholder();
        placeholder.getParameters().put(PackageDependenciesContentGenerator.JAR_RELATIVE_PATHS_PIPE_SEPARATED,
            "irrelevant");
        placeholder.getParameters().put(PackageDependenciesContentGenerator.DEPENDANT_PACKAGE,
            "someDependantPackages");
        String message = EXPECTED_STRING_NOT_FOUND +
            PackageDependenciesContentGenerator.PARAMETER_WITH_NAME +
            PackageDependenciesContentGenerator.DEPENDENCIES_PACKAGES_PIPE_SEPARATED + "'";
        try {
            generator.generate(placeholder, null);
        } catch (SubstitutionRuntimeException e) {
            assertTrue(message + ", but found " + e.getMessage(),
                e.getMessage().contains(
                    PackageDependenciesContentGenerator.PARAMETER_WITH_NAME +
                    PackageDependenciesContentGenerator.DEPENDENCIES_PACKAGES_PIPE_SEPARATED));
            return;
        }
        throw new AssertionFailedError(message);
    }

    @Test
    public void testValidateJarExists() {
        PackageDependenciesContentGenerator generator = new PackageDependenciesContentGenerator();
        Placeholder placeholder = new Placeholder();
        placeholder.getParameters().put(PackageDependenciesContentGenerator.JAR_RELATIVE_PATHS_PIPE_SEPARATED,
            "irrelevant");
        placeholder.getParameters().put(PackageDependenciesContentGenerator.DEPENDANT_PACKAGE,
            "someDependantPackage");
        placeholder.getParameters().put(PackageDependenciesContentGenerator.DEPENDENCIES_PACKAGES_PIPE_SEPARATED,
            "someDependencyPackage");
        String message = EXPECTED_STRING_NOT_FOUND +
            PackageDependenciesContentGenerator.THE_FILE_CANNOT_BE_FOUND + "'";
        try {
            generator.generate(placeholder, null);
        } catch (SubstitutionRuntimeException e) {
            assertTrue(message + ", but found " + e.getMessage(),
                e.getMessage().contains(PackageDependenciesContentGenerator.THE_FILE_CANNOT_BE_FOUND));
            return;
        }
        throw new AssertionFailedError(message);
    }

    @Test
    public void testEmptyResult() {
        PackageDependenciesContentGenerator generator = new PackageDependenciesContentGenerator();
        Placeholder placeholder = new Placeholder();
        placeholder.getParameters().put(PackageDependenciesContentGenerator.JAR_RELATIVE_PATHS_PIPE_SEPARATED,
            EXAMPLE_JAR);
        placeholder.getParameters().put(PackageDependenciesContentGenerator.DEPENDANT_PACKAGE,
            "someDependantPackage");
        placeholder.getParameters().put(PackageDependenciesContentGenerator.DEPENDENCIES_PACKAGES_PIPE_SEPARATED,
            "someDependencyPackage");
        placeholder.setDescription(DESCRIPTION);
        Content content = generator.generate(placeholder, null);
        assertEquals(PackageDependenciesContentGenerator.NOTE_NO_DEPENDENCIES_FOUND, content.getPlantUmlContent());
        assertTrue(CONSTANT + DESCRIPTION + CANNOT_BE_FOUND_IN_THE_CONTENT +
            " Placeholder Content: '" + content.getMarkdownContent() +
            "'", content.getMarkdownContent().contains(DESCRIPTION));
    }

    @Test
    public void testNonEmptyJarResult() {
        PackageDependenciesContentGenerator generator = new PackageDependenciesContentGenerator();
        Placeholder placeholder = new Placeholder();
        placeholder.getParameters().put(PackageDependenciesContentGenerator.JAR_RELATIVE_PATHS_PIPE_SEPARATED,
            EXAMPLE_JAR);
        placeholder.getParameters().put(PackageDependenciesContentGenerator.DEPENDANT_PACKAGE,
            COM_CREDIBLEDOC_PLANTUML_SVGGENERATOR);
        placeholder.getParameters().put(PackageDependenciesContentGenerator.DEPENDENCIES_PACKAGES_PIPE_SEPARATED,
            COM_CREDIBLEDOC_PLANTUML_EXCEPTION);
        placeholder.setDescription(DESCRIPTION);
        Content content = generator.generate(placeholder, null);
        assertTrue(
            TEXT + EXPECTED_UML_PART + CANNOT_BE_FOUND_IN_THE_CONTENT +
                CONTENT + content.getPlantUmlContent() +
            "'", content.getPlantUmlContent().contains(EXPECTED_UML_PART));

        assertTrue(
            CONSTANT + DESCRIPTION + CANNOT_BE_FOUND_IN_THE_CONTENT +
                CONTENT + content.getMarkdownContent() +
            "'", content.getMarkdownContent().contains(DESCRIPTION));

        String svg = SvgGeneratorService.getInstance().generateSvgFromPlantUml(content.getPlantUmlContent());
        assertFalse("Cannot find Graphviz. Please install the \"https://www.graphviz.org/download/\" tool. See more info in svg text lines:\n" + svg, svg.contains("No dot executable found"));
    }

    @Test
    public void testNonEmptySourceResult() {
        PackageDependenciesContentGenerator generator = new PackageDependenciesContentGenerator();
        Placeholder placeholder = new Placeholder();
        placeholder.getParameters().put(PackageDependenciesContentGenerator.SOURCE_RELATIVE_PATHS_PIPE_SEPARATED,
            TARGET_TEST_CLASSES_PATHS);
        placeholder.getParameters().put(PackageDependenciesContentGenerator.DEPENDANT_PACKAGE,
            COM_CREDIBLEDOC_PLANTUML_SVGGENERATOR);
        placeholder.getParameters().put(PackageDependenciesContentGenerator.DEPENDENCIES_PACKAGES_PIPE_SEPARATED,
            COM_CREDIBLEDOC_PLANTUML_EXCEPTION);
        placeholder.setDescription(DESCRIPTION);
        Content content = generator.generate(placeholder, null);
        assertTrue(
            TEXT + EXPECTED_UML_PART + CANNOT_BE_FOUND_IN_THE_CONTENT +
                CONTENT + content.getPlantUmlContent() +
            "'", content.getPlantUmlContent().contains(EXPECTED_UML_PART));

        assertTrue(
            CONSTANT + DESCRIPTION + CANNOT_BE_FOUND_IN_THE_CONTENT +
                CONTENT + content.getMarkdownContent() +
            "'", content.getMarkdownContent().contains(DESCRIPTION));
    }

    @Test
    public void testIgnoreInnerPackages() {
        PackageDependenciesContentGenerator generator = new PackageDependenciesContentGenerator();
        Placeholder placeholder = new Placeholder();
        placeholder.getParameters().put(PackageDependenciesContentGenerator.SOURCE_RELATIVE_PATHS_PIPE_SEPARATED,
            TARGET_TEST_CLASSES_PATHS);
        placeholder.getParameters().put(PackageDependenciesContentGenerator.DEPENDANT_PACKAGE,
            COM_CREDIBLEDOC_PLANTUML_SVGGENERATOR);
        placeholder.getParameters().put(PackageDependenciesContentGenerator.DEPENDENCIES_PACKAGES_PIPE_SEPARATED,
            COM_CREDIBLEDOC_PLANTUML_EXCEPTION);
        placeholder.getParameters().put(PackageDependenciesContentGenerator.IGNORE_INNER_PACKAGES,
            "true");
        placeholder.setDescription(DESCRIPTION);
        Content content = generator.generate(placeholder, null);
        assertTrue(
            TEXT + EXPECTED_UML_PART + CANNOT_BE_FOUND_IN_THE_CONTENT +
                CONTENT + content.getPlantUmlContent() +
            "'", content.getPlantUmlContent().contains(EXPECTED_UML_PART));

        assertTrue(
            CONSTANT + DESCRIPTION + CANNOT_BE_FOUND_IN_THE_CONTENT +
                CONTENT + content.getMarkdownContent() +
            "'", content.getMarkdownContent().contains(DESCRIPTION));
    }
}
