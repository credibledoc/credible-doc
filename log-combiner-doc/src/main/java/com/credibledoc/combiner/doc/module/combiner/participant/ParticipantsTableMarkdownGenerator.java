package com.credibledoc.combiner.doc.module.combiner.participant;

import com.credibledoc.substitution.core.content.ContentGenerator;
import com.credibledoc.substitution.core.placeholder.Placeholder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.steppschuh.markdowngenerator.table.Table;
import com.credibledoc.combiner.doc.module.combiner.markdown.table.TableService;
import com.credibledoc.combiner.doc.plantuml.Participant;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Generates a table of {@link Participant}s maintained in the combiner-doc module.
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ParticipantsTableMarkdownGenerator implements ContentGenerator {

    @NonNull
    private final TableService tableService;

    @Override
    public String generate(Placeholder placeholder) {
        Table.Builder tableBuilder = new Table.Builder()
                .addRow("Participant name", "Description");

        return tableService.createMarkdownTableFromEnum(tableBuilder, Participant.class);
    }

}
