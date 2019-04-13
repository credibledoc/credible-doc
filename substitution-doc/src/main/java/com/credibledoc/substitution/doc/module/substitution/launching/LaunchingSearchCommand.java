package com.credibledoc.substitution.doc.module.substitution.launching;

import com.credibledoc.substitution.doc.SubstitutionDocMain;
import com.credibledoc.substitution.doc.filesmerger.log.buffered.LogBufferedReader;
import com.credibledoc.substitution.doc.reportdocument.ReportDocument;
import com.credibledoc.substitution.doc.searchcommand.SearchCommand;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LaunchingSearchCommand implements SearchCommand {

    @Override
    public boolean isApplicable(ReportDocument reportDocument, List<String> multiLine, LogBufferedReader logBufferedReader) {
        return multiLine.get(0).contains(SubstitutionDocMain.APPLICATION_SUBSTITUTION_DOC_LAUNCHED);
    }
}
