package com.credibledoc.substitution.doc.module.substitution.launching;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.substitution.doc.SubstitutionDocMain;
import com.credibledoc.enricher.deriving.Deriving;
import com.credibledoc.enricher.transformer.Transformer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LaunchingTransformer implements Transformer {

    private static final String LINE_SEPARATOR = System.lineSeparator();

    @Override
    public String transform(Deriving deriving, List<String> multiLine, LogBufferedReader logBufferedReader) {
        String result = "start" + LINE_SEPARATOR +
            ":" + SubstitutionDocMain.APPLICATION_SUBSTITUTION_DOC_LAUNCHED + ";" +
            LINE_SEPARATOR;

        deriving.getCacheLines().add(result);

        return null;
    }
}
