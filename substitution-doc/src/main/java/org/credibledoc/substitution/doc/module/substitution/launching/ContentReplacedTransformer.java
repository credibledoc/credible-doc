package org.credibledoc.substitution.doc.module.substitution.launching;

import com.credibledoc.substitution.core.placeholder.Placeholder;
import com.credibledoc.substitution.core.placeholder.PlaceholderService;
import lombok.RequiredArgsConstructor;
import org.credibledoc.substitution.doc.filesmerger.log.buffered.LogBufferedReader;
import org.credibledoc.substitution.doc.markdown.MarkdownService;
import org.credibledoc.substitution.doc.reportdocument.ReportDocument;
import org.credibledoc.substitution.doc.transformer.Transformer;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ContentReplacedTransformer implements Transformer {

    private static final String LINE_SEPARATOR = System.lineSeparator();

    @Override
    public String transform(ReportDocument reportDocument,
                            List<String> multiLine, LogBufferedReader logBufferedReader) {

        String plantUml = ":" + MarkdownService.CONTENT_REPLACED + ";" + LINE_SEPARATOR +
            "note right" + LINE_SEPARATOR +
            parsePlaceholderDescription(multiLine) + LINE_SEPARATOR +
            "end note" + LINE_SEPARATOR;

        reportDocument.getCacheLines().add(plantUml);

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
