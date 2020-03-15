package com.credibledoc.combiner.config;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConfigServiceTest {

    @Test
    public void loadConfig() {
        ConfigService configService = new ConfigService();

        Config config = configService.loadConfig(null);
        
        assertNotNull(config);
    }
}
