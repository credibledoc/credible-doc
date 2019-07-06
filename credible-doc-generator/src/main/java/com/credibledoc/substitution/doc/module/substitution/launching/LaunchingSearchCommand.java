package com.credibledoc.substitution.doc.module.substitution.launching;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.generator.CredibleDocGeneratorMain;
import com.credibledoc.enricher.printable.Printable;
import com.credibledoc.enricher.searchcommand.SearchCommand;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LaunchingSearchCommand implements SearchCommand {

    @Override
    public boolean isApplicable(Printable printable, List<String> multiLine, LogBufferedReader logBufferedReader) {
        return multiLine.get(0).contains(CredibleDocGeneratorMain.APPLICATION_SUBSTITUTION_DOC_LAUNCHED);
    }
}
