package org.credibledoc.substitution.doc.module.substitution.launching;

import com.credibledoc.substitution.core.configuration.ConfigurationService;
import org.credibledoc.substitution.doc.filesmerger.log.buffered.LogBufferedReader;
import org.credibledoc.substitution.doc.reportdocument.ReportDocument;
import org.credibledoc.substitution.doc.searchcommand.SearchCommand;
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
