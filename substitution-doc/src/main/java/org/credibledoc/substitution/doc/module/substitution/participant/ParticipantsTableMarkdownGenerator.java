package org.credibledoc.substitution.doc.module.substitution.participant;

import com.credibledoc.substitution.core.content.ContentGenerator;
import com.credibledoc.substitution.core.placeholder.Placeholder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.steppschuh.markdowngenerator.table.Table;
import org.credibledoc.substitution.doc.markdown.generator.table.TableService;
import org.credibledoc.substitution.doc.plantuml.Participant;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Generates a table of {@link Participant}s maintained in the substitution-doc module.
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
