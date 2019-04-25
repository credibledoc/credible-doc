package com.credibledoc.combiner.doc.module.combiner;

import com.credibledoc.combiner.application.Application;
import com.credibledoc.combiner.application.identifier.ApplicationIdentifier;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.doc.module.combiner.application.Substitution;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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
        return line.contains("com.credibledoc.combiner.doc");
    }

    @Override
    public Application getApplication() {
        return substitution;
    }
}
