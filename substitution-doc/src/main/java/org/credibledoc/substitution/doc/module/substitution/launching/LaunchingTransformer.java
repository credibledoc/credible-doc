package org.credibledoc.substitution.doc.module.substitution.launching;

import com.credibledoc.plantuml.link.LinkService;
import org.credibledoc.substitution.doc.SubstitutionDocMain;
import org.credibledoc.substitution.doc.log.buffered.LogBufferedReader;
import org.credibledoc.substitution.doc.plantuml.Participant;
import org.credibledoc.substitution.doc.plantuml.sequence.PlantUmlSequenceMessageArrow;
import org.credibledoc.substitution.doc.reportdocument.ReportDocument;
import org.credibledoc.substitution.doc.transformer.Transformer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LaunchingTransformer implements Transformer {
    @Override
    public String transform(ReportDocument reportDocument, List<String> multiLine, LogBufferedReader logBufferedReader) {
        String link = LinkService.getInstance().generateLink(SubstitutionDocMain.APPLICATION_SUBSTITUTION_DOC_LAUNCHED,
            multiLine.get(0),
            reportDocument.getLinkResource(),
            multiLine.get(0));

        String uml = Participant.SUBSTITUTION_DOC.getUml() + PlantUmlSequenceMessageArrow.FULL_ARROW.getUml() +
            Participant.SUBSTITUTION_DOC.getUml() +
            ": " + "Application stared" + " " + link;

        reportDocument.getCacheLines().add(uml);

        return null;
    }
}
