package com.credibledoc.log.labelizer.iterator;

import com.credibledoc.log.labelizer.classifier.LinesWithDateClassification;
import com.credibledoc.log.labelizer.date.DateLabel;
import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.primitives.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CharIterator implements DataSetIterator {
    private static final Logger logger = LoggerFactory.getLogger(CharIterator.class);
    public static final int NUM_LABELS_2 = 2;
    private static final String NOT_IMPLEMENTED = "Not implemented";

    /**
     * Maps each character to an index ind the input/output. These characters is used in train and test data.
     */
    private Map<Integer, Character> intToCharMap;

    /**
     * All characters of the input file (after filtering to only those that are valid) labeled as "date"
     */
    private List<String> linesWithDate;

    /**
     * All characters of the input file (after filtering to only those that are valid) labeled as "without"
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
     * Offsets for the start of next {@link #linesWithDate} example
     */
    private int linesWithDateOffset = 0;

    /**
     * Offsets for the start of next {@link #linesWithoutDate} example
     */
    private int linesWithoutDateOffset = 0;

    private static final List<Character> PUNCTUATIONS = new ArrayList<>();
    private static final List<Character> SMALL_LETTERS = listChars('a', 'z');
    private static final List<Character> LARGE_LETTERS = listChars('A', 'Z');
    private static final List<Character> DIGITS = listChars('0', '9');
    private static final List<Character> DIGITS_AND_LETTERS = new ArrayList<>();
    private static final List<Character> DIGITS_AND_LETTERS_AND_PUNCTUATIONS = new ArrayList<>();
    
    static {
        PUNCTUATIONS.addAll(Arrays.asList('!', '&', '(', ')', '?', '-', '\\', ',', '.', '\"', ':', ';', ' '));
        DIGITS_AND_LETTERS.addAll(LARGE_LETTERS);
        DIGITS_AND_LETTERS.addAll(SMALL_LETTERS);
        DIGITS_AND_LETTERS.addAll(DIGITS);
        DIGITS_AND_LETTERS_AND_PUNCTUATIONS.addAll(DIGITS_AND_LETTERS);
        DIGITS_AND_LETTERS_AND_PUNCTUATIONS.addAll(PUNCTUATIONS);
    }
    
    /**
     * @param textFilePath     Path to text file to use for generating samples
     * @param textFileEncoding Encoding of the text file. Can try Charset.defaultCharset()
     * @param miniBatchSize    Number of examples per mini-batch
     * @param exampleLength    Number of characters in each input/output vector
     * @throws IOException If text file cannot be loaded
     */
    public CharIterator(String textFilePath, Charset textFileEncoding, int miniBatchSize,
                        int exampleLength) throws IOException {
        if (!new File(textFilePath).exists()) {
            throw new IOException("Could not access file (does not exist): " + textFilePath);
        }
        if (miniBatchSize <= 0) {
            throw new IllegalArgumentException("Invalid miniBatchSize (must be > 0)");
        }
        this.exampleLength = exampleLength;
        this.miniBatchSize = miniBatchSize;

        //Store valid characters is a map for later use in vectorization
        initCharToIdxMap();

        linesWithDate = readFilesToCharArray(textFilePath, textFileEncoding, "date");
        linesWithoutDate = readFilesToCharArray(textFilePath, textFileEncoding, "without");
    }

    public static String randomWordChar() {
        int randomIndex = ThreadLocalRandom.current().nextInt(0, DIGITS_AND_LETTERS_AND_PUNCTUATIONS.size());
        return String.valueOf(DIGITS_AND_LETTERS_AND_PUNCTUATIONS.get(randomIndex));
    }

    private List<String> readFilesToCharArray(String labeledDatePath, Charset textFileEncoding,
                                              String labeledDir) throws IOException {
        File dateDir = new File(labeledDatePath, labeledDir);
        Collection<File> dateFiles = FileUtils.listFiles(dateDir, null, false);
        List<String> exampleLines = new ArrayList<>();
        for (File file : dateFiles) {
            exampleLines.addAll(Files.readAllLines(file.toPath(), textFileEncoding));
        }
        for (String line : exampleLines) {
            char[] thisLine = line.toCharArray();
            for (char nextChar : thisLine) {
                if (!intToCharMap.containsValue(nextChar)) {
                    throw new LabelizerRuntimeException("Cannot find character in charToIdxMap. " +
                        "Character: '" + nextChar + "'.");
                }
            }
        }
        return exampleLines;
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
        List<Character> validChars = new ArrayList<>();
        validChars.addAll(DIGITS_AND_LETTERS);
        validChars.addAll(PUNCTUATIONS);
        
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
        return linesWithDate.size() - 1 > linesWithDateOffset &&
            linesWithoutDate.size() - 1 > linesWithoutDateOffset;
    }

    public DataSet next() {
        try {
            return next(miniBatchSize);
        } catch (Exception e) {
            throw new NoSuchElementException(e.getMessage());
        }
    }

    public DataSet next(int miniBatches) {
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
                
            String datesString = left.toString();
            Pair<String, String> pair = new Pair<>(datesString, right.toString());
            examples.add(pair);
        }

        int currMinibatchSize = Math.min(miniBatches, examples.size());
        //Allocate space:
        //Note the order here:
        // dimension 0 = number of examples in minibatch
        // dimension 1 = size of each vector (i.e., number of characters)
        // dimension 2 = length of each time series/example
        //Why 'f' order here? See http://deeplearning4j.org/usingrnns.html#data section "Alternative: Implementing a 
        // custom DataSetIterator"
        INDArray input = Nd4j.create(new int[]{currMinibatchSize, intToCharMap.size(), exampleLength}, 'f');
        INDArray labels = Nd4j.create(new int[]{currMinibatchSize, NUM_LABELS_2, exampleLength}, 'f');

        for (int miniBatchIndex = 0; miniBatchIndex < currMinibatchSize; miniBatchIndex++) {
            for (int charIndex = 0; charIndex < examples.get(miniBatchIndex).getLeft().length(); charIndex++) {
                char exampleChar = examples.get(miniBatchIndex).getLeft().charAt(charIndex);
                char labelChar = examples.get(miniBatchIndex).getRight().charAt(charIndex);
                int exampleCharIndex = convertCharacterToIndex(exampleChar);
                int labelCharIndex = DateLabel.findIndex(labelChar);
                input.putScalar(new int[]{miniBatchIndex, exampleCharIndex, charIndex}, 1.0);
                labels.putScalar(new int[]{miniBatchIndex, labelCharIndex, charIndex}, 1.0);
            }
            logger.info("MiniBatch: {}: {}", miniBatchIndex, examples.get(miniBatchIndex).getLeft());
            logger.info("MiniBatch: {}: {}", miniBatchIndex, examples.get(miniBatchIndex).getRight());
            logger.info("");
        }

        return new DataSet(input, labels);
    }

    private void prepareDataForLearning(StringBuilder left, StringBuilder right) {
        String withoutDate = linesWithoutDate.get(linesWithoutDateOffset++);
        if (withoutDate.length() < LinesWithDateClassification.EXAMPLE_LENGTH) {
            withoutDate = StringUtils.rightPad(withoutDate, LinesWithDateClassification.EXAMPLE_LENGTH, " ");
        }
        String withDate = linesWithDate.get(linesWithDateOffset++);
        int startIndex = ThreadLocalRandom.current().nextInt(0, withoutDate.length() - withDate.length());
        left.append(withoutDate, 0, startIndex);
        right.append(StringUtils.rightPad("", left.length(), DateLabel.W_WITHOUT_DATE.getCharacter()));

        left.append(withDate);
        right.append(StringUtils.rightPad("", withDate.length(), DateLabel.D_DATE.getCharacter()));

        left.append(withoutDate.substring(left.length()));
        right.append(StringUtils.rightPad(
            "",
            LinesWithDateClassification.EXAMPLE_LENGTH - right.length(),
            DateLabel.W_WITHOUT_DATE.getCharacter()));
    }

    public int inputColumns() {
        return intToCharMap.size();
    }

    public int totalOutcomes() {
        return NUM_LABELS_2;
    }

    public void reset() {
        linesWithDateOffset = 0;
        linesWithoutDateOffset = 0;
    }

    public boolean resetSupported() {
        return true;
    }

    @Override
    public boolean asyncSupported() {
        return true;
    }

    public int batch() {
        return miniBatchSize;
    }

    public void setPreProcessor(DataSetPreProcessor preProcessor) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public DataSetPreProcessor getPreProcessor() {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public List<String> getLabels() {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
