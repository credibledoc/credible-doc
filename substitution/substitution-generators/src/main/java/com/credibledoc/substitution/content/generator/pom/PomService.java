package com.credibledoc.substitution.content.generator.pom;

import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This stateless singleton contains common business logic used in this package.
 *
 * @author Kyrylo Semenko
 */
public class PomService {
    private static final String TEXT_XML = "text/xml";

    /**
     * Singleton.
     */
    private static PomService instance;

    /**
     * @return The {@link PomService} singleton.
     */
    public static PomService getInstance() {
        if (instance == null) {
            instance = new PomService();
        }
        return instance;
    }

    String loadXmlString(String urlParameter) {
        String xmlString;
        try {
            URL url = new URL(urlParameter);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new SubstitutionRuntimeException("Response code is not " + HttpURLConnection.HTTP_OK +
                    ". Response code: " + responseCode);
            }
            String contentType = httpConn.getContentType();
            if (!TEXT_XML.equals(contentType)) {
                throw new SubstitutionRuntimeException("Expected '" + TEXT_XML + "', but found " + contentType);
            }
            InputStream inputStream = httpConn.getInputStream();
            xmlString = convertStreamToString(inputStream);
            httpConn.disconnect();
        } catch (Exception e) {
            throw new SubstitutionRuntimeException(e);
        }
        return xmlString;
    }

    /**
     * Parse text from XML
     * @param xmlString source
     * @param beginTag begin pattern
     * @param endTag end pattern
     * @return parsed text
     */
    String parseTag(String xmlString, String beginTag, String endTag) {
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
