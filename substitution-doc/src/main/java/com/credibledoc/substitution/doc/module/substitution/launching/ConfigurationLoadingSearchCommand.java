package com.credibledoc.substitution.doc.module.substitution.launching;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.substitution.core.configuration.ConfigurationService;
import com.credibledoc.enricher.deriving.Deriving;
import com.credibledoc.enricher.searchcommand.SearchCommand;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigurationLoadingSearchCommand implements SearchCommand {

    @Override
    public boolean isApplicable(Deriving deriving,
                                List<String> multiLine, LogBufferedReader logBufferedReader) {
        return multiLine.get(0).contains(ConfigurationService.PROPERTIES_LOADED_BY_CLASS_LOADER_FROM_THE_RESOURCE);
    }
}
