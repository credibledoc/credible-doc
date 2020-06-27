package com.credibledoc.plantuml.svggenerator;

import com.credibledoc.plantuml.exception.PlantumlRuntimeException;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.DiagramDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * This class provides a service for generating SVG files from PlantUML notations,
 * see <a href="https://en.wikipedia.org/wiki/Scalable_Vector_Graphics">https://en.wikipedia.org/wiki/Scalable_Vector_Graphics</a>
 * and <a href="https://en.wikipedia.org/wiki/PlantUML">https://en.wikipedia.org/wiki/PlantUML</a>
 *
 * @author Kyrylo Semenko
 */
public class SvgGeneratorService {
    
    private static final String FORBIDDEN_SEQUENCE_FOR_XML_DOCUMENT = "[#";

    private static final String ENDUML = "@enduml";

    private static final String LINE_SEPARATOR = System.lineSeparator();


    private static final String STARTUML = "@startuml";

    private static final Logger logger = LoggerFactory.getLogger(SvgGeneratorService.class);
    
    private static final String TAG_G_SVG = "</g></svg>";

    /**
     * Singleton.
     */
    private static SvgGeneratorService instance;

    private SvgGeneratorService() {
        // empty
    }

    /**
     * @return An existing {@link #instance}. If the instance is 'null',
     * instantiate new {@link SvgGeneratorService}.
     */
    public static SvgGeneratorService getInstance() {
        if (instance == null) {
            instance = new SvgGeneratorService();
        }
        return instance;
    }

    /**
     * Call the {@link #generateSvgFromPlantUml(String, boolean)}
     * method with <b>true</b> as the second argument.
     *
     * @param plantUml see the {@link #generateSvgFromPlantUml(String, boolean)}
     *                 method description
     * @return see the {@link #generateSvgFromPlantUml(String, boolean)}
     * method description
     */
    public String generateSvgFromPlantUml(String plantUml) {
        return generateSvgFromPlantUml(plantUml, true);
    }

    /**
     * <p>
     * Generate a SVG content from PlantUML notations.
     * <p>
     * For launching of the generator, the Graphviz tool should be installed,
     * see <a href="http://plantuml.com/graphviz-dot">http://plantuml.com/graphviz-dot</a>.
     * <p>
     * If the plantUml notations do not begin with <b>@startuml</b> tag,
     * attach the tag to the beginning of the plantUml notations.
     * <p>
     * If the plantUml notations do not ends by <b>@enduml</b> tag,
     * append the tag to the ent of plantUml
     * <p>
     * Append commented plantUml to the end of SVG
     *
     * @param printWarningAndPlantUml if 'true', the #plantUml content will be escaped and printed at the end of
     *                                svg as a comment for humans readability.
     * @param plantUml  source string, for example <pre>{@code Bob -> Alice : hello\nAlice -> Bob : hi}</pre>
     * @param formatSvg if 'true', a formatted SVG content will be returned
     *                  by calling the {@link #formatSvg(String)} method,
     *                  but in case when the source plantUml content has
     *                  the <b>[#</b> sequence, do NOT format the SVG content.
     * @return SVG content.
     */
    public String generateSvgFromPlantUml(boolean printWarningAndPlantUml, String plantUml, boolean formatSvg) {
        try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            if (!plantUml.trim().startsWith(STARTUML)) {
                plantUml = STARTUML + LINE_SEPARATOR + plantUml;
            }
            if (!plantUml.trim().endsWith(ENDUML)) {
                plantUml = plantUml + LINE_SEPARATOR + ENDUML;
            }

            SourceStringReader reader = new SourceStringReader(plantUml);


            FileFormatOption fileFormatOption = new FileFormatOption(FileFormat.SVG);
            DiagramDescription diagramDescription = reader.outputImage(os, fileFormatOption);
            logger.info("DiagramDescription: {}", diagramDescription.getDescription());

            // The XML is stored into svg
            String svg = new String(os.toByteArray(), StandardCharsets.UTF_8);

            if (printWarningAndPlantUml) {
                final String warningMessage = "!WARNING! Original strings (double dash) has been replaced" +
                    " by '- -' (dash+space+dash) in this comment" +
                    ", because the string (double dash) is not permitted within comments." +
                    " And link parameters, for example ?search=... have also been REMOVED from the comment," +
                    " because they are not readable for humans.";

                svg = svg.replace(TAG_G_SVG,
                    LINE_SEPARATOR
                        + "<!--"
                        + LINE_SEPARATOR
                        + warningMessage
                        + LINE_SEPARATOR
                        + "<img uml=\""
                        + LINE_SEPARATOR
                        + escape(plantUml)
                        + LINE_SEPARATOR
                        + "\"/>"
                        + LINE_SEPARATOR
                        + "-->"
                        + TAG_G_SVG);
            }

            if (plantUml.contains(FORBIDDEN_SEQUENCE_FOR_XML_DOCUMENT)) {
                formatSvg = false;
            }
            if (formatSvg) {
                return formatSvg(svg);
            } else {
                return svg;
            }
        } catch (Exception e) {
            throw new PlantumlRuntimeException("PlantUML: " + plantUml, e);
        }
    }

    /**
     * <p>
     * Call the {@link #generateSvgFromPlantUml(boolean, String, boolean)} with first parameter 'false'.
     * <p>
     * For launching of the generator, the Graphviz tool should be installed,
     * see <a href="http://plantuml.com/graphviz-dot">http://plantuml.com/graphviz-dot</a>.
     * <p>
     * If the plantUml notations do not begin with <b>@startuml</b> tag,
     * attach the tag to the beginning of the plantUml notations.
     * <p>
     * If the plantUml notations do not ends by <b>@enduml</b> tag,
     * append the tag to the ent of plantUml
     * <p>
     * Append commented plantUml to the end of SVG
     *
     * @param plantUml  source string, for example <pre>{@code Bob -> Alice : hello\nAlice -> Bob : hi}</pre>
     * @param formatSvg if 'true', a formatted SVG content will be returned
     *                  by calling the {@link #formatSvg(String)} method,
     *                  but in case when the source plantUml content has
     *                  the <b>[#</b> sequence, do NOT format the SVG content.
     * @return SVG content.
     */
    public String generateSvgFromPlantUml(String plantUml, boolean formatSvg) {
        return generateSvgFromPlantUml(false, plantUml, formatSvg);
    }

    /**
     * Create a formatted svg content from a source
     * @param svg the source
     * @return the formatted content
     */
    private String formatSvg(String svg) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            StreamResult result = new StreamResult(new StringWriter());

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(svg));
            Document document = db.parse(inputSource);

            DOMSource source = new DOMSource(document);
            transformer.transform(source, result);
            String formattedSvg = result.getWriter().toString();
            if (logger.isTraceEnabled()) {
                logger.trace(formattedSvg);
            }
            return formattedSvg;
        } catch (Exception e) {
            throw new PlantumlRuntimeException("Cannot format the svg source. Source svg: " + svg, e);
        }
    }

    /**
     * Replace -- to - -, because <i>SAXParseException: The string "--" is not permitted within comments</i>.
     * <p>
     * Remove link parameters <i>?search=...</i> because they are not readable for humans.
     *
     * @param plantUml PlantUML notations
     * @return PlantUML notations prepared for printing as a HTML comment.
     */
    private String escape(String plantUml) {
        // PlantUML do the same when attaching its source to SVG xml as comment
        return plantUml
                .replaceAll("--", "- -")
                .replaceAll("--", "- -")
                .replaceAll("\\?search=.*\\s", " ");
    }

}
