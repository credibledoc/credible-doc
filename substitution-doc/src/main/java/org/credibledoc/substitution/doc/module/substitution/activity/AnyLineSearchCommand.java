package org.credibledoc.substitution.doc.module.substitution.activity;

import org.credibledoc.substitution.doc.log.buffered.LogBufferedReader;
import org.credibledoc.substitution.doc.reportdocument.ReportDocument;
import org.credibledoc.substitution.doc.searchcommand.SearchCommand;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnyLineSearchCommand implements SearchCommand {

    @Override
    public boolean isApplicable(ReportDocument reportDocument, List<String> multiLine, LogBufferedReader logBufferedReader) {
        return true;
    }
}
