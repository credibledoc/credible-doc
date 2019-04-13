package com.credibledoc.substitution.doc.module.substitution.application;

import com.credibledoc.substitution.doc.filesmerger.application.Application;
import com.credibledoc.substitution.doc.filesmerger.specific.SpecificTactic;
import com.credibledoc.substitution.doc.module.substitution.SubstitutionSpecificTactic;
import org.springframework.stereotype.Service;

@Service
public class Substitution implements Application {
    @Override
    public Class<? extends SpecificTactic> getSpecificTacticClass() {
        return SubstitutionSpecificTactic.class;
    }

    @Override
    public String getShortName() {
        return "placeholder-substitution";
    }
}
