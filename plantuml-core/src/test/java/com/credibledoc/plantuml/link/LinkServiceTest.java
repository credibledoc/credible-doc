package com.credibledoc.plantuml.link;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests of the {@link LinkService} class.
 *
 * @author Kyrylo Semenko
 */
@SuppressWarnings("ALL")
public class LinkServiceTest {

    /**
     * Test of the {@link LinkService#generateLink(String, String, String)} method.
     */
    @Test
    public void generateLink() {
        LinkService linkService = LinkService.getInstance();
        String text = "text";
        String multilineJoined = "multiline" + "\r\n" + "Joined";
        String reportDocumentLinkResource = "reportDocumentLinkResource";
        String link = linkService.generateLink(text, multilineJoined, reportDocumentLinkResource);
        assertEquals("[[reportDocumentLinkResource?search=multiline%0D%0AJoined  {multiline\\\\nJoined} text]]", link);
    }

    /**
     * Test of the {@link LinkService#generateLink(String, String, String, String)} method.
     */
    @Test
    public void generateLinkWithTooltip() {
        LinkService linkService = LinkService.getInstance();
        String text = "text";
        String multilineJoined = "multiline" + "\r\n" + "Joined";
        String reportDocumentLinkResource = "reportDocumentLinkResource";
        String tooltip = "tooltip";
        String link = linkService.generateLink(text, multilineJoined, reportDocumentLinkResource, tooltip);
        assertEquals("[[reportDocumentLinkResource?search=multiline%0D%0AJoined  {tooltip} text]]", link);
    }
}
