package com.credibledoc.combiner.doc.module.combiner.launching;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.doc.CombinerDocMain;
import com.credibledoc.combiner.doc.reportdocument.ReportDocument;
import com.credibledoc.combiner.doc.transformer.Transformer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LaunchingTransformer implements Transformer {

    private static final String LINE_SEPARATOR = System.lineSeparator();

    @Override
    public String transform(ReportDocument reportDocument, List<String> multiLine, LogBufferedReader logBufferedReader) {
        String result = "start" + LINE_SEPARATOR +
            ":" + CombinerDocMain.APPLICATION_SUBSTITUTION_DOC_LAUNCHED + ";" +
            LINE_SEPARATOR;

        reportDocument.getCacheLines().add(result);

        return null;
    }
}
