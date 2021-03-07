package com.credibledoc.enricher.xml;

import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.combiner.file.FileService;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

/**
 * Helps to format an unformatted xml into a pretty tree.
 * 
 * @author Kyrylo Semenko
 */
public class XmlFormatter {
    
    private XmlFormatter() {
        throw new CombinerRuntimeException("Don't instantiate this static helper, please");
    }
    
    private static final DocumentBuilderFactory documentBuilderFactory;

    private static final LSSerializer writer;

    static {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        // Setup pretty print options
        try {
            final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            writer = impl.createLSSerializer();

            writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
            writer.getDomConfig().setParameter("xml-declaration", false);

        } catch (Exception e) {
            throw new CombinerRuntimeException(e);
        }
    }

    /**
     * Format the xml, but let its header (if any) unchanged.
     * @param xml the source XML
     * @param suppressException if 'true', the source xml will be returned in case of an exception
     * @return The formatted source XML
     */
    public static String getPrettyString(String xml, boolean suppressException) {
        try {
            boolean hasHeader = xml.contains("?xml");
            String lineEnding = FileService.findLineEndingIfExists(xml);

            // Save a header unchanged
            int headerEndIndex = 0;
            if (hasHeader) {
                int headerEnd = xml.indexOf(">");
                headerEndIndex = xml.indexOf("<", headerEnd);
            }
            
            String formattedXml = format(xml, false);

            // Save a header unchanged
            String header = xml.substring(0, headerEndIndex);
            String headerAndXml = header + formattedXml;
            if (lineEnding != null) {
                return headerAndXml.replaceAll(FileService.ANY_LINE_ENDING, lineEnding);
            }
            return headerAndXml;
        } catch (Exception e) {
            if (suppressException) {
                return xml;
            }
            throw new CombinerRuntimeException(e);
        }
    }

    /**
     * Call the {@link #getPrettyString(String, boolean)} with the second parameter value 'true'.
     * @param xml see the {@link #getPrettyString(String, boolean)} method description.
     * @return See the {@link #getPrettyString(String, boolean)} method description.
     */
    public static String getPrettyString(String xml) {
        return getPrettyString(xml, true);
    }

    /**
     * Format the source XML.
     * @param xml the source
     * @param keepDeclaration should be the xml header generated?
     * @return The formatted XML
     */
    public static String format(String xml, boolean keepDeclaration) {
        try {
            final InputSource src = new InputSource(new StringReader(xml));
            final Node document = documentBuilderFactory.newDocumentBuilder().parse(src).getDocumentElement();
            writer.getDomConfig().setParameter("xml-declaration", keepDeclaration);
            return writer.writeToString(document);
        } catch (Exception e) {
            throw new CombinerRuntimeException(e);
        }
    }
}
