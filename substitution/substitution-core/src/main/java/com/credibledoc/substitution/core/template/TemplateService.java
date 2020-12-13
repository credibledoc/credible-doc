package com.credibledoc.substitution.core.template;

import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.core.resource.ResourceType;
import com.credibledoc.substitution.core.resource.TemplateResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Provides templates of documents.
 *
 * @author Kyrylo Semenko
 */
public class TemplateService {

    private static final Logger logger = LoggerFactory.getLogger(TemplateService.class);

    private static final String BEGINNING_OF_THE_INPUT_BOUNDARY = "\\A";

    /**
     * Singleton.
     */
    private static final TemplateService instance = new TemplateService();

    private TemplateService() {
        // empty
    }

    /**
     * @return The {@link TemplateService} singleton.
     */
    public static TemplateService getInstance() {
        return instance;
    }

    /**
     * Return a template as a String
     *
     * @param templateResource the template source
     * @param charsetName      the character set for reading from the source stream.
     * @return the template content in the {@link StandardCharsets#UTF_8} charset.
     */
    public String getTemplateContent(TemplateResource templateResource, String charsetName) {
        if (templateResource.getType() == ResourceType.CLASSPATH) {
            try (InputStream inputStream = getClass().getResourceAsStream(templateResource.getPath());
                 Scanner scanner = new Scanner(inputStream, charsetName)) {

                scanner.useDelimiter(BEGINNING_OF_THE_INPUT_BOUNDARY);
                return scanner.hasNext() ? scanner.next() : "";
            } catch (Exception e) {
                throw new SubstitutionRuntimeException(
                    "Cannot read from the resource '" +
                        templateResource + "'", e);
            }
        }
        if (templateResource.getType() == ResourceType.FILE) {
            try (InputStream inputStream = new FileInputStream(templateResource.getFile());
                 Scanner scanner = new Scanner(inputStream, charsetName)) {

                scanner.useDelimiter(BEGINNING_OF_THE_INPUT_BOUNDARY);
                return scanner.hasNext() ? scanner.next() : "";
            } catch (Exception e) {
                throw new SubstitutionRuntimeException(
                    "Cannot read from the resource '" +
                        templateResource + "'", e);
            }
        }
        throw new SubstitutionRuntimeException("Unknown ResourceType " + templateResource.getType());
    }

    /**
     * Export the resource embedded into a Jar file to the local file path.
    *
    * @param resourceName, for example "/template/css/css.css"
    * @param targetFilePath where to copy, for example "c:\template\css.css"
    * @return The file of the exported resource
    */
   public File exportResource(String resourceName, String targetFilePath) {
       File targetFile = new File(targetFilePath);
       File parenDirectory = targetFile.getParentFile();
       boolean created = parenDirectory.mkdirs();
       if (created) {
           logger.info("Directory created: {}", parenDirectory.getAbsolutePath());
       }
       try (InputStream stream = TemplateService.class.getResourceAsStream(resourceName);
            OutputStream resStreamOut = new FileOutputStream(targetFile)) {
           
           if (stream == null) {
               throw new SubstitutionRuntimeException("Cannot get resource \"" +
                       resourceName + "\" from Jar file.");
           }

           int readBytes;
           byte[] buffer = new byte[4096];
           while ((readBytes = stream.read(buffer)) > 0) {
               resStreamOut.write(buffer, 0, readBytes);
           }
           return targetFile;
       } catch (Exception e) {
           throw new SubstitutionRuntimeException(e);
       }
   }

}
