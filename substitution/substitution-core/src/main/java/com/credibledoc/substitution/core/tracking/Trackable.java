package com.credibledoc.substitution.core.tracking;

import com.credibledoc.substitution.core.placeholder.Placeholder;

import java.nio.file.Path;
import java.util.List;

/**
 * The interface of the {@link Placeholder} {@link com.credibledoc.substitution.core.content.ContentGenerator} content
 * which should be appended to the tracking service
 * because its content should be published to a mother template immediately after changes.
 */
public interface Trackable {
    /**
     * @return File paths that will be tracked (or watched) for changes. Changes in the files content will
     * trigger generation of templates where these fragments are used.
     */
    List<Path> getFragmentPaths();
}
