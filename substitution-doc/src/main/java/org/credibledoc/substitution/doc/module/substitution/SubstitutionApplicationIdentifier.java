package org.credibledoc.substitution.doc.module.substitution;

import org.credibledoc.substitution.doc.filesmerger.application.identifier.ApplicationIdentifier;
import org.credibledoc.substitution.doc.filesmerger.log.buffered.LogBufferedReader;
import org.credibledoc.substitution.doc.module.tactic.TacticHolder;
import org.springframework.stereotype.Service;

/**
 * {@link ApplicationIdentifier} of {@link TacticHolder#SUBSTITUTION}.
 *
 * @author Kyrylo Semenko
 */
@Service
public class SubstitutionApplicationIdentifier implements ApplicationIdentifier {

    @Override
    public boolean identifyApplication(String line, LogBufferedReader logBufferedReader) {
        return line.contains("org.credibledoc.substitution.doc");
    }

    @Override
    public TacticHolder getSpecificTacticHolder() {
        return TacticHolder.SUBSTITUTION;
    }
}
