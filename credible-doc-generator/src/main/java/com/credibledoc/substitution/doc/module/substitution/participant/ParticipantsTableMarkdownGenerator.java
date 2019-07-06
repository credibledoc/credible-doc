package com.credibledoc.substitution.doc.module.substitution.participant;

import com.credibledoc.substitution.core.content.Content;
import com.credibledoc.substitution.core.content.ContentGenerator;
import com.credibledoc.substitution.core.placeholder.Placeholder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.steppschuh.markdowngenerator.table.Table;
import com.credibledoc.substitution.doc.module.substitution.markdown.table.TableService;
import com.credibledoc.substitution.doc.plantuml.Participant;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Generates a table of {@link Participant}s maintained in the credible-doc-generator module.
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ParticipantsTableMarkdownGenerator implements ContentGenerator {

    @NonNull
    private final TableService tableService;

    @Override
    public Content generate(Placeholder placeholder) {
        Table.Builder tableBuilder = new Table.Builder()
                .addRow("Participant name", "Description");

        String markdown = tableService.createMarkdownTableFromEnum(tableBuilder, Participant.class);
        Content content = new Content();
        content.setMarkdownContent(markdown);
        return content;
    }

}
