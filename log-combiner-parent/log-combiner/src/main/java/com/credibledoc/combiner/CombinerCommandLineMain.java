package com.credibledoc.combiner;

import com.credibledoc.combiner.context.Context;
import com.credibledoc.combiner.exception.CombinerRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * This class contains a main method for launching of the {@link #LOG_COMBINER_MODULE_NAME} tool.
 */
public class CombinerCommandLineMain {
    private static final Logger logger = LoggerFactory.getLogger(CombinerCommandLineMain.class);
    static final String LOG_COMBINER_MODULE_NAME = "log-combiner-parent/log-combiner";
    private static final String LOG_COMBINER_REPOSITORY_NAME = "credible-doc";
    private static final String LINE_SEPARATOR = System.lineSeparator();

    public static void main(String[] arguments) {
        // arguments validation
        if (arguments.length < 1) {
            // print command-line options
            logger.info("Usage of the {} tool.{}" +
                "java -jar {}.jar <folderAbsolutePath> [configAbsolutePath]{}" +
                "More examples see on https://github.com/credibledoc/{}/tree/master/{}",
                LOG_COMBINER_MODULE_NAME,
                LINE_SEPARATOR,
                LOG_COMBINER_MODULE_NAME,
                LINE_SEPARATOR,
                LOG_COMBINER_REPOSITORY_NAME,
                LOG_COMBINER_MODULE_NAME);
            System.exit(0);
        }
        String folderAbsolutePath = arguments[0];
        logger.info("Source folderAbsolutePath: '{}'", folderAbsolutePath);

        File folder = new File(folderAbsolutePath);
        if (!folder.exists()) {
            throw new CombinerRuntimeException("Folder not found: '" + folder.getAbsolutePath() + "'.");
        }

        String configAbsolutePath = null;
        if (arguments.length > 1) {
            configAbsolutePath = arguments[1];
        }
        logger.info("Configuration configAbsolutePath: '{}'", configAbsolutePath);

        // Contains instances of Tactics, NodeFiles and NodeLogs
        Context context = new Context().init();
        
        CombinerService.getInstance().combine(folder, configAbsolutePath, context);
        logger.info("Application {} finished.", LOG_COMBINER_MODULE_NAME);
    }
}
