package com.credibledoc.substitution.doc.module.substitution.launching;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.substitution.doc.reportdocument.ReportDocument;
import com.credibledoc.substitution.doc.transformer.Transformer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FinishedTransformer implements Transformer {

    private static final String LINE_SEPARATOR = System.lineSeparator();

    @Override
    public String transform(ReportDocument reportDocument, List<String> multiLine, LogBufferedReader logBufferedReader) {
        String result = "stop" + LINE_SEPARATOR;

        reportDocument.getCacheLines().add(result);

        return null;
    }
}
