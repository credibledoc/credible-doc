package org.credibledoc.substitution.doc.module.substitution.jar;

import com.credibledoc.substitution.core.content.ContentGenerator;
import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.core.placeholder.Placeholder;
import lombok.RequiredArgsConstructor;
import org.credibledoc.substitution.doc.SubstitutionDocMain;
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
                throw new SubstitutionRuntimeException("Jar name cannot be found. " +
                    "Target directory does not exists: '" + targetDirectory.getAbsolutePath() +
                    "'. Please run 'mvn install' first");
            }
            File[] files = targetDirectory.listFiles();
            if (files == null) {
                throw new SubstitutionRuntimeException("Local variable 'files' is null. " +
                    "TargetDirectory: " + targetDirectory.getAbsolutePath());
            }
            for (File file : files) {
                String name = file.getName();
                if (name.startsWith(SubstitutionDocMain.SUBSTITUTION_DOC) && name.endsWith(".jar")) {
                    return name;
                }
            }
            throw new SubstitutionRuntimeException("Jar name cannot be found. " +
                "Target directory '" + targetDirectory.getAbsolutePath() +
                "' has no jar file with '" + SubstitutionDocMain.SUBSTITUTION_DOC + "' prefix. Please run 'mvn install' first");
        } catch (Exception e) {
            throw new SubstitutionRuntimeException(e);
        }
    }

}
