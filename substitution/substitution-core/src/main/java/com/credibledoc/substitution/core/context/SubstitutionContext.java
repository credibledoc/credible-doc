package com.credibledoc.substitution.core.context;

import com.credibledoc.substitution.core.configuration.ConfigurationService;
import com.credibledoc.substitution.core.placeholder.PlaceholderRepository;

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
     * Contains the {@link ConfigurationService} state.
     */
    // TODO Kyrylo Semenko - delete and use Configuration directly. Remain ConfigurationService stateless.
    private ConfigurationService configurationService;


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
     * Create a new instance of {@link PlaceholderRepository}.
     * @return the current instance of {@link SubstitutionContext}.
     */
    public SubstitutionContext init() {
        this.placeholderRepository = new PlaceholderRepository();
        return this;
    }

    /**
     * Call the {@link ConfigurationService#loadConfiguration()} method.
     * @return the current instance of {@link SubstitutionContext}.
     */
    public SubstitutionContext loadConfiguration() {
        this.configurationService = new ConfigurationService();
        this.configurationService.loadConfiguration();
        return this;
    }

    /**
     * @return The {@link #configurationService} field value.
     */
    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    /**
     * @param configurationService see the {@link #configurationService} field description.
     */
    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
}
