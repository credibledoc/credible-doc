package com.credibledoc.log.labelizer.crawler;

import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;
import com.credibledoc.log.labelizer.pagepattern.PagePattern;
import com.credibledoc.log.labelizer.pagepattern.PagePatternRepository;
import com.credibledoc.log.labelizer.train.TrainDataGenerator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Helps to collect date and time patterns for a network training data.
 * 
 * @author Kyrylo Semenko
 */
public class Crawler {
    private static final Logger logger = LoggerFactory.getLogger(Crawler.class);
    private static final String HTTPS_WWW_GOOGLE_COM = "https://www.google.com";
    private static final String URL_SITE_GITHUB_COM_HH = HTTPS_WWW_GOOGLE_COM + "/search?q=site:github.com+SimpleDateFormat&client=firefox-b-d&ei=c4-sXZbhLYT4wQLEoK_gAw&start=0&sa=N&ved=0ahUKEwjW5p__pKvlAhUEfFAKHUTQCzw4ChDy0wMIfg&biw=1920&bih=1084";
    private static final int MAX_THREADS_25 = 25;
    
    public static void main(String[] args) {
        PagePatternRepository pagePatternRepository = PagePatternRepository.getInstance();
        logger.info("Repository created. HashCode: {}", pagePatternRepository.hashCode());
        Crawler crawler = new Crawler();
        crawler.startJobs();
    }

    private void startJobs() {
        execute(getNextSearchPage(URL_SITE_GITHUB_COM_HH));
    }

    private void execute(String searchPageUrl) {
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS_25);
            while (searchPageUrl != null) {
                Thread.sleep(1000 * TrainDataGenerator.randomBetween(7, 15));
                String nextSearchPageUrl = getNextSearchPage(searchPageUrl);
                createRunnable(searchPageUrl, executorService);
                searchPageUrl = nextSearchPageUrl;
            }
            executorService.shutdown();
        } catch (Exception e) {
            throw new LabelizerRuntimeException(e);
        }
    }

    private void createRunnable(String searchPageUrl, ExecutorService executorService) {
        RunnableCrawler runnableCrawler = new RunnableCrawler(searchPageUrl);
        executorService.submit(runnableCrawler);
    }

    private String getNextSearchPage(String searchPageUrl) {
        try {
            Document document = Jsoup.connect(searchPageUrl).get();
            Elements nextPage = document.select("#pnnext");
            if (nextPage == null) {
                return null;
            }
            String suffix = nextPage.attr("href");
            return HTTPS_WWW_GOOGLE_COM + suffix;
        } catch (Exception e) {
            PagePattern pagePattern = new PagePattern();
            pagePattern.setPageUrl(searchPageUrl);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pagePattern.setErrorMessage("Error in the getNextSearchPage method. Message: " + e.getMessage() + ". StackTrace: " + sw.toString());
            PagePatternRepository.getInstance().save(Collections.singletonList(pagePattern));
            return null;
        }
    }
    
}
