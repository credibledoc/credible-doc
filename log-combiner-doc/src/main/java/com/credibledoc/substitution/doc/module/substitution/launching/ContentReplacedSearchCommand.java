package com.credibledoc.substitution.doc.module.substitution.launching;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.substitution.doc.markdown.MarkdownService;
import com.credibledoc.substitution.doc.reportdocument.ReportDocument;
import com.credibledoc.substitution.doc.searchcommand.SearchCommand;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContentReplacedSearchCommand implements SearchCommand {

    @Override
    public boolean isApplicable(ReportDocument reportDocument,
                                List<String> multiLine, LogBufferedReader logBufferedReader) {
        return multiLine.get(0).contains(MarkdownService.CONTENT_REPLACED);
    }
}
