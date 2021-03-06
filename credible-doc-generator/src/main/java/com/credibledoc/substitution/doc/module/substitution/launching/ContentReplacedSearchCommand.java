package com.credibledoc.substitution.doc.module.substitution.launching;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.enricher.printable.Printable;
import com.credibledoc.enricher.searchcommand.SearchCommand;
import com.credibledoc.substitution.reporting.replacement.ReplacementService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContentReplacedSearchCommand implements SearchCommand {

    @Override
    public boolean isApplicable(Printable printable,
                                List<String> multiLine, LogBufferedReader logBufferedReader) {
        return multiLine.get(0).contains(ReplacementService.CONTENT_REPLACED);
    }
}
