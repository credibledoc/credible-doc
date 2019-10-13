package com.credibledoc.log.labelizer.iterator;

import com.credibledoc.log.labelizer.date.DateExample;
import com.credibledoc.log.labelizer.date.ProbabilityLabel;
import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;
import com.credibledoc.log.labelizer.hint.Hint;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class CharIterator implements MultiDataSetIterator {
    public static final String RESOURCES_DIR = "vectors/labeled";
    public static final int EXAMPLE_LENGTH = 100;
    private static final Logger logger = LoggerFactory.getLogger(CharIterator.class);
    private static final String NOT_IMPLEMENTED = "Not implemented";
    private static final String NATIONAL_CHARS_TXT = "chars/nationalChars.txt";
    private static final char ORDERING_FORTRAN = 'f';
    private static final String DATE_FOLDER = "date";
    private static final String WITHOUT_DATE_FOLDER = "without";
    private static final int BOUNDARY_LENGTH_2 = 2;
    private static final String MINI_BATCH = "MiniBatch: {}: {}";

    /**
     * Maps each character to an index in the input/output. These characters is used in train and test data.
     * <p>
     * Key is an order number and value is a character.
     */
    private Map<Integer, Character> intToCharMap;

    /**
     * Examples of a date string where individual parts are marked with labels.
     */
    private transient List<DateExample> dateExamples;

    /**
     * Examples of lines without date or other labels.
     */
    private List<String> linesWithoutDate;


    /**
     * Length of each example/minibatch (number of characters)
     */
    private int exampleLength;

    /**
     * Size of each minibatch (number of examples)
     */
    private int miniBatchSize;

    /**
     * Offsets for the start of next {@link #dateExamples} example
     */
    private int linesOffset = 0;

    private static final List<Character> PUNCTUATIONS = new ArrayList<>(Arrays.asList('!', '&', '(', ')', '?', '-',
        '\\', ',', '.', '\"', ':', ';', ' '));
    private static final List<Character> SMALL_LETTERS = listChars('a', 'z');
    private static final List<Character> LARGE_LETTERS = listChars('A', 'Z');
    private static final List<Character> DIGITS = listChars('0', '9');
    private static final List<Character> DIGITS_AND_LETTERS = new ArrayList<>();
    private static final List<Character> DIGITS_AND_LETTERS_AND_PUNCTUATIONS = new ArrayList<>();
    private static final List<String> BOUNDARIES = new ArrayList<>(Arrays.asList("||", "  ", "{}", "()", "//",
        ",,", "\"\"", "''", "--", "__", "[]"));
    
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
        ClassPathResource resource = new ClassPathResource(CharIterator.RESOURCES_DIR + "/../" + NATIONAL_CHARS_TXT);
        File file = resource.getFile();
        if (!file.exists()) {
            throw new LabelizerRuntimeException("File not found: '" + file.getAbsolutePath() + "'");
        }
        return new String(Files.readAllBytes(file.toPath()));
    }

    /**
     * @param resourcesDirPath     Path to text file to use for generating samples
     * @param textFileEncoding Encoding of the text file. Can try Charset.defaultCharset()
     * @param miniBatchSize    Number of examples per mini-batch
     * @param exampleLength    Number of characters in each input/output vector
     * @throws IOException If text file cannot be loaded
     */
    public CharIterator(String resourcesDirPath, Charset textFileEncoding, int miniBatchSize,
                        int exampleLength) throws IOException {
        if (!new File(resourcesDirPath).exists()) {
            throw new IOException("Could not access file (does not exist): " + resourcesDirPath);
        }
        if (miniBatchSize <= 0) {
            throw new IllegalArgumentException("Invalid miniBatchSize (must be > 0)");
        }
        this.exampleLength = exampleLength;
        this.miniBatchSize = miniBatchSize;

        //Store valid characters is a map for later use in vectorization
        initCharToIdxMap();

        dateExamples = readDateExamples(resourcesDirPath, textFileEncoding);
        linesWithoutDate = readLinesFromFolder(resourcesDirPath, textFileEncoding, WITHOUT_DATE_FOLDER);
    }

    private List<DateExample> readDateExamples(String resourcesDirPath, Charset textFileEncoding) {
        try {
            List<String> exampleLines = readLinesFromFolder(resourcesDirPath, textFileEncoding, DATE_FOLDER);
            List<DateExample> result = new ArrayList<>();
            ObjectMapper objectMapper = new ObjectMapper();
            for (String json : exampleLines) {
                DateExample dateExample = objectMapper.readValue(json, DateExample.class);
                result.add(dateExample);
            }
            return result;
        } catch (Exception e) {
            throw new LabelizerRuntimeException(e);
        }
    }

    public static String randomWordChar() {
        int randomIndex = randomFromZeroToMaxInclusive(DIGITS_AND_LETTERS_AND_PUNCTUATIONS.size() - 1);
        return String.valueOf(DIGITS_AND_LETTERS_AND_PUNCTUATIONS.get(randomIndex));
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
        throw new LabelizerRuntimeException("Cannot find index for character: '" + character + "'.");
    }

    public boolean hasNext() {
        return hasMoreExamples();
    }

    private boolean hasMoreExamples() {
        return dateExamples.size() - 1 > linesOffset &&
            linesWithoutDate.size() - 1 > linesOffset;
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

            prepareDataForLearning(left, right);
                
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
        
        INDArray input = Nd4j
            .create(new int[]{currMinibatchSize, intToCharMap.size(), exampleLength}, ORDERING_FORTRAN);
        
        INDArray inputHint = Nd4j
            .create(new int[]{currMinibatchSize, intToCharMap.size(), exampleLength}, ORDERING_FORTRAN);
        
        INDArray labels = Nd4j
            .create(new int[]{currMinibatchSize, ProbabilityLabel.values().length, exampleLength}, ORDERING_FORTRAN);

        for (int miniBatchIndex = 0; miniBatchIndex < currMinibatchSize; miniBatchIndex++) {
            String stringLine = examples.get(miniBatchIndex).getLeft();
            String hintLine = yearHintLenient(stringLine);

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
                int hintCharIndex = convertCharacterToIndex(hintChar);
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
     * Fill the buffers with data.
     * @param stringLine an empty buffer
     * @param labelsLine an empty buffer
     */
    private void prepareDataForLearning(StringBuilder stringLine, StringBuilder labelsLine) {
        String withoutDate = linesWithoutDate.get(linesOffset);
        if (withoutDate.length() > EXAMPLE_LENGTH) {
            // WithoutDate linee can be longer in noDatesManual.txt
            withoutDate = withoutDate.substring(0, EXAMPLE_LENGTH);
        }

        DateExample dateExample = dateExamples.get(linesOffset++);

        int boundaryIndex = randomFromZeroToMaxInclusive(BOUNDARIES.size() - 1);
        String boundary = BOUNDARIES.get(boundaryIndex);

        if (dateExample.getSource().length() + withoutDate.length() + 1 <= EXAMPLE_LENGTH) {
            stringLine.append(dateExample.getSource());
            labelsLine.append(dateExample.getLabels());

            stringLine.append(boundary.substring(1));
            labelsLine.append(ProbabilityLabel.N_WITHOUT_DATE.getString());

            stringLine.append(stringLine);
            labelsLine.append(StringUtils.rightPad("", stringLine.length(),
                ProbabilityLabel.N_WITHOUT_DATE.getString()));

            fillPadding(stringLine, labelsLine);

            return;
        }
        
        int lengthWithoutDateAndBoundary = withoutDate.length() - dateExample.getSource().length() - BOUNDARY_LENGTH_2;
        if (lengthWithoutDateAndBoundary < 0) {
            throw new LabelizerRuntimeException("Date is longer then withoutDate + 3. " +
                "Date: '" + dateExample.getSource() + "', " +
                "withoutDate: '" + withoutDate + "'");
        }
        int startIndex = randomFromZeroToMaxInclusive(lengthWithoutDateAndBoundary);
        
        stringLine.append(withoutDate, 0, startIndex);
        labelsLine.append(StringUtils.rightPad("", stringLine.length(), ProbabilityLabel.N_WITHOUT_DATE.getCharacter()));
        if (startIndex > 0) {
            stringLine.append(boundary, 0, 1);
            labelsLine.append(ProbabilityLabel.N_WITHOUT_DATE.getCharacter());
        }

        stringLine.append(dateExample.getSource());
        labelsLine.append(dateExample.getLabels());

        stringLine.append(boundary, 1, 1 + 1);
        labelsLine.append(ProbabilityLabel.N_WITHOUT_DATE.getCharacter());

        stringLine.append(withoutDate.substring(stringLine.length()));

        fillPadding(stringLine, labelsLine);
    }

    private void fillPadding(StringBuilder stringLine, StringBuilder labelsLine) {
        int remaining = EXAMPLE_LENGTH - stringLine.length();
        if (remaining > 0) {
            // line padding
            String lineFiller = StringUtils.leftPad("", remaining, " ");
            stringLine.append(lineFiller);
        }
        // labels padding
        int labelsRemaining = EXAMPLE_LENGTH - labelsLine.length();
        if (labelsRemaining > 0) {
            String labelsFiller = StringUtils.leftPad("", labelsRemaining,
                ProbabilityLabel.N_WITHOUT_DATE.getString());
            labelsLine.append(labelsFiller);
        }
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
        linesOffset = 0;
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
        return ThreadLocalRandom.current().nextInt(0, max + 1);
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

    public static int countOfNotMarkedCharsInDatePattern(String recognizedOutput, String expectedOutput) {
        int result = 0;
        for (int index = 0; index < recognizedOutput.length(); index++) {
            char expectedChar = expectedOutput.charAt(index);
            char recognizedChar = recognizedOutput.charAt(index);
            ProbabilityLabel probabilityLabel = ProbabilityLabel.find(expectedChar);
            if (probabilityLabel != null &&
                ProbabilityLabel.dates.contains(probabilityLabel) &&
                recognizedChar != expectedChar) {
                result++;
            }
        }
        return result;
    }

    /**
     * How many train lines contains this {@link CharIterator}.
     * @return Min value of {@link #linesWithoutDate} and {@link #dateExamples} sizes.
     */
    public int trainDataSetSize() {
        return Math.min(linesWithoutDate.size(), dateExamples.size());
    }

    /**
     * @return The current value of {@link #linesOffset}.
     */
    public int getLinesOffset() {
        return linesOffset;
    }

    /**
     * @param linesOffset see the {@link #linesOffset} field description.
     */
    public void setLinesOffset(int linesOffset) {
        this.linesOffset = linesOffset;
    }
}
