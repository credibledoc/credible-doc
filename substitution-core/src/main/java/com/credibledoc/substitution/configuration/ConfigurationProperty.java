package com.credibledoc.substitution.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class fields marked as a {@link ConfigurationProperty} represents items of a properties file.
 * <p>
 * This field should have a setter method.
 * <p>
 * Example of usage:
 * <pre>
 *         &#64;ConfigurationProperty(key = "unique.configuration.key.of.templates.resource",
 *             defaultValue = "Default value of the 'templatesResource' field")
 *
 *         private String templatesResource;
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigurationProperty {
    /**
     * @return For example unique.configuration.key.of.templates.resource
     */
    String key();

    /**
     * @return A default value of this field. It can be 'null'.
     */
    String defaultValue();
}
