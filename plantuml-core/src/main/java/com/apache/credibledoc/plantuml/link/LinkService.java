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
    
    public static final String PIPE = "|";

    public static final String DOT = ".";
    
    private static final String SEARCH_PARAMETER = "?search=";

    private static final String LAST_DIGIT = "(.*\\d+)\\D*$";
    private static final Pattern PATTERN = Pattern.compile(LAST_DIGIT);

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
            if (matcher.find()) {
                String dateTimeString = matcher.group(1);
                String encoded = new URLCodec().encode(dateTimeString, StandardCharsets.UTF_8.name()).replace("+", "%20");
                return SEARCH_PARAMETER + encoded;
            }

            String encoded = new URLCodec().encode(truncated, StandardCharsets.UTF_8.name()).replace("+", "%20");
            return SEARCH_PARAMETER + encoded;

        } catch (UnsupportedEncodingException e) {
            throw new PlantumlRuntimeException(e);
        }
    }

    /**
     * Generate link for example
     * <pre>
     * [[public/wallet-2018-09-29.0.log.001.expanded.html?search=searchParam  {tooltip ...} ]]
     * </pre>
     * @param text text in the link
     * @param multilineJoined lines from log file
     * @param reportDocumentLinkResource a relative path to the linked resource
     */
    public String generateLink(String text, String multilineJoined, String reportDocumentLinkResource) {
        return "[["
                + reportDocumentLinkResource + generateSearchParam(multilineJoined)
                + " " + TooltipService.getInstance().generateTooltip(multilineJoined) + " " + text
                + "]]";
    }

    /**
     * Generate link for example
     * <pre>
     * [[public/wallet-2018-09-29.0.log.001.expanded.html?search=searchParam  {tooltip ...} ]]
     * </pre>
     * @param text text in the link
     * @param searchParam the first line from a log file for search parameter generation
     * @param reportDocumentLinkResource a relative path to the linked resource
     * @param tooltip the link's tooltip
     */
    public String generateLink(String text, String searchParam, String reportDocumentLinkResource, String tooltip) {
        return "[["
                + reportDocumentLinkResource + generateSearchParam(searchParam)
                + " " + TooltipService.getInstance().generateTooltip(tooltip) + " " + text
                + "]]";
    }
    
}
