package com.credibledoc.substitution.doc.module.substitution.activity.modules;

import com.credibledoc.combiner.context.CombinerContext;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.log.reader.ReaderService;
import com.credibledoc.generator.CredibleDocGeneratorMain;
import com.credibledoc.plantuml.svggenerator.SvgGeneratorService;
import com.credibledoc.substitution.content.generator.jar.LocalJarNameContentGenerator;
import com.credibledoc.substitution.core.resource.ResourceService;
import com.credibledoc.enricher.printable.Printable;
import com.credibledoc.substitution.doc.module.substitution.exception.SubstitutionDocRuntimeException;
import com.credibledoc.substitution.doc.module.substitution.logmessage.LogMessageService;
import com.credibledoc.enricher.transformer.Transformer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

/**
 * Create a part of PlantUML activity diagram, for example
 * <pre>
 *     |Swimlane1|
 *         :foo4;
 * </pre>
 * from log lines, for example
 * <i>04.03.2019 18:41:13.658|main|INFO |com.credibledoc.substitution.core.configuration.ConfigurationService - Properties loaded by ClassLoader from the resource: file..</i>.
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class ModulesActivityTransformer implements Transformer {

    public static final String MODULE_NAME = "substitution-reporting";
    
    @NonNull
    public final LogMessageService logMessageService;

    private static final String PLANTUML_CORE_MODULE_NAME = "plantuml-core";
    private static final Map<String, String> packagePrefixToModuleName = new HashMap<>();

    static {
        packagePrefixToModuleName.put("com.credibledoc.substitution.core",
            ResourceService.SUBSTITUTION_CORE_MODULE_NAME);
        packagePrefixToModuleName.put("com.credibledoc.substitution.doc",
            CredibleDocGeneratorMain.CREDIBLE_DOC_GENERATOR);
        packagePrefixToModuleName.put("com.credibledoc.plantuml",
            PLANTUML_CORE_MODULE_NAME);
        packagePrefixToModuleName.put("com.credibledoc.combiner",
            ReaderService.COMBINER_CORE_MODULE_NAME);
        packagePrefixToModuleName.put("com.credibledoc.substitution.content.generator",
            LocalJarNameContentGenerator.MODULE_NAME);
        packagePrefixToModuleName.put("com.credibledoc.substitution.reporting",
            MODULE_NAME);
        packagePrefixToModuleName.put("org.springframework.context.annotation",
            "spring-libraries");
        packagePrefixToModuleName.put("com.credibledoc.generator",
            CredibleDocGeneratorMain.MODULE_NAME);

        // Should be here for activating of the "com.credibledoc.plantuml" class loader
        Package svgGeneratorPackage = SvgGeneratorService.class.getPackage();
        Package generatorJarPackage = LocalJarNameContentGenerator.class.getPackage();
        log.trace("Class loaders activated: '{}', '{}'.", svgGeneratorPackage, generatorJarPackage);
        validatePackagesExist();
    }

    private static void validatePackagesExist() {
        Set<String> foundPrefixes = new HashSet<>();
        for (Package pkg : Package.getPackages()) {
            String name = pkg.getName();
            for (String prefix : packagePrefixToModuleName.keySet()) {
                if (name.startsWith(prefix)) {
                    foundPrefixes.add(prefix);
                    if (foundPrefixes.size() == packagePrefixToModuleName.size()) {
                        return;
                    }
                }
            }
        }
        Set<String> missingPrefixes = new HashSet<>(packagePrefixToModuleName.keySet());
        missingPrefixes.removeAll(foundPrefixes);

        throw new SubstitutionDocRuntimeException("Package(s) not found: " + missingPrefixes);
    }

    @Override
    public String transform(Printable printable,
                            List<String> multiLine, LogBufferedReader logBufferedReader, CombinerContext combinerContext) {
        String line = multiLine.get(0);
        String canonicalClassName = parseClassName(line);
        String moduleName = findModuleName(canonicalClassName);

        int maxRowLength = moduleName.length() * 2 + moduleName.length() / 2;
        List<String> cacheLines = printable.getCacheLines();
        addMessageToCache(multiLine, moduleName, maxRowLength, cacheLines);

        return null;
    }

    private String findModuleName(String canonicalClassName) {
        for (Map.Entry<String, String> entry : packagePrefixToModuleName.entrySet()) {
            if (canonicalClassName.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        throw new SubstitutionDocRuntimeException("Module name cannot be found for package: " + canonicalClassName);
    }

    private void addMessageToCache(List<String> multiLine, String canonicalClassName, int maxRowLength,
                                   List<String> cacheLines) {
        String message = logMessageService.parseMessage(multiLine.get(0), maxRowLength);
        String result = "|" + canonicalClassName + "|" + LogMessageService.LINE_SEPARATOR +
            LogMessageService.FOUR_SPACES + ":" + message + ";" + LogMessageService.LINE_SEPARATOR;
        cacheLines.add(result);
    }

    private String parseClassName(String line) {
        int separatorIndex = line.indexOf(LogMessageService.LOG_SEPARATOR);
        String firstPart = line.substring(0, separatorIndex);
        int startIndex = firstPart.lastIndexOf('|') + 1;
        return firstPart.substring(startIndex);
    }
}
