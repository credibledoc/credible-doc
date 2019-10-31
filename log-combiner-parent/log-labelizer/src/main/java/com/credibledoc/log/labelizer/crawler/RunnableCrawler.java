package com.credibledoc.log.labelizer.crawler;

import com.credibledoc.log.labelizer.pagepattern.PagePattern;
import com.credibledoc.log.labelizer.pagepattern.PagePatternRepository;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Runs in multi - thread environment. Searches the {@link #pagePattern} for date and time patterns.
 * 
 * @author Kyrylo Semenko
 */
public class RunnableCrawler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RunnableCrawler.class);
    
    private PagePattern pagePattern;

    RunnableCrawler(PagePattern pagePattern) {
        this.pagePattern = pagePattern;
    }
    
    @Override
    public void run() {
        analyze(pagePattern);
    }
    
    private void analyze(PagePattern pagePattern) {
        try {
            PagePatternRepository pagePatternRepository = PagePatternRepository.getInstance();
            Document searchResult = Jsoup.connect(pagePattern.getPageUrl()).get();
            String pageSource = searchResult.outerHtml();

            List<String> patterns = RegexService.parse(pageSource);
            List<String> uniquePatterns = new ArrayList<>();
            for (String pattern : patterns) {
                if (isPatternValid(pattern) && !pagePatternRepository.containsPattern(pattern) && !uniquePatterns.contains(pattern)) {
                    uniquePatterns.add(pattern);
                }
            }
            if (!uniquePatterns.isEmpty()) {
                List<PagePattern> pagePattens = new ArrayList<>();
                for (String pattern : uniquePatterns) {
                    PagePattern nextPagePattern = new PagePattern(pagePattern.getPageUrl(), pattern);
                    nextPagePattern.setVisited(true);
                    pagePattens.add(nextPagePattern);
                }
                pagePatternRepository.save(pagePattens);
            }
            
            pagePattern.setVisited(true);
            pagePatternRepository.save(Collections.singletonList(pagePattern));
        } catch (Exception e) {
            logger.error("URL: " + pagePattern.getPageUrl() + ", Message: " + e.getMessage(), e);
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
