package com.credibledoc.substitution.doc.module.substitution.launching;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.substitution.core.placeholder.Placeholder;
import com.credibledoc.substitution.core.placeholder.PlaceholderService;
import com.credibledoc.enricher.deriving.Deriving;
import com.credibledoc.substitution.reporting.markdown.MarkdownService;
import com.credibledoc.enricher.transformer.Transformer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ContentReplacedTransformer implements Transformer {

    private static final String LINE_SEPARATOR = System.lineSeparator();

    @Override
    public String transform(Deriving deriving,
                            List<String> multiLine, LogBufferedReader logBufferedReader) {

        String plantUml = ":" + MarkdownService.CONTENT_REPLACED + ";" + LINE_SEPARATOR +
            "note right" + LINE_SEPARATOR +
            parsePlaceholderDescription(multiLine) + LINE_SEPARATOR +
            "end note" + LINE_SEPARATOR;

        deriving.getCacheLines().add(plantUml);

        return null;
    }

    private String parsePlaceholderDescription(List<String> multiLine) {
        String line = multiLine.get(0);
        int beginIndex = line.indexOf(MarkdownService.CONTENT_REPLACED);
        String json = line.substring(beginIndex +  MarkdownService.CONTENT_REPLACED.length());
        Placeholder placeholder = PlaceholderService.getInstance().readPlaceholderFromJson(json);
        return placeholder.getDescription();
    }

}
