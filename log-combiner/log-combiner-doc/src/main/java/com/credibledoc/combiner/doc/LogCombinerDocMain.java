package com.credibledoc.combiner.doc;

import com.credibledoc.substitution.core.configuration.Configuration;
import com.credibledoc.substitution.core.configuration.ConfigurationService;
import com.credibledoc.substitution.reporting.markdown.MarkdownService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * The main class for generation of documentation for the log-combiner library and tool.
 *
 * @author Kyrylo Semenko
 */
@Slf4j
public class LogCombinerDocMain {

    /**
     * The main method for generation of documentation of the credibledoc-substitution tool.
     */
    public static void main(String[] args) {
        try {
            log.info("Application '{}' launched.", LogCombinerDocMain.class.getSimpleName());
            try (AnnotationConfigApplicationContext applicationContext
                     = new AnnotationConfigApplicationContext(LogCombinerDocMain.class)) {
                applicationContext.start();
                log.info("Spring ApplicationContext created and started");
                Configuration configuration = ConfigurationService.getInstance().getConfiguration();
                configuration.setTemplatesResource("template/markdown/doc");
                MarkdownService.getInstance().generateContentFromTemplates();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.info("Application finished");
    }

}
