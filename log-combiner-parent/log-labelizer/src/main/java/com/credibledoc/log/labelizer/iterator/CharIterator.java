package com.credibledoc.log.labelizer.iterator;

import com.credibledoc.log.labelizer.classifier.LinesWithDateClassification;
import com.credibledoc.log.labelizer.date.DateExample;
import com.credibledoc.log.labelizer.date.ProbabilityLabel;
import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;
import com.credibledoc.log.labelizer.hint.Hint;
import com.credibledoc.log.labelizer.hint.IpGenerator;
import com.credibledoc.log.labelizer.hint.SimilarityHint;
import com.credibledoc.log.labelizer.pagepattern.PagePattern;
import com.credibledoc.log.labelizer.pagepattern.PagePatternRepository;
import com.credibledoc.log.labelizer.training.TrainingDataGenerator;
import com.google.common.primitives.Chars;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.MultiDataSet;
import org.nd4j.linalg.dataset.api.MultiDataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.io.ClassPathResource;
import org.nd4j.linalg.primitives.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * Provides data for training and testing.
 * 
 * @author Kyrylo Semenko
 */
public class CharIterator implements MultiDataSetIterator {
    public static final String RESOURCES_DIR = "vectors";

    private static final Logger logger = LoggerFactory.getLogger(CharIterator.class);
    private static final String NOT_IMPLEMENTED = "Not implemented";
    public static final String NATIONAL_CHARS_TXT = "chars/nationalChars.txt";
    private static final char ORDERING_FORTRAN = 'f';
    private static final String MINI_BATCH = "MiniBatch: {}: {}";
    private static final int NUM_EXAMPLES_OF_DATE_PATTERN_100 = 100;


    private static final List<Character> PUNCTUATIONS = new ArrayList<>(Arrays.asList('!', '&', '(', ')', '?', '-',
        '\\', ',', '.', '\"', ':', ';', ' '));

    private static final List<Character> SMALL_LETTERS = listChars('a', 'z');
    private static final List<Character> LARGE_LETTERS = listChars('A', 'Z');
    private static final List<Character> DIGITS = listChars('0', '9');
    private static final List<Character> DIGITS_AND_LETTERS = new ArrayList<>();
    private static final List<Character> DIGITS_AND_LETTERS_AND_PUNCTUATIONS = new ArrayList<>();
    private static final List<String> SEPARATORS = new ArrayList<>(Arrays.asList("|", " ", "/", ",", "'", "-", "_"));

    private static final List<String> BOUNDARIES = new ArrayList<>(Arrays.asList(
        // repeated for more probability
        "  ", "  ", "  ", "  ", "  ",
        "{}", "()", "()", "()",
        "//", ",,", "\"\"", "''", "--", "__", "||",
        "[]", "[]", "[]", "[]", "[]"));

    private static final List<String> THREAD_COMMON_NAMES = new ArrayList<>(Arrays.asList("thread", "main", "worker", "job", "pool",
        "local", "exec", "Main"));

    private static final List<String> LOG_LEVELS = new ArrayList<>(Arrays.asList("ALL", "TRACE", "DEBUG", "INFO", "WARN", "WARNING",
        "ERROR", "SEVERE", "FATAL", "CONFIG", "FINE", "FINER", "FINEST", "CRITICAL", "VERBOSE", "Trace", "Debug", "Info", "Warn", "Error"));
    private static final int KEY_FOR_MISSED_CHARS = 0;
    public static final String NEW_CHARS_TXT = "chars/newChars.txt";


    /**
     * Maps each character to an index in the input/output. These characters is used in train and test data.
     * <p>
     * Key is an order number and value is a character.
     */
    private Map<Integer, Character> intToCharMap;

    /**
     * The examples of a date string where individual parts are marked with labels.
     * This list contains max {@link #NUM_EXAMPLES_OF_DATE_PATTERN_100} elements and the new elements where appends
     * continuously until some patterns exists in the database.
     */
    private transient List<DateExample> dateExamples = new ArrayList<>();

    /**
     * Provides characters without labels for filling out of gaps between the labeled data.
     */
    private transient LineFiller lineFiller;

    /**
     * Length of each example/minibatch (number of characters)
     */
    private int exampleLength;

    /**
     * Size of each minibatch (number of examples)
     */
    private int miniBatchSize;

    /**
     * The current trained {@link PagePattern}.
     */
    private transient PagePattern lastPagePattern;

    /**
     * How many {@link PagePattern}s for a training the database contains before start of the training.
     */
    private long patternsCount;

    /**
     * How many {@link PagePattern}s has been trained.
     */
    private long patternsPassed = 0;

    /**
     * Resource files directory.
     */
    private final String resourcesDirPath;

    static {
        DIGITS_AND_LETTERS.addAll(LARGE_LETTERS);
        DIGITS_AND_LETTERS.addAll(SMALL_LETTERS);
        DIGITS_AND_LETTERS.addAll(DIGITS);
        DIGITS_AND_LETTERS_AND_PUNCTUATIONS.addAll(DIGITS_AND_LETTERS);
        DIGITS_AND_LETTERS_AND_PUNCTUATIONS.addAll(PUNCTUATIONS);
        DIGITS_AND_LETTERS_AND_PUNCTUATIONS.addAll(readNationalChars());
    }

    private static Collection<? extends Character> readNationalChars() {
        try {
            String chars = getNationalCharsFromFile();
            return new ArrayList<>(Chars.asList(chars.toCharArray()));
        } catch (Exception e) {
            throw new LabelizerRuntimeException(e);
        }
    }

    @NotNull
    private static String getNationalCharsFromFile() throws IOException {
        ClassPathResource resource = new ClassPathResource(CharIterator.RESOURCES_DIR + "/" + NATIONAL_CHARS_TXT);
        File file = resource.getFile();
        if (!file.exists()) {
            throw new LabelizerRuntimeException("File not found: '" + file.getAbsolutePath() + "'");
        }
        return new String(Files.readAllBytes(file.toPath()));
    }

    /**
     * @param resourcesDirPath     Path to text file to use for generating samples
     * @param charset Encoding of the text file(s). Can try Charset.defaultCharset()
     * @param miniBatchSize    Number of examples per mini-batch
     * @param exampleLength    Number of characters in each input/output vector
     * @throws IOException If text file cannot be loaded
     */
    public CharIterator(String resourcesDirPath, Charset charset, int miniBatchSize,
                        int exampleLength) throws IOException {
        if (!new File(resourcesDirPath).exists()) {
            throw new IOException("Could not access file (does not exist): " + resourcesDirPath);
        }
        if (miniBatchSize <= 0) {
            throw new IllegalArgumentException("Invalid miniBatchSize (must be > 0)");
        }
        this.resourcesDirPath = resourcesDirPath;
        this.exampleLength = exampleLength;
        this.miniBatchSize = miniBatchSize;

        //Store valid characters is a map for later use in vectorization
        initCharToIdxMap();
        
        lineFiller = new LineFiller(resourcesDirPath, charset);
        patternsCount = PagePatternRepository.getInstance().countNotTrainedPatterns();
    }

    public List<String> readLinesFromFolder(String resourcesDirPath, Charset textFileEncoding, String folderName) throws IOException {
        File dateDir = new File(resourcesDirPath, folderName);
        Collection<File> dateFiles = FileUtils.listFiles(dateDir, null, false);
        List<String> exampleLines = new ArrayList<>();
        for (File file : dateFiles) {
            exampleLines.addAll(Files.readAllLines(file.toPath(), textFileEncoding));
        }
        checkMissingChars(resourcesDirPath, exampleLines);
        return exampleLines;
    }

    private void checkMissingChars(String resourcesDirPath, List<String> exampleLines) throws IOException {
        List<Character> missingChars = new ArrayList<>();
        for (String line : exampleLines) {
            char[] thisLine = line.toCharArray();
            for (char nextChar : thisLine) {
                if (!intToCharMap.containsValue(nextChar) && !missingChars.contains(nextChar)) {
                    missingChars.add(nextChar);
                }
            }
        }
        if (!missingChars.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Character next : missingChars) {
                stringBuilder.append(next);
            }
            File charsFile = new File(resourcesDirPath, NATIONAL_CHARS_TXT);
            logger.info("File will be created: '{}'", charsFile.getAbsolutePath());
            if (!charsFile.getParentFile().mkdirs()) {
                logger.info("Not created '{}'", charsFile.getParentFile().getAbsolutePath());
            }
            String existingChars = getNationalCharsFromFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(charsFile))) {
                writer.write(existingChars);
                writer.write(stringBuilder.toString());
            }
            throw new LabelizerRuntimeException("missingChars:" + stringBuilder.toString());
        }
    }

    private void initCharToIdxMap() {
        intToCharMap = new HashMap<>();
        char[] chars = getCharacters();
        for (int i = 0; i < chars.length; i++) {
            intToCharMap.put(i, chars[i]);
        }
        String escaped = intToCharMap.toString()
            .replaceAll("(\\t)+", "\\\\t")
            .replaceAll("(\\r)+", "\\\\r")
            .replaceAll("(\\n)+", "\\\\n");
        logger.info("All used characters in the charToIdxMap: {}", escaped);
    }

    /**
     * A minimal character set, with a-z, A-Z, 0-9 and common punctuation etc.
     * As per getMinimalCharacterSet(), but with a few extra characters.
     */
    private static char[] getCharacters() {
        List<Character> validChars = new ArrayList<>(DIGITS_AND_LETTERS_AND_PUNCTUATIONS);
        
        Character[] temp = {'\n', '\t', '\r'};
        validChars.addAll(Arrays.asList(temp));

        Character[] additionalChars = {'@', '#', '$', '%', '^', '*', '{', '}', '[', ']', '/', '+', '_',
            '\\', '|', '<', '>', '='};
        validChars.addAll(Arrays.asList(additionalChars));

        char[] out = new char[validChars.size()];
        int i = 0;
        for (Character c : validChars) out[i++] = c;
        return out;
    }

    private static List<Character> listChars(char from, char to) {
        List<Character> result = new ArrayList<>();
        for (char c = from; c <= to; c++) {
            result.add(c);
        }
        return result;
    }

    public int convertCharacterToIndex(char character) {
        for (Map.Entry<Integer, Character> entry : intToCharMap.entrySet()) {
            if (character == entry.getValue()) {
                return entry.getKey();
            }
        }
        File charsFile = new File(resourcesDirPath + "/", NEW_CHARS_TXT);
        if (!charsFile.exists()) {
            try {
                Files.createFile(charsFile.toPath());
            } catch (IOException e) {
                throw new LabelizerRuntimeException(e);
            }
        }
        String chars;
        try {
            chars = new String(Files.readAllBytes(charsFile.toPath()));
        } catch (Exception e) {
            throw new LabelizerRuntimeException(e);
        }
        if (!chars.contains(Character.toString(character))) {
            try (FileWriter fileWriter = new FileWriter(charsFile, true)) {
                String message = "Cannot find index for character: '" + character + "'. It will be write to the file '" +
                    charsFile.getAbsolutePath() + "'. The default character with key '" + KEY_FOR_MISSED_CHARS +
                    "' will be used. Please merge the " +
                    "file content to the '" + NATIONAL_CHARS_TXT + "' file.";
                logger.info(message);
                fileWriter.append(character);
            } catch (Exception e) {
                throw new LabelizerRuntimeException(e);
            }
        }
        return KEY_FOR_MISSED_CHARS;
    }

    public boolean hasNext() {
        return hasMoreExamples();
    }

    private boolean hasMoreExamples() {
        return !dateExamples.isEmpty() || patternsCount > patternsPassed;
    }

    public MultiDataSet next() {
        try {
            return next(miniBatchSize);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new NoSuchElementException(e.getMessage());
        }
    }

    public MultiDataSet next(int miniBatches) {
        if (!hasMoreExamples()) {
            throw new NoSuchElementException();
        }
        List<Pair<String, String>> examples = new ArrayList<>(miniBatches);
        // Create pairs where first row is examples and second row is labels, for example
        // 1) 28.2.2019 11:45:00.123 1234567654 abcde 28.2.2018 11:46:00.124...
        // 2) ddddddddddddddddddddddwwwwwwwwwwwwwwwwwwdddddddddddddddddddddd...
        // Every pair has the same length and they equal with exampleLength
        for (int i = 0; i < miniBatches && hasMoreExamples(); i++) {
            StringBuilder left = new StringBuilder(exampleLength);
            StringBuilder right = new StringBuilder(exampleLength);

            int randomIndex = randomFromZeroToMaxInclusive(LinesWithDateClassification.NUM_SUB_LINES.size() - 1);
            int numSubLines = LinesWithDateClassification.NUM_SUB_LINES.get(randomIndex);
            prepareDataForLearning(left, right, numSubLines);
                
            String exampleString = left.toString();
            String labelsString = right.toString();
            Pair<String, String> pair = new Pair<>(exampleString, labelsString);
            examples.add(pair);
        }

        int currMinibatchSize = Math.min(miniBatches, examples.size());
        //Allocate space:
        //Note the order here:
        // dimension 0 = number of examples in minibatch
        // dimension 1 = size of each vector (i.e., number of characters or number of labels)
        // dimension 2 = length of each time series/example
        // Why 'f' order here? See https://jrmerwin.github.io/deeplearning4j-docs/usingrnns.html,
        // section "Alternative: Implementing a custom DataSetIterator"
        INDArray input = Nd4j.create(new int[]{currMinibatchSize, intToCharMap.size(), exampleLength}, ORDERING_FORTRAN); // NOSONAR
        INDArray inputHint = Nd4j.create(new int[]{currMinibatchSize, 2, exampleLength}, ORDERING_FORTRAN); // NOSONAR
        INDArray labels = Nd4j.create(new int[]{currMinibatchSize, ProbabilityLabel.values().length, exampleLength}, ORDERING_FORTRAN); // NOSONAR

        for (int miniBatchIndex = 0; miniBatchIndex < currMinibatchSize; miniBatchIndex++) {
            String stringLine = examples.get(miniBatchIndex).getLeft();
            
            String hintLine = SimilarityHint.linesSimilarityMarker(stringLine);

            String labelsLine = examples.get(miniBatchIndex).getRight();

            if (stringLine.length() != labelsLine.length()) {
                String lines =
                    "\nLength: '" + stringLine.length() + "', line  : '" + stringLine + "'" +
                        "\nLength: '" + labelsLine.length() + "', labels: '" + labelsLine + "'";
                throw new LabelizerRuntimeException("Line and labeled line should have same lengths.\n" + lines);
            }
            logger.info(MINI_BATCH, miniBatchIndex, stringLine);
            logger.info(MINI_BATCH, miniBatchIndex, labelsLine);
            logger.info(MINI_BATCH, miniBatchIndex, hintLine);
            logger.info("");
            for (int charIndex = 0; charIndex < stringLine.length(); charIndex++) {
                char exampleChar = stringLine.charAt(charIndex);
                char labelChar = labelsLine.charAt(charIndex);
                char hintChar = hintLine.charAt(charIndex);
                int exampleCharIndex = convertCharacterToIndex(exampleChar);
                int hintCharIndex = hintChar == 'n' ? 0 : 1;
                int labelCharIndex = ProbabilityLabel.findIndex(labelChar);
                input.putScalar(new int[]{miniBatchIndex, exampleCharIndex, charIndex}, 1.0);
                inputHint.putScalar(new int[]{miniBatchIndex, hintCharIndex, charIndex}, 1.0);
                labels.putScalar(new int[]{miniBatchIndex, labelCharIndex, charIndex}, 1.0);
            }
        }

        return new org.nd4j.linalg.dataset.MultiDataSet(
            new INDArray[]{input, inputHint},
            new INDArray[]{labels}
        );
    }

    @Override
    public void setPreProcessor(MultiDataSetPreProcessor preProcessor) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * Fill the buffers with data. These data will be used for training.
     * <p>
     * StringLine contains several sub-lines divided with line separators.
     * <p>
     * Sub-line can be a main line or additional line. The main line contains date, thread name and log level,
     * for example
     * <pre>
     *     01.11.2019;00:00:01.095 DEBUG [DefaultQuartzScheduler_Worker-9] [  /   ] - Main... (main line)
     * </pre>.
     * <p>
     * The additional line follows the main line, for example
     * <pre>
     *     01.11.2019;12:52:09.721 ERROR [DefaultQuartzScheduler_Worker-3] [  /   ] - Error in... (main line)
     *        at com.bla.security(Class.java:65) (additional line)
     * </pre>
     * Examples
     * <pre>
     *     main line
     *     additional line
     *     main line
     * </pre>
     * <pre>
     *     main line
     *     additional line
     *     additional line
     * </pre>
     * <pre>
     *     main line
     *     main line
     *     additional line
     * </pre>
     * <pre>
     *     additional line
     *     main line
     *     additional line
     * </pre>
     * Lines are divided with line separators, mixed Windows and Unix randomly.
     * The last line contains the separator as well.
     * <p>
     * IP address can be placed to the both main line and additional line.
     * 
     *
     * @param stringLine an empty buffer
     * @param labelsLine an empty buffer
     */
    @SuppressWarnings("unchecked")
    private void prepareDataForLearning(StringBuilder stringLine, StringBuilder labelsLine, int numSubLines) {
        Map<Integer, Boolean> isMainLine = new HashMap<>(numSubLines);
        for (int i = 0; i < numSubLines; i++) {
            boolean isMain = randomFromZeroToMaxInclusive(9) > 0;
            isMainLine.put(i, isMain);
        }
        if (LinesWithDateClassification.EXAMPLE_LENGTH_120 % numSubLines != 0) {
            throw new LabelizerRuntimeException("Constant EXAMPLE_LENGTH must be divided by " + numSubLines +
                " without remaining.");
        }
        int subLineLen = LinesWithDateClassification.EXAMPLE_LENGTH_120 / numSubLines;
        boolean dateIsFirst = randomFromZeroToMaxInclusive(9) > 0;
        boolean containsThreadName = randomFromZeroToMaxInclusive(9) > 0;
        boolean containsLogLevel = randomFromZeroToMaxInclusive(19) > 0;
        boolean threadNameBeforeLogLevel = randomFromZeroToMaxInclusive(1) == 0;

        DateExample dateExample = nextDateExample();
        Pair<String, String> beforeDate = null;
        Pair<String, String> threadName = null;
        Pair<String, String> dateLeftBoundary = null;
        Pair<String, String> dateRightBoundary = null;
        Pair<String, String> logLevelLeftBoundary = null;
        Pair<String, String> logLevelRightBoundary = null;
        String dateBoundary = BOUNDARIES.get(randomFromZeroToMaxInclusive(BOUNDARIES.size() - 1));
        String levelBoundary = BOUNDARIES.get(randomFromZeroToMaxInclusive(BOUNDARIES.size() - 1));
        for (int subLineNum = 0; subLineNum < numSubLines; subLineNum++) {
            StringBuilder line = new StringBuilder(subLineLen);
            StringBuilder labels = new StringBuilder(subLineLen);

            String newLine = randomFromZeroToMaxInclusive(1) == 0 ? "\r\n" : "\n";
            String newLineLabels = StringUtils.leftPad("", newLine.length(),
                ProbabilityLabel.N_WITHOUT_DATE.getCharacter());
            Pair<String, String> newLinePair = new Pair<>(newLine, newLineLabels);
            int remaining = subLineLen - newLine.length();

            boolean isMain = isMainLine.get(subLineNum);

            Pair<String, String> date = nextDate(dateExample, isMain);
            remaining = remaining - date.getLeft().length();

            dateLeftBoundary = dateLeftBoundary(dateLeftBoundary, isMain, dateIsFirst, dateBoundary.substring(0, 1));
            remaining = remaining - dateLeftBoundary.getLeft().length();

            dateRightBoundary = dateRightBoundary(dateRightBoundary, isMain, dateBoundary.substring(1));
            remaining = remaining - dateRightBoundary.getLeft().length();

            beforeDate = getBeforeDate(beforeDate, dateIsFirst, isMain, remaining);
            remaining = remaining - beforeDate.getLeft().length();

            threadName = getThreadName(threadName, containsThreadName, isMain, remaining);
            Pair<String, String> threadNameCurrent = isMain ? threadName : new Pair<>("", "");
            remaining = remaining - threadNameCurrent.getLeft().length();
            
            Pair<String, String> logLevel = getLogLevel(isMain, containsLogLevel, remaining);
            remaining = remaining - logLevel.getLeft().length();

            logLevelLeftBoundary = logLevelBoundary(logLevelLeftBoundary, isMain, containsLogLevel, remaining,
                levelBoundary.substring(0, 1));
            remaining = remaining - logLevelLeftBoundary.getLeft().length();
            
            logLevelRightBoundary = logLevelBoundary(logLevelRightBoundary, isMain, containsLogLevel, remaining,
                levelBoundary.substring(1));
            remaining = remaining - logLevelRightBoundary.getLeft().length();

            StringBuilder logLevelFinalString = new StringBuilder();
            StringBuilder logLevelFinalLabels = new StringBuilder();
            appendAll(logLevelFinalString, logLevelFinalLabels, logLevelLeftBoundary, logLevel, logLevelRightBoundary);
            Pair<String, String> logLevelFinal = new Pair<>(logLevelFinalString.toString(), logLevelFinalLabels.toString());

            boolean generateIpAddress = randomFromZeroToMaxInclusive(4) == 0;
            Pair<String, String> ipAddress = getIpAddress(generateIpAddress, remaining);
            remaining = remaining - ipAddress.getLeft().length();
            
            appendAll(line, labels, beforeDate, dateLeftBoundary, date, dateRightBoundary);
            
            List<Pair<String, String>> subFields;
            
            if (threadNameBeforeLogLevel) {
                subFields = Arrays.asList(threadNameCurrent, logLevelFinal, ipAddress);
            } else {
                subFields = Arrays.asList(logLevelFinal, threadNameCurrent, ipAddress);
            }
            
            int fillersLen = remaining / subFields.size();
            if (fillersLen < 0) {
                fillersLen = 0;
            }

            applyFilling(line, labels, remaining, subFields, fillersLen);

            int lenWithNewLine = line.length() + newLine.length();
            if (lenWithNewLine > subLineLen) {
                line.setLength(subLineLen - newLine.length());
                labels.setLength(subLineLen - newLine.length());
            } else {
                Pair<String, String> lastFiller = lineFiller.generateFiller(subLineLen - lenWithNewLine);
                appendAll(line, labels, lastFiller);
            }
            appendAll(line, labels, newLinePair);
            
            stringLine.append(line);
            labelsLine.append(labels);
        }

        validateLength(stringLine, labelsLine);
    }

    @SuppressWarnings("unchecked")
    private void applyFilling(StringBuilder line, StringBuilder labels, int remaining, List<Pair<String, String>> subFields, int fillersLen) {
        for (Pair<String, String> pair : subFields) {
            Pair<String, String> filler = lineFiller.generateFiller(fillersLen);
            remaining = remaining - filler.getFirst().length();

            appendAll(line, labels, filler, pair);
        }
    }

    private void validateLength(StringBuilder stringLine, StringBuilder labelsLine) {
        if (stringLine.length() != LinesWithDateClassification.EXAMPLE_LENGTH_120) {
            throw new LabelizerRuntimeException("StringLine length " + stringLine.length() +
                " not equals with " + LinesWithDateClassification.EXAMPLE_LENGTH_120 + ", " +
                "stringLine: " + stringLine);
        }

        if (labelsLine.length() != LinesWithDateClassification.EXAMPLE_LENGTH_120) {
            throw new LabelizerRuntimeException("LabelsLine length " + labelsLine.length() +
                " not equals with " + LinesWithDateClassification.EXAMPLE_LENGTH_120 + ", " +
                "labelsLine: " + labelsLine);
        }
    }

    private Pair<String, String> dateRightBoundary(Pair<String, String> dateRightBoundary, boolean isMain,
                                                   String boundary) {
        if (dateRightBoundary != null) {
            return dateRightBoundary;
        }
        if (!isMain) {
            return new Pair<>("", "");
        }
        String labels = StringUtils.leftPad("", boundary.length(), ProbabilityLabel.N_WITHOUT_DATE.getString());
        return new Pair<>(boundary, labels);
    }

    private Pair<String, String> dateLeftBoundary(Pair<String, String> dateLeftBoundary, boolean isMain,
                                                  boolean dateIsFirst, String boundary) {
        if (dateLeftBoundary != null) {
            return dateLeftBoundary;
        }
        if (!isMain || dateIsFirst) {
            return new Pair<>("", "");
        }
        String labels = StringUtils.leftPad("", boundary.length(), ProbabilityLabel.N_WITHOUT_DATE.getString());
        return new Pair<>(boundary, labels);
    }

    @SuppressWarnings("unchecked")
    private void appendAll(StringBuilder line, StringBuilder labels, Pair<String, String>... pairs) {
        for (Pair<String, String> pair : pairs) {
            line.append(pair.getKey());
            labels.append(pair.getValue());
        }
    }

    private Pair<String, String> getIpAddress(boolean generateIpAddress, int remaining) {
        if (!generateIpAddress) {
            return new Pair<>("", "");
        }
        String boundaries = BOUNDARIES.get(randomFromZeroToMaxInclusive(BOUNDARIES.size() - 1));
        String ip = IpGenerator.randomIp();
        if (boundaries.length() + ip.length() > remaining) {
            return new Pair<>("", "");
        }
        String result = boundaries.substring(0, 1) + ip + boundaries.substring(1);
        String labels = ProbabilityLabel.N_WITHOUT_DATE.getString() +
            StringUtils.leftPad("", ip.length(), ProbabilityLabel.I_IP_ADDRESS_AND_PORT.getCharacter()) +
            ProbabilityLabel.N_WITHOUT_DATE.getString();
        return new Pair<>(result, labels);
    }

    private Pair<String, String> getLogLevel(boolean isMain, boolean containsLogLevel, int remaining) {
        if (!isMain || !containsLogLevel) {
            return new Pair<>("", "");
        }
        String level = LOG_LEVELS.get(randomFromZeroToMaxInclusive(LOG_LEVELS.size() - 1));
        if (remaining < level.length()) {
            return new Pair<>("", "");
        }
        String labels = StringUtils.leftPad("", level.length(), ProbabilityLabel.L_LOG_LEVEL.getString());
        return new Pair<>(level, labels);
    }

    private Pair<String, String> logLevelBoundary(Pair<String, String> logLevelLeftBoundary, boolean isMain,
                                                  boolean containsLogLevel, int remaining, String boundary) {
        if (logLevelLeftBoundary != null) {
            return logLevelLeftBoundary;
        }
        if (!isMain || !containsLogLevel || remaining < boundary.length()) {
            return new Pair<>("", "");
        }
        String labels = StringUtils.leftPad("", boundary.length(), ProbabilityLabel.N_WITHOUT_DATE.getString());
        return new Pair<>(boundary, labels);
    }

    private Pair<String, String> getThreadName(Pair<String, String> threadName, boolean containsThreadName, boolean isMain, int remaining) {
        if (threadName != null) {
            return threadName;
        }
        if (!containsThreadName || !isMain) {
            return new Pair<>("", "");
        }
        boolean useThreadSubstring = randomFromZeroToMaxInclusive(9) == 9;
        String threadSubstring = "";
        boolean before = randomFromZeroToMaxInclusive(1) == 1;
        if (useThreadSubstring) {
            String separator = SEPARATORS.get(randomFromZeroToMaxInclusive(SEPARATORS.size() - 1));
            threadSubstring = THREAD_COMMON_NAMES.get(randomFromZeroToMaxInclusive(THREAD_COMMON_NAMES.size() - 1));
            if (before) {
                threadSubstring = threadSubstring + separator;
            } else {
                threadSubstring = separator + threadSubstring;
            }
        }
        int randomLen = getRandom(3, 15);
        int length = Math.min(remaining - threadSubstring.length(), randomLen + threadSubstring.length());
        if (length < 1) {
            return new Pair<>("", "");
        }
        String filler = lineFiller.generateFiller(length).getKey();
        String threadNameWithoutBoundaries;
        if (before) {
            threadNameWithoutBoundaries = threadSubstring + filler;
        } else {
            threadNameWithoutBoundaries = filler + threadSubstring;
        }

        String boundary = BOUNDARIES.get(randomFromZeroToMaxInclusive(BOUNDARIES.size() - 1));
        String threadNameFinal = boundary.substring(0, 1) + threadNameWithoutBoundaries + boundary.substring(1);
        String labels = ProbabilityLabel.N_WITHOUT_DATE.getString() +
            StringUtils.leftPad("", threadNameWithoutBoundaries.length(), ProbabilityLabel.T_THREAD.getCharacter()) +
            ProbabilityLabel.N_WITHOUT_DATE.getString();
        return new Pair<>(threadNameFinal, labels);
    }

    private Pair<String, String> getBeforeDate(Pair<String, String> beforeDate, boolean dateIsFirst, boolean isMain, int remaining) {
        if (beforeDate != null) {
            return beforeDate;
        }
        if (!isMain || dateIsFirst) {
            return new Pair<>("", "");
        }
        if (remaining < 1) {
            remaining = 1;
        }
        int fillersLen = randomFromZeroToMaxInclusive(Math.min(19, remaining - 1));
        return lineFiller.generateFiller(fillersLen);
    }

    private Pair<String, String> nextDate(DateExample dateExample, boolean isMain) {
        if (!isMain) {
            return new Pair<>("", "");
        }
        Pair<String, String> result = new Pair<>(dateExample.getSource(), dateExample.getLabels());
        
        // prepare the data for next invocation
        Date date = dateExample.getDate();
        String pattern = dateExample.getPattern();
        Locale locale = dateExample.getLocale();
        TimeZone timeZone = dateExample.getTimeZone();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, locale);
        simpleDateFormat.setTimeZone(timeZone);
        int oneSecond = 1000;
        int randomAddition = randomFromZeroToMaxInclusive(oneSecond);
        date.setTime(date.getTime() + randomAddition);
        String dateString = simpleDateFormat.format(date);
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(date);
        String labels = TrainingDataGenerator.findLabels(dateString, pattern, locale, gregorianCalendar, timeZone);
        dateExample.setDate(date);
        dateExample.setSource(dateString);
        dateExample.setLabels(labels);
        
        return result;
    }

    /**
     * @return A {@link DateExample} from {@link #dateExamples} or 'null'. The {@link #dateExamples} is filled with
     * the {@link PagePatternRepository#getNotTrainedPattern()} method.
     */
    private DateExample nextDateExample() {
        if (dateExamples.isEmpty()) {
            PagePatternRepository pagePatternRepository = PagePatternRepository.getInstance();
            PagePattern pagePattern = pagePatternRepository.getNotTrainedPattern();
            if (lastPagePattern != null) {
                lastPagePattern.setTrained(true);
                pagePatternRepository.save(lastPagePattern);
                patternsPassed = patternsCount - pagePatternRepository.countNotTrainedPatterns();
            }
            lastPagePattern = pagePattern;
            dateExamples.addAll(TrainingDataGenerator.generateDates(pagePattern, NUM_EXAMPLES_OF_DATE_PATTERN_100));
            if (dateExamples.isEmpty()) {
                logger.info("List of dateExamples is empty. Last PagePattern: {}", lastPagePattern);
                return nextDateExample();
            }
        }
        return dateExamples.remove(0);
    }

    /**
     * @return Size of {@link #intToCharMap}.
     */
    public int inputColumns() {
        return intToCharMap.size();
    }

    /**
     * @return Number of {@link ProbabilityLabel}s.
     */
    public int totalOutcomes() {
        return ProbabilityLabel.values().length;
    }

    public void reset() {
        PagePatternRepository.getInstance().resetTrained();
        patternsPassed = 0;
        patternsCount = PagePatternRepository.getInstance().countNotTrainedPatterns();
        lastPagePattern = null;
    }

    public boolean resetSupported() {
        return true;
    }

    @Override
    public boolean asyncSupported() {
        return true;
    }

    @Override
    public MultiDataSetPreProcessor getPreProcessor() {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEachRemaining(Consumer<? super MultiDataSet> action) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    private static int randomFromZeroToMaxInclusive(int max) {
        if (max <= 0) {
            return 0;
        }
        return getRandom(0, max + 1);
    }

    private static int getRandom(int minInclusive, int maxExclusive) {
        if (minInclusive >= maxExclusive) {
            return minInclusive;
        }
        return ThreadLocalRandom.current().nextInt(minInclusive, maxExclusive);
    }

    /**
     * Parse an input line and mark some digits as {@link ProbabilityLabel#Y_YEAR} (<b>y</b>)
     * and others as {@link ProbabilityLabel#N_WITHOUT_DATE} (<b>n</b>).
     * <p>
     * Digits are considered as a year (<b>y</b>) when they are a part of a year
     * from 1980 to current year + 1 (next year).
     * <p>
     * Year can be formatted as 4 digits, for example 2019 or two digits, for example 19.
     *
     * @param line input with (or without) date pattern(s), for example <i>abc 2019.05.10 def</i>
     * @return for example <i>nnnnyyyynyynyynnnn</i>
     */
    public static String yearHintLenient(String line) {
        StringBuilder result = new StringBuilder(line.length());
        StringBuilder context4 = new StringBuilder(4);
        StringBuilder context2 = new StringBuilder(2);
        for (char character : line.toCharArray()) {
            result.append(ProbabilityLabel.N_WITHOUT_DATE.getCharacter());
            if (Character.isDigit(character)) {
                processDigit(result, context4, context2, character);
            } else {
                context4.setLength(0);
                context2.setLength(0);
            }
        }
        return result.toString();
    }

    private static void processDigit(StringBuilder result, StringBuilder context4, StringBuilder context2,
                                     char character) {
        context4.append(character);
        context2.append(character);

        if (context4.length() == 4) {
            if (isDate(Integer.parseInt(context4.toString()))) {
                writeToResult(result, 4);
            }
            context4.deleteCharAt(0);
        }
        if (context2.length() == 2) {
            if (isDate(Integer.parseInt(context2.toString()))) {
                writeToResult(result, 2);
            }
            context2.deleteCharAt(0);
        }
    }

    private static void writeToResult(StringBuilder result, int numToAppend) {
        String labels = StringUtils.rightPad("", numToAppend, ProbabilityLabel.Y_YEAR.getCharacter());
        result.setLength(result.length() - numToAppend);
        result.append(labels);
    }

    private static boolean isDate(Integer contextResult) {
        return ((contextResult >= Hint.OLDEST_YEAR && contextResult <= Hint.ACTUAL_YEAR + 1)
            || (contextResult >= Hint.SHORT_ZERO_YEAR && contextResult <= Hint.SHORT_ACTUAL_YEAR + 1)
            || (contextResult >= Hint.SHORT_OLD_YEAR && contextResult < Hint.SHORT_HELPFULL_YEAR));
    }

    public static int countOfSuccessfullyMarkedChars(String recognizedOutput, String expectedOutput) {
        if (recognizedOutput.length() != expectedOutput.length()) {
            throw new LabelizerRuntimeException("RecognizedOutput and expectedOutput length are not equal. " +
                "recognizedOutput: '" + recognizedOutput +
                "', expectedOutput: '" + expectedOutput +
                "'");
        }
        int result = 0;
        for (int index = 0; index < recognizedOutput.length(); index++) {
            if (recognizedOutput.charAt(index) == expectedOutput.charAt(index)) {
                result++;
            }
        }
        return result;
    }

    /**
     * How many lines for training contains the {@link CharIterator}. The value is calculated from {@link #patternsCount}
     * multiplied with {@link #NUM_EXAMPLES_OF_DATE_PATTERN_100}.
     */
    public long trainingDataSetSize() {
        return NUM_EXAMPLES_OF_DATE_PATTERN_100 * patternsCount;
    }

    /**
     * How many lines for training remained in the {@link CharIterator}. The value is calculated from {@link #trainingDataSetSize()}
     * minus {@link #patternsPassed} multiplied with {@link #NUM_EXAMPLES_OF_DATE_PATTERN_100}.
     */
    public long getRemainingDataSetSize() {
        return trainingDataSetSize() - (patternsPassed * NUM_EXAMPLES_OF_DATE_PATTERN_100);
    }

    /**
     * If the {@link #dateExamples} is empty, return 'true'. It means all the examples has been trained and the next
     * {@link PagePattern} can be loaded from the {@link PagePatternRepository#getNotTrainedPattern()} if exists.
     */
    public boolean isPatternTrained() {
        return dateExamples.isEmpty();
    }
}
