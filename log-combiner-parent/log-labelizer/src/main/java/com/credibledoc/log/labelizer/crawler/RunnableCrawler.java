package com.credibledoc.log.labelizer.crawler;

import com.credibledoc.log.labelizer.pagepattern.PagePattern;
import com.credibledoc.log.labelizer.pagepattern.PagePatternRepository;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Runs in multi - thread environment. Searches the {@link #searchPageUrl} for date and time patterns.
 * 
 * @author Kyrylo Semenko
 */
public class RunnableCrawler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RunnableCrawler.class);
    
    private String searchPageUrl;

    RunnableCrawler(String searchPageUrl) {
        this.searchPageUrl = searchPageUrl;
    }
    
    @Override
    public void run() {
        analyze(searchPageUrl);
    }
    
    private void analyze(String searchPageUrl) {
        try {
            PagePatternRepository pagePatternRepository = PagePatternRepository.getInstance();
            Document searchResult = Jsoup.connect(searchPageUrl).get();
            Elements linksToPatternsPages = searchResult.select("#rso > div > div > div > div > div > div.r > a[href]:first-child");
            for (Element linkToPatternsPage : linksToPatternsPages) {
                saveOrSkip(pagePatternRepository, linkToPatternsPage);
            }
        } catch (Exception e) {
            logger.error("URL: " + searchPageUrl + "Message: " + e.getMessage(), e);
        }
    }

    private void saveOrSkip(PagePatternRepository pagePatternRepository, Element linkToPatternsPage) throws IOException {
        String href = linkToPatternsPage.attr("href");
        if (!pagePatternRepository.containsPage(href)) {
            String page = Jsoup.connect(href).get().outerHtml();
            List<String> patterns = RegexService.parse(page);
            List<String> uniquePatterns = new ArrayList<>();
            for (String pattern : patterns) {
                if (isPatternValid(pattern) && !pagePatternRepository.containsPattern(pattern) && !patterns.contains(pattern)) {
                    uniquePatterns.add(pattern);
                }
            }
            if (!uniquePatterns.isEmpty()) {
                List<PagePattern> pagePattens = new ArrayList<>();
                for (String pattern : uniquePatterns) {
                    pagePattens.add(new PagePattern(href, pattern));
                }
                pagePatternRepository.save(pagePattens);
            }
        }
    }

    private boolean isPatternValid(String pattern) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String dateString = simpleDateFormat.format(new Date());
            return StringUtils.isNotEmpty(dateString);
        } catch (Exception e) {
            return false;
        }
    }
}
