package com.apache.credibledoc.plantuml.link;

import com.apache.credibledoc.plantuml.exception.PlantumlRuntimeException;
import com.apache.credibledoc.plantuml.tooltip.TooltipService;
import org.apache.commons.codec.net.URLCodec;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides html links and parameters
 * @author Kyrylo Semenko
 */
public class LinkService {

    private static final String SEARCH_PARAMETER = "?search=";

    private static final String LAST_DIGIT = "(.*\\d+)\\D*$";
    private static final Pattern PATTERN = Pattern.compile(LAST_DIGIT);
    private static final String SPACE_URL_REPLACEMENT = "%20";
    private static final String PLUS = "+";

    /**
     * Singleton.
     */
    private static LinkService instance;

    private LinkService() {
        // empty
    }

    /**
     * @return the {@link LinkService} singleton.
     */
    public static LinkService getInstance() {
        if (instance == null) {
            instance = new LinkService();
        }
        return instance;
    }

    /**
     * Return for example "?search=29.09.2018 22:53:42.494|https-jsse-nio-15443-exec-1"
     * from "29.09.2018 22:53:42.494|https-jsse-nio-15443-exec-1|DEBUG.."
     */
    private String generateSearchParam(String line) {
        try {
            int len = line.length() > 30 ? 30 : line.length();

            String truncated = line.substring(0, len);

            Matcher matcher = PATTERN.matcher(truncated);
            String encodedWithPluses = null;
            if (matcher.find()) {
                String dateTimeString = matcher.group(1);
                encodedWithPluses = new URLCodec().encode(dateTimeString, StandardCharsets.UTF_8.name());
            } else {
                encodedWithPluses = new URLCodec().encode(truncated, StandardCharsets.UTF_8.name());
            }

            String encoded = encodedWithPluses.replace(PLUS, SPACE_URL_REPLACEMENT);
            return SEARCH_PARAMETER + encoded;

        } catch (UnsupportedEncodingException e) {
            throw new PlantumlRuntimeException(e);
        }
    }

    /**
     * Generate a link for example
     * <pre>
     * [[public/application.log.001.expanded.html?search=searchParam  {tooltip ...} ]]
     * </pre>
     *
     * @param text                       text in the link
     * @param multilineJoined            lines from log file
     * @param reportDocumentLinkResource a relative path to the linked resource
     * @return for example <pre>"[[reportDocumentLinkResource?search=multiline%0D%0AJoined  {multiline\\\\nJoined} text]]"</pre>
     */
    public String generateLink(String text, String multilineJoined, String reportDocumentLinkResource) {
        return "[["
                + reportDocumentLinkResource + generateSearchParam(multilineJoined)
                + " " + TooltipService.getInstance().generateTooltip(multilineJoined) + " " + text
                + "]]";
    }

    /**
     * Generate a link for example
     * <pre>
     * [[public/application.log.001.expanded.html?search=searchParam  {tooltip ...} ]]
     * </pre>
     * @param text text in the link
     * @param searchParam the first line from a log file for search parameter generation
     * @param reportDocumentLinkResource a relative path to the linked resource
     * @param tooltip the link's tooltip
     * @return for example <pre>"[[reportDocumentLinkResource?search=multiline%0D%0AJoined  {tooltip} text]]"</pre>
     */
    public String generateLink(String text, String searchParam, String reportDocumentLinkResource, String tooltip) {
        return "[["
                + reportDocumentLinkResource + generateSearchParam(searchParam)
                + " " + TooltipService.getInstance().generateTooltip(tooltip) + " " + text
                + "]]";
    }
    
}
