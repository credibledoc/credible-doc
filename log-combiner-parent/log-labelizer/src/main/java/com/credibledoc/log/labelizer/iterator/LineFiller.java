package com.credibledoc.log.labelizer.iterator;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.log.buffered.LogConcatenatedInputStream;
import com.credibledoc.combiner.log.buffered.LogFileInputStream;
import com.credibledoc.combiner.log.buffered.LogInputStreamReader;
import com.credibledoc.log.labelizer.crawler.RegexService;
import com.credibledoc.log.labelizer.date.ProbabilityLabel;
import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;
import com.google.common.primitives.Chars;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nd4j.linalg.primitives.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

/**
 * Provides an 'infinite' source of characters for filling gaps between labeled data.
 * 
 * @author Kyrylo Semenko
 */
@Slf4j
class LineFiller {
    private static final String WITHOUT_DATE_FOLDER = "without";
    private static final String WIKI_URL = "https://cs.wikipedia.org/wiki/Speci%C3%A1ln%C3%AD:Speci%C3%A1ln%C3%AD_str%C3%A1nky";

    private LogBufferedReader logBufferedReader;
    
    private boolean hasNextInLogBufferedReader = true;

    private List<Character> charList = new ArrayList<>();
    
    private List<String> links;
    
    private int linkIndex = 0;
    
    LineFiller(String resourceDirPath, Charset charset) {
        File dir = new File(resourceDirPath, WITHOUT_DATE_FOLDER);
        if (dir.exists()) {
            Collection<File> files = FileUtils.listFiles(dir, null, false);
            List<LogFileInputStream> inputStreams = new ArrayList<>();
            for (File file : files) {
                try {
                    inputStreams.add(new LogFileInputStream(file));
                } catch (FileNotFoundException e) {
                    throw new LabelizerRuntimeException(e);
                }
            }
            Enumeration<LogFileInputStream> enumeration = Collections.enumeration(inputStreams);
            LogConcatenatedInputStream logConcatenatedInputStream = new LogConcatenatedInputStream(enumeration);
            LogInputStreamReader logInputStreamReader
                = new LogInputStreamReader(logConcatenatedInputStream, charset);
            logBufferedReader = new LogBufferedReader(logInputStreamReader);
        } else {
            hasNextInLogBufferedReader = false;
        }
    }

    /**
     * Read files from the resourceDirPath + {@link #WITHOUT_DATE_FOLDER} obtained in the class constructor.
     * When characters from these files run out, read characters from the downloaded pages.
     * @return a single character except of line ending (\\n, \\r\\n).
     */
    private char next() {
        try {
            if (hasNextInLogBufferedReader) {
                int nextInt = logBufferedReader.read();
                if (nextInt != -1) {
                    char nextChar = (char) nextInt;
                    if ('\r' == nextChar || '\n' == nextChar) {
                        return next();
                    }
                    return nextChar;
                } else {
                    hasNextInLogBufferedReader = false;
                }
            }
            if (charList.isEmpty()) {
                readFromNet(0);
            }
            Character nextChar = charList.remove(0);
            if ('\r' == nextChar || '\n' == nextChar) {
                return next();
            }
            return nextChar;
        } catch (Exception e) {
            throw new LabelizerRuntimeException(e);
        }
    }

    private void readFromNet(int numReads) throws InterruptedException {
        try {
            if (links == null) {
                links = new ArrayList<>();
                Document document = Jsoup.connect(WIKI_URL).get();
                appendToStream(document);
                return;
            }
            if (linkIndex == links.size()) {
                linkIndex = 0;
            }
            Document document = Jsoup.connect(links.get(linkIndex++)).get();
            appendToStream(document);
        } catch (Exception e) {
            if (numReads++ > 20) {
                // start again from WIKI_URL
                links = null;
            }
            TimeUnit.SECONDS.sleep(7);
            log.error(e.getMessage(), e);
            readFromNet(numReads);
        }
    }

    private void appendToStream(Document document) {
        Elements elements = document.select("a[href]");
        for (Element element : elements) {
            links.add(element.attr("abs:href"));
        }
        String content = document.outerHtml();
        String[] lines = content.split("\n|\r\n");
        for (String line : lines) {
            Matcher matcher = RegexService.DATE_PATTERN.matcher(line);
            if (!matcher.find()) {
                charList.addAll(Chars.asList(line.toCharArray()));
            }
        }
    }

    Pair<String, String> generateFiller(int fillersLen) {
        StringBuilder filler = new StringBuilder(fillersLen);
        StringBuilder labels = new StringBuilder(fillersLen);
        for (int i = 0; i < fillersLen; i++) {
            filler.append(next());
            labels.append(ProbabilityLabel.N_WITHOUT_DATE.getCharacter());
        }
        return new Pair<>(filler.toString(), labels.toString());
    }
}
