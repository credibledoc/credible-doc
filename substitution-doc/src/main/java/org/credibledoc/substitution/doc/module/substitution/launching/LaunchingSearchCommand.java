package org.credibledoc.substitution.doc.module.substitution.launching;

import org.credibledoc.substitution.doc.SubstitutionDocMain;
import org.credibledoc.substitution.doc.log.buffered.LogBufferedReader;
import org.credibledoc.substitution.doc.reportdocument.ReportDocument;
import org.credibledoc.substitution.doc.searchcommand.SearchCommand;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LaunchingSearchCommand implements SearchCommand {

    @Override
    public boolean isApplicable(ReportDocument reportDocument, List<String> multiLine, LogBufferedReader logBufferedReader) {
        return multiLine.get(0).contains(SubstitutionDocMain.APPLICATION_SUBSTITUTION_DOC_LAUNCHED);
    }
}
