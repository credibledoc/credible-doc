package com.credibledoc.combiner.doc.module.combiner.application;

import com.credibledoc.combiner.application.Application;
import com.credibledoc.combiner.tactic.Tactic;
import com.credibledoc.combiner.doc.module.combiner.SubstitutionTactic;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class Substitution implements Application {

    @NonNull
    SubstitutionTactic substitutionTactic;

    @Override
    public Tactic getTactic() {
        return substitutionTactic;
    }

    @Override
    public String getShortName() {
        return "placeholder-combiner";
    }
}
