package com.credibledoc.combiner.doc.module.combiner.jar;

import com.credibledoc.combiner.doc.CombinerDocMain;
import com.credibledoc.substitution.core.content.ContentGenerator;
import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.substitution.core.placeholder.Placeholder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.File;

/**
 * Generates the published jar name. Tries to find the current jar name in the <b>target</b> directory.
 *
 * @author Kyrylo Semenko
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SubstitutionDocJarNameContentGenerator implements ContentGenerator {

    @Override
    public String generate(Placeholder placeholder) {
        try {
            File targetDirectory = new File("target");
            if (!targetDirectory.exists()) {
                throw new CombinerRuntimeException("Jar name cannot be found. " +
                    "Target directory does not exists: '" + targetDirectory.getAbsolutePath() +
                    "'. Please run 'mvn install' first");
            }
            File[] files = targetDirectory.listFiles();
            if (files == null) {
                throw new CombinerRuntimeException("Local variable 'files' is null. " +
                    "TargetDirectory: " + targetDirectory.getAbsolutePath());
            }
            for (File file : files) {
                String name = file.getName();
                if (name.startsWith(CombinerDocMain.SUBSTITUTION_DOC) && name.endsWith(".jar")) {
                    return name;
                }
            }
            throw new CombinerRuntimeException("Jar name cannot be found. " +
                "Target directory '" + targetDirectory.getAbsolutePath() +
                "' has no jar file with '" + CombinerDocMain.SUBSTITUTION_DOC + "' prefix. Please run 'mvn install' first");
        } catch (Exception e) {
            throw new CombinerRuntimeException(e);
        }
    }

}
