package com.credibledoc.substitution.doc.module.substitution.activity;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.enricher.deriving.Deriving;
import com.credibledoc.enricher.searchcommand.SearchCommand;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnyLineSearchCommand implements SearchCommand {

    @Override
    public boolean isApplicable(Deriving deriving, List<String> multiLine, LogBufferedReader logBufferedReader) {
        return true;
    }
}
