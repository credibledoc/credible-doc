package com.credibledoc.substitution.doc.module.substitution.activity;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.substitution.doc.reportdocument.ReportDocument;
import com.credibledoc.substitution.doc.searchcommand.SearchCommand;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnyLineSearchCommand implements SearchCommand {

    @Override
    public boolean isApplicable(ReportDocument reportDocument, List<String> multiLine, LogBufferedReader logBufferedReader) {
        return true;
    }
}
