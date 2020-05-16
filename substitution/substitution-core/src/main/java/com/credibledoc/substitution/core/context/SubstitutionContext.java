package com.credibledoc.substitution.core.context;

import com.credibledoc.substitution.core.configuration.Configuration;
import com.credibledoc.substitution.core.configuration.ConfigurationService;
import com.credibledoc.substitution.core.placeholder.PlaceholderRepository;
import com.credibledoc.substitution.core.tracking.TrackableRepository;

/**
 * Contains data related to the current application instance.
 * 
 * @author Kyrylo Semenko
 */
public class SubstitutionContext {
    /**
     * Contains state of current {@link com.credibledoc.substitution.core.placeholder.Placeholder}s parsed from templates.
     */
    private PlaceholderRepository placeholderRepository;

    /**
     * Contains {@link com.credibledoc.substitution.core.tracking.Trackable} pairs state.
     */
    private TrackableRepository trackableRepository;

    /**
     * Contains the {@link Configuration} state.
     */
    private Configuration configuration;

    @Override
    public String toString() {
        return "SubstitutionContext{" +
            "placeholderRepository=" + placeholderRepository +
            ", configuration=" + configuration +
            '}';
    }

    /**
     * @return The {@link #placeholderRepository} field value.
     */
    public PlaceholderRepository getPlaceholderRepository() {
        return placeholderRepository;
    }

    /**
     * @param placeholderRepository see the {@link #placeholderRepository} field description.
     */
    public void setPlaceholderRepository(PlaceholderRepository placeholderRepository) {
        this.placeholderRepository = placeholderRepository;
    }

    /**
     * @return The {@link #trackableRepository} field value.
     */
    public TrackableRepository getTrackableRepository() {
        return trackableRepository;
    }

    /**
     * @param trackableRepository see the {@link #trackableRepository} field description.
     */
    public void setTrackableRepository(TrackableRepository trackableRepository) {
        this.trackableRepository = trackableRepository;
    }

    /**
     * Create a new instances of {@link PlaceholderRepository}
     * and {@link TrackableRepository}.
     * @return the current instance of {@link SubstitutionContext}.
     */
    public SubstitutionContext init() {
        this.placeholderRepository = new PlaceholderRepository();
        this.trackableRepository = new TrackableRepository();
        return this;
    }

    /**
     * Create a new instance of {@link Configuration} object and call the
     * {@link ConfigurationService#loadConfiguration(Configuration)} method.
     * @return the current instance of {@link SubstitutionContext}.
     */
    public SubstitutionContext loadConfiguration() {
        this.configuration = new Configuration();
        ConfigurationService.getInstance().loadConfiguration(configuration);
        return this;
    }

    /**
     * @return The {@link #configuration} field value.
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * @param configuration see the {@link #configuration} field description.
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
