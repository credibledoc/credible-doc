package org.credibledoc.substitution.doc.module.substitution;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.credibledoc.substitution.doc.filesmerger.application.Application;
import org.credibledoc.substitution.doc.filesmerger.application.identifier.ApplicationIdentifier;
import org.credibledoc.substitution.doc.filesmerger.log.buffered.LogBufferedReader;
import org.credibledoc.substitution.doc.module.substitution.application.Substitution;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * {@link ApplicationIdentifier} of {@link Substitution} {@link Application}.
 *
 * @author Kyrylo Semenko
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SubstitutionApplicationIdentifier implements ApplicationIdentifier {

    @NonNull
    private final Substitution substitution;

    @Override
    public boolean identifyApplication(String line, LogBufferedReader logBufferedReader) {
        return line.contains("org.credibledoc.substitution.doc");
    }

    @Override
    public Application getApplication() {
        return substitution;
    }
}
