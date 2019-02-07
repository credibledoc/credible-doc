package com.credibledoc.plantuml.svggenerator;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test of the {@link SvgGeneratorService} class.
 */
public class SvgGeneratorServiceTest {

    /**
     * Test the {@link SvgGeneratorService#generateSvgFromPlantUml(String)} method.
     * The generated svg should not be empty.
     */
    @Test
    public void generateSvgFromPlantUml() {
        String plantUml = "Bob -> Alice : hello \n Alice -> Bob : hi";
        String svg = SvgGeneratorService.getInstance().generateSvgFromPlantUml(plantUml);
        Assert.assertNotNull(svg);
    }
}
