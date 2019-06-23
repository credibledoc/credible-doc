package com.credibledoc.substitution.doc.module.substitution.activity.anyline;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.enricher.printable.Printable;
import com.credibledoc.enricher.searchcommand.SearchCommand;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnyLineSearchCommand implements SearchCommand {

    @Override
    public boolean isApplicable(Printable printable, List<String> multiLine, LogBufferedReader logBufferedReader) {
        return true;
    }
}
