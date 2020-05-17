package com.credibledoc.substitution.core.tracking;

import com.credibledoc.substitution.core.pair.Pair;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains pairs of fragments and templates for tracking fragments' changes. If the fragment is changed,
 * the template content will be generated too.
 * 
 * @author Kyrylo Semenko
 */
public class TrackableRepository {

    /**
     * Pairs of {@link Path}s where a key is a fragment of a template and a value is the template.
     * See the {@link TrackableRepository} class description.
     */
    private final List<Pair<Path, Path>> pairs;

    /**
     * Initialize the {@link #pairs} list.
     */
    public TrackableRepository() {
        pairs = new ArrayList<>();
    }

    /**
     * @return The {@link #pairs} field value.
     */
    public List<Pair<Path, Path>> getPairs() {
        return pairs;
    }
}
