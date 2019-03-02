package org.credibledoc.substitution.doc.module.substitution.pom;

import com.credibledoc.substitution.content.ContentGenerator;
import com.credibledoc.substitution.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.placeholder.Placeholder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Generates the published jar name. Tries to find out the LATEST version in a Nexus repository, for example from
 * the
 * <a href="https://repo1.maven.org/maven2/com/credibledoc/plantuml-core/maven-metadata.xml">plantuml-core</a>
 * API url. If the LATEST version not found, the {@link #NOT_PUBLISHED_YET} string will be returned.
 * <p>
 * Link to the repository API is defined in the <i>url</i> {@link Placeholder#getParameters()} property.
 * <p>
 * Example
 * <pre>
 *     &&beginPlaceholder {
 *             "className": "org.credibledoc.substitution.doc.module.substitution.pom.JarNameContentGenerator",
 *             "description": "Latest plantuml-core.jar name",
 *             "parameters": {"url": "https://repo1.maven.org/maven2/com/credibledoc/plantuml-core/maven-metadata.xml"}
 *           } &&endPlaceholder
 * </pre>
 *
 * @author Kyrylo Semenko
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class JarNameContentGenerator implements ContentGenerator {

    private static final String NOT_PUBLISHED_YET = "'The artifact not published yet.'";
    private static final String TEXT_XML = "text/xml";
    private static final String ARTIFACT_ID_BEGIN_TAG = "<artifactId>";
    private static final String ARTIFACT_ID_END_TAG = "</artifactId>";
    private static final String LATEST_BEGIN_TAG = "<latest>";
    private static final String LATEST_END_TAG = "</latest>";

    @Override
    public String generate(Placeholder placeholder) {
        String url = placeholder.getParameters().get("url");
        if (url == null) {
            throw new SubstitutionRuntimeException("Placeholder parameter 'url' is required, but found 'null'.");
        }
        return loadJarName(url);
    }

    private String loadJarName(String urlParameter) {
        String result;
        try {
            URL url = new URL(urlParameter);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String contentType = httpConn.getContentType();
                if (!TEXT_XML.equals(contentType)) {
                    throw new SubstitutionRuntimeException("Expected '" + TEXT_XML + "', but found " + contentType);
                }
                InputStream inputStream = httpConn.getInputStream();
                String xmlString = convertStreamToString(inputStream);
                String artifactId = parseTag(xmlString, ARTIFACT_ID_BEGIN_TAG, ARTIFACT_ID_END_TAG);
                String latestVersion = parseTag(xmlString, LATEST_BEGIN_TAG, LATEST_END_TAG);
                result = artifactId + "-" + latestVersion + ".jar";
            } else {
                log.info("ResponseCode is " + responseCode);
                result = NOT_PUBLISHED_YET;
            }
            httpConn.disconnect();
        } catch (Exception e) {
            throw new SubstitutionRuntimeException(e);
        }
        return result;
    }

    private String parseTag(String xmlString, String beginTag, String endTag) {
        int beginIndex = xmlString.indexOf(beginTag);
        if (beginIndex == -1) {
            throw new SubstitutionRuntimeException("Cannot find " + beginTag);
        }
        int endIndex = xmlString.indexOf(endTag, beginIndex);
        if (endIndex == -1) {
            throw new SubstitutionRuntimeException("Cannot find " + endTag);
        }
        return xmlString.substring(beginIndex + beginTag.length(), endIndex);
    }

    private String convertStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
