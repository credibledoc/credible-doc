package com.credibledoc.log.labelizer.crawler;

import com.credibledoc.log.labelizer.config.Config;
import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;
import com.credibledoc.log.labelizer.github.GithubRepo;
import com.credibledoc.log.labelizer.github.GithubRepoRepository;
import com.credibledoc.log.labelizer.github.VisitedUrl;
import com.credibledoc.log.labelizer.github.VisitedUrlRepository;
import com.credibledoc.log.labelizer.pagepattern.PagePattern;
import com.credibledoc.log.labelizer.pagepattern.PagePatternRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.morphia.query.internal.MorphiaCursor;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Helps to collect date and time patterns for a network training data.
 * 
 * @author Kyrylo Semenko
 */
public class Crawler {
    private static final Logger logger = LoggerFactory.getLogger(Crawler.class);
    private static final String HTTPS_API_GITHUB_COM = "https://api.github.com";
    private static final String SEARCH_REPOSITORIES_Q = "/search/repositories?q=";
    private static final String LANGUAGE_JAVA = "+language%3Ajava?";
    private static final String PER_PAGE = "&per_page=";
    private static final int NUM_ITEMS_PER_PAGE_100 = 100;

    /**
     * Due to terms and conditions up to 10 requests per minute. See the https://developer.github.com/v3/search page.
     */
    private static final int DELAY_BETWEEN_REQUESTS_SECONDS_2 = 2;
    private static final int MAX_THREADS_25 = 25;
    private static final String FIELD_FULL_NAME = "full_name";
    private static final String FIELD_LANGUAGE = "language";
    private static final String JAVA = "Java";
    private static final int MAX_QUERY_LENGTH_256 = 256;
    private static final String PLACEHOLDER = "##placeholder##";
    private static final int DIGITS_IN_PAGE_NUM = 2;

    public static void main(String[] args) {
        PagePatternRepository pagePatternRepository = PagePatternRepository.getInstance();
        logger.info("Context path: '{}'", new File("").getAbsolutePath());
        logger.info("Repository created. HashCode: {}", pagePatternRepository.hashCode());
        Crawler crawler = new Crawler();
        crawler.startJobs();
    }

    private void startJobs() {
        try {
            findRepositories();
            GithubRepoRepository githubRepoRepository = GithubRepoRepository.getInstance();
            List<GithubRepo> githubRepos = githubRepoRepository.selectNotVisited();
            StringBuilder stringBuilder = new StringBuilder(MAX_QUERY_LENGTH_256);
            String mmInFile = "https://api.github.com/search/code?q=mm+in%3Afile";
            String searchString = mmInFile +
                PLACEHOLDER +
                PER_PAGE +
                NUM_ITEMS_PER_PAGE_100 +
                "&page=";
            List<GithubRepo> visitedRepos = new ArrayList<>();
            for (GithubRepo githubRepo : githubRepos) {
                searchLinksInRepository(stringBuilder, searchString, visitedRepos, githubRepo);
            }
            execute();
        } catch (Exception e) {
            throw new LabelizerRuntimeException(e);
        }
    }

    private void searchLinksInRepository(StringBuilder stringBuilder, String searchString, List<GithubRepo> visitedRepos, GithubRepo githubRepo) {
        VisitedUrlRepository visitedUrlRepository = VisitedUrlRepository.getInstance();
        GithubRepoRepository githubRepoRepository = GithubRepoRepository.getInstance();
        String repoName = "+repo:" + githubRepo.getFullName();
        if (searchString.length() - PLACEHOLDER.length() + stringBuilder.length() + repoName.length() +
                    DIGITS_IN_PAGE_NUM <= MAX_QUERY_LENGTH_256) {
            stringBuilder.append(repoName);
            githubRepo.setVisited(true);
            visitedRepos.add(githubRepo);
        } else {
            int page = 1;
            String longSearchString = searchString.replace(PLACEHOLDER, stringBuilder);
            stringBuilder.setLength(0);

            JsonObject searchResults = savePages(page, longSearchString);

            int numResults = searchResults.getAsJsonPrimitive("total_count").getAsInt();
            int numPages = numResults / NUM_ITEMS_PER_PAGE_100;
            for (int nextPage = 2; nextPage <= numPages && nextPage < 11; nextPage++) {
                if (!visitedUrlRepository.contains(longSearchString + nextPage)) {
                    savePages(nextPage, longSearchString);
                }
            }
            githubRepoRepository.save(visitedRepos);
            visitedRepos.clear();
            stringBuilder.append(repoName);
            githubRepo.setVisited(true);
            visitedRepos.add(githubRepo);
        }
    }

    private void findRepositories() throws IOException {
        VisitedUrlRepository visitedUrlRepository = VisitedUrlRepository.getInstance();
        for (String keyword : Config.getGithubSearchKeywords()) {
            // Github provides max 1000 results
            for (int page = 1; page <= 10; page++) {
                String nextPageUrl = HTTPS_API_GITHUB_COM + SEARCH_REPOSITORIES_Q + keyword + LANGUAGE_JAVA +
                    PER_PAGE + NUM_ITEMS_PER_PAGE_100 + "&page=" + page;
                if (!visitedUrlRepository.contains(nextPageUrl)) {
                    JsonObject repoList = getNextRepoList(nextPageUrl);
                    JsonArray items = repoList.getAsJsonArray("items");
                    for (JsonElement nextElement : items) {
                        getAndSaveJavaRepos(nextElement);
                    }
                    VisitedUrl visitedUrl = new VisitedUrl(nextPageUrl); 
                    visitedUrlRepository.save(Collections.singletonList(visitedUrl));
                }
            }
        }
    }

    @NotNull
    private JsonObject savePages(int page, String longSearchString) {
        JsonObject searchResults = getNextSearchResult(longSearchString + page);
        JsonArray items = searchResults.getAsJsonArray("items");
        List<PagePattern> pagePatterns = new ArrayList<>();
        for (JsonElement element : items) {
            String pageUrl = ((JsonObject)element).get("html_url").getAsString();
            if (!PagePatternRepository.getInstance().containsPage(pageUrl)) {
                PagePattern pagePattern = new PagePattern();
                pagePattern.setPageUrl(pageUrl);
                pagePatterns.add(pagePattern);
            }
        }
        PagePatternRepository.getInstance().save(pagePatterns);
        return searchResults;
    }

    private JsonObject getNextSearchResult(String query) {
        try {
            String json = createConnection(query)
                .execute()
                .body();
            return new JsonParser().parse(json).getAsJsonObject();
        } catch (Exception e) {
            throw new LabelizerRuntimeException(e);
        }
    }

    private void getAndSaveJavaRepos(JsonElement nextElement) throws IOException {
        JsonObject nextJsonObject = nextElement.getAsJsonObject();
        JsonElement jsonElementLanguage = nextJsonObject.get(FIELD_LANGUAGE);
        if (!jsonElementLanguage.isJsonNull()) {
            String language = jsonElementLanguage.getAsString();
            if (JAVA.equals(language)) {
                JsonObject owner = nextJsonObject.get("owner").getAsJsonObject();
                String repoFullName = nextJsonObject.getAsJsonPrimitive(FIELD_FULL_NAME).getAsString();
                String reposUrl = owner.get("repos_url").getAsString();
                if (!GithubRepoRepository.getInstance().contains(repoFullName)) {
                    getAndSaveRepos(reposUrl);
                }
            }
        }
    }

    private void getAndSaveRepos(String reposUrl) throws IOException {
        String json = createConnection(reposUrl)
            .execute()
            .body();
        JsonArray repos = new JsonParser().parse(json).getAsJsonArray();
        List<GithubRepo> githubRepos = new ArrayList<>();
        for (JsonElement repo : repos) {
            JsonObject repoJsonObject = repo.getAsJsonObject();
            JsonElement langJsonElement = repoJsonObject.get(FIELD_LANGUAGE);
            if (!langJsonElement.isJsonNull()) {
                String lang = langJsonElement.getAsString();
                if (JAVA.equals(lang)) {
                    String fullName = repoJsonObject.get(FIELD_FULL_NAME).getAsString();
                    GithubRepo githubRepo = new GithubRepo(fullName);
                    githubRepos.add(githubRepo);
                }
            }
        }
        GithubRepoRepository.getInstance().save(githubRepos);
    }

    private void execute() {
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS_25);
            MorphiaCursor<PagePattern> cursor = PagePatternRepository.getInstance().getCursorOfEmptyPatterns();
            while (cursor.hasNext()) {
                PagePattern pagePattern = cursor.next();
                createRunnable(pagePattern, executorService);
            }
            executorService.shutdown();
        } catch (Exception e) {
            throw new LabelizerRuntimeException(e);
        }
    }

    private void createRunnable(PagePattern pagePattern, ExecutorService executorService) {
        RunnableCrawler runnableCrawler = new RunnableCrawler(pagePattern);
        executorService.submit(runnableCrawler);
    }

    private JsonObject getNextRepoList(String searchPageUrl) {
        try {
            Connection.Response response = createConnection(searchPageUrl).execute();
            String json = response.body();
            return new JsonParser().parse(json).getAsJsonObject();
        } catch (Exception e) {
            PagePattern pagePattern = new PagePattern();
            pagePattern.setPageUrl(searchPageUrl);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pagePattern.setErrorMessage("Error in the getNextSearchPage method. Message: " + e.getMessage() + ". StackTrace: " + sw.toString());
            PagePatternRepository.getInstance().save(Collections.singletonList(pagePattern));
            throw new LabelizerRuntimeException(e);
        }
    }

    private Connection createConnection(String searchPageUrl) {
        try {
            Thread.sleep(DELAY_BETWEEN_REQUESTS_SECONDS_2 * 1000L + 50);
            String token = "token " + Config.getGithubOauthToken();
            return Jsoup.connect(searchPageUrl)
                .ignoreContentType(true)
                .header("Accept", "application/vnd.github.v3+json")
                .header("Authorization", token);
        } catch (Exception e) {
            throw new LabelizerRuntimeException(e);
        }
    }

}
