package com.credibledoc.combiner.doc.module.combiner.launching;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.substitution.core.configuration.ConfigurationService;
import com.credibledoc.combiner.doc.reportdocument.ReportDocument;
import com.credibledoc.combiner.doc.searchcommand.SearchCommand;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigurationLoadingSearchCommand implements SearchCommand {

    @Override
    public boolean isApplicable(ReportDocument reportDocument,
                                List<String> multiLine, LogBufferedReader logBufferedReader) {
        return multiLine.get(0).contains(ConfigurationService.PROPERTIES_LOADED_BY_CLASS_LOADER_FROM_THE_RESOURCE);
    }
}
