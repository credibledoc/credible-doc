package com.credibledoc.substitution.doc.module.substitution;

import com.credibledoc.substitution.doc.filesmerger.log.buffered.LogBufferedReader;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import com.credibledoc.substitution.doc.filesmerger.application.Application;
import com.credibledoc.substitution.doc.filesmerger.application.identifier.ApplicationIdentifier;
import com.credibledoc.substitution.doc.module.substitution.application.Substitution;
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
        return line.contains("com.credibledoc.substitution.doc");
    }

    @Override
    public Application getApplication() {
        return substitution;
    }
}
