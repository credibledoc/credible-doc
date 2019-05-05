package com.credibledoc.substitution.doc.module.substitution.launching;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.substitution.doc.SubstitutionDocMain;
import com.credibledoc.enricher.deriving.Deriving;
import com.credibledoc.enricher.searchcommand.SearchCommand;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LaunchingSearchCommand implements SearchCommand {

    @Override
    public boolean isApplicable(Deriving deriving, List<String> multiLine, LogBufferedReader logBufferedReader) {
        return multiLine.get(0).contains(SubstitutionDocMain.APPLICATION_SUBSTITUTION_DOC_LAUNCHED);
    }
}
