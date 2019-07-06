package com.credibledoc.substitution.doc.module.substitution.launching;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.enricher.printable.Printable;
import com.credibledoc.enricher.transformer.Transformer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FinishedTransformer implements Transformer {

    private static final String LINE_SEPARATOR = System.lineSeparator();

    @Override
    public String transform(Printable printable, List<String> multiLine, LogBufferedReader logBufferedReader) {
        String result = "stop" + LINE_SEPARATOR;

        printable.getCacheLines().add(result);

        return null;
    }
}
