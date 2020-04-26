package com.credibledoc.substitution.core.content;

import com.credibledoc.substitution.core.configuration.Configuration;
import com.credibledoc.substitution.core.context.SubstitutionContext;
import com.credibledoc.substitution.core.placeholder.Placeholder;

/**
 * Classes which implement this interface has a single method
 * {@link #generate(Placeholder, SubstitutionContext)}. Links to these classes are defined in templates
 * stored in the {@link Configuration#getTemplatesResource()} directory. These
 * templates has tags, content of these tags is parsed and stored in {@link Placeholder}
 * objects, where class name is stored in the
 * {@link Placeholder#setClassName(String)} field.
 *
 * @author Kyrylo Semenko
 */
public interface ContentGenerator {

    /**
     * Generate a part of a document, for example markdown. This part will be placed
     * instead of placeholder, that begins with
     * {@link Configuration#getPlaceholderBegin()}
     * and ends with
     * {@link Configuration#getPlaceholderEnd()}
     * tags.
     *
     * @param placeholder its properties can be used for customization of content generation.
     * @param substitutionContext the current state of the instance.
     * @return Generated {@link Content}
     */
    Content generate(Placeholder placeholder, SubstitutionContext substitutionContext);
}
