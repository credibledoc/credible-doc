package com.credibledoc.plantuml.link;

import com.credibledoc.plantuml.exception.PlantumlRuntimeException;
import com.credibledoc.plantuml.tooltip.TooltipService;
import org.apache.commons.codec.net.URLCodec;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides a service for creation of html links and parameters.
 *
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
    private static final LinkService instance = new LinkService();

    /**
     * Create a new instance of this class.
     */
    public LinkService() {
        // empty
    }

    /**
     * @return A singleton {@link #instance} of this class.
     */
    public static LinkService getInstance() {
        return instance;
    }

    /**
     * Create a HTML parameter from the method argument.
     *
     * @param line for example {@code 29.09.2018 22:53:42.494|https-jsse-nio-15443-exec-1|DEBUG..}
     * @return For example {@code ?search=29.09.2018 22:53:42.494|https-jsse-nio-15443-exec-1}
     */
    private String generateSearchParam(String line) {
        try {
            int len = Math.min(line.length(), 30);

            String truncated = line.substring(0, len);

            Matcher matcher = PATTERN.matcher(truncated);
            String encodedWithPluses;
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
