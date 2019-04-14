package com.credibledoc.substitution.doc.module.substitution.application;

import com.credibledoc.combiner.application.Application;
import com.credibledoc.combiner.tactic.Tactic;
import com.credibledoc.substitution.doc.module.substitution.SubstitutionTactic;
import org.springframework.stereotype.Service;

@Service
public class Substitution implements Application {
    @Override
    public Class<? extends Tactic> getSpecificTacticClass() {
        return SubstitutionTactic.class;
    }

    @Override
    public String getShortName() {
        return "placeholder-substitution";
    }
}
