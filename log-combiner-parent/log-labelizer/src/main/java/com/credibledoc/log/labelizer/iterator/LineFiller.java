package com.credibledoc.log.labelizer.iterator;

import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.log.buffered.LogConcatenatedInputStream;
import com.credibledoc.combiner.log.buffered.LogFileInputStream;
import com.credibledoc.combiner.log.buffered.LogInputStreamReader;
import com.credibledoc.log.labelizer.crawler.RegexService;
import com.credibledoc.log.labelizer.date.ProbabilityLabel;
import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nd4j.linalg.primitives.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Stream;

/**
 * Provides an 'infinite' source of characters for filling gaps between labeled data.
 * 
 * @author Kyrylo Semenko
 */
class LineFiller {
    private static final String WITHOUT_DATE_FOLDER = "without";
    private static final String WIKI_URL = "view-source:https://cs.wikipedia.org/wiki/Speci%C3%A1ln%C3%AD:Speci%C3%A1ln%C3%AD_str%C3" +
        "%A1nky";

    private LogBufferedReader logBufferedReader;
    
    private boolean hasNextInLogBufferedReader = true;
    
    private Stream<Character> stream = Stream.of('a');
    
    private List<String> links;
    
    private int linkIndex = 0;
    
    LineFiller(String resourceDirPath, Charset charset) {
        File dir = new File(resourceDirPath, WITHOUT_DATE_FOLDER);
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
            if (stream == null) {
                readFromNet();
            }
            Optional<Character> next = stream.findFirst();
            while (!next.isPresent()) {
                readFromNet();
                next = stream.findFirst();
            }
            char nextChar = next.get();
            if ('\r' == nextChar || '\n' == nextChar) {
                return next();
            }
            return nextChar;
        } catch (Exception e) {
            throw new LabelizerRuntimeException(e);
        }
    }

    private void readFromNet() {
        stream = Stream.empty();
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
            Document document = Jsoup.connect(links.get(linkIndex)).get();
            linkIndex++;
            appendToStream(document);
        } catch (Exception e) {
            throw new LabelizerRuntimeException(e);
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
                Stream<Character> nextStream = line.chars().mapToObj(c -> (char) c);
                stream = Stream.concat(stream, nextStream);
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
