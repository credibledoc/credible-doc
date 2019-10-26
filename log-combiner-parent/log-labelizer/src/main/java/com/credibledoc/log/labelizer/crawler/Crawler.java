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
    private static final String HTTPS_WWW_API_GITHUB_COM = "https://api.github.com";
    private static final String SEARCH_REPOS_HH_MM = HTTPS_WWW_API_GITHUB_COM + "/search/repositories?q=HH+mm+in:file+language:java?page=";
    private static final String PER_PAGE = "&per_page=100";

    /**
     * Due to terms and conditions up to 10 requests per minute. See the https://developer.github.com/v3/search page.
     */
    private static final int DELAY_BETWEEN_REQUESTS_SECONDS_6 = 60 / 10;
    private static final int MAX_THREADS_25 = 25;
    
    public static void main(String[] args) {
        PagePatternRepository pagePatternRepository = PagePatternRepository.getInstance();
        logger.info("Repository created. HashCode: {}", pagePatternRepository.hashCode());
        Crawler crawler = new Crawler();
        crawler.startJobs();
    }

    private void startJobs() {
        execute(getNextSearchPage(SEARCH_REPOS_HH_MM));
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
            // TODO Kyrylo Semenko - 
            int page = 1;
            return HTTPS_WWW_API_GITHUB_COM + page + PER_PAGE + suffix;
        } catch (Exception e) {
            PagePattern pagePattern = new PagePattern();
            pagePattern.setPageUrl(searchPageUrl);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
//            e.printStackTrace(pw);
            pagePattern.setErrorMessage("Error in the getNextSearchPage method. Message: " + e.getMessage() + ". StackTrace: " + sw.toString());
            PagePatternRepository.getInstance().save(Collections.singletonList(pagePattern));
            return null;
        }
    }
    
}
