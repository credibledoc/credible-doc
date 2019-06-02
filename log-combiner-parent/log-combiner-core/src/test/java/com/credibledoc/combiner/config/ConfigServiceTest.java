package com.credibledoc.combiner.config;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConfigServiceTest {

    @Test
    public void loadConfig() {
        ConfigService configService = ConfigService.getInstance();

        Config config = configService.loadConfig(null);
        
        assertNotNull(config);
    }
}
