package org.credibledoc.substitution.doc.module.substitution.launching;

import com.credibledoc.substitution.core.configuration.ConfigurationService;
import org.credibledoc.substitution.doc.filesmerger.log.buffered.LogBufferedReader;
import org.credibledoc.substitution.doc.reportdocument.ReportDocument;
import org.credibledoc.substitution.doc.transformer.Transformer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigurationLoadingTransformer implements Transformer {

    private static final String LINE_SEPARATOR = System.lineSeparator();

    @Override
    public String transform(ReportDocument reportDocument,
                            List<String> multiLine, LogBufferedReader logBufferedReader) {

        String plantUml = ":" + "Configuration properties loaded" + ";" + LINE_SEPARATOR +
            "note right" + LINE_SEPARATOR +
            parseFileName(multiLine) + LINE_SEPARATOR +
            "end note" + LINE_SEPARATOR;

        reportDocument.getCacheLines().add(plantUml);

        return null;
    }

    private String parseFileName(List<String> multiLine) {
        String line = multiLine.get(0);
        int beginIndex = line.indexOf(ConfigurationService.PROPERTIES_LOADED_BY_CLASS_LOADER_FROM_THE_RESOURCE);
        return line.substring(beginIndex +
            ConfigurationService.PROPERTIES_LOADED_BY_CLASS_LOADER_FROM_THE_RESOURCE.length());
    }
}
