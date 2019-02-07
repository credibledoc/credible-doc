package com.credibledoc.plantuml.tooltip;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests of the {@link TooltipService} class.
 *
 * @author Kyrylo Semenko
 */
public class TooltipServiceTest {

    /**
     * Test the {@link TooltipService#generateTooltip(String)} method.
     */
    @Test
    public void generateTooltip() {
        String tooltip = "tooltip";
        String result = TooltipService.getInstance().generateTooltip(tooltip);
        assertEquals(" {tooltip}", result);
    }
}
