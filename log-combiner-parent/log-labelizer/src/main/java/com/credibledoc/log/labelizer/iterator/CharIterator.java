package com.credibledoc.log.labelizer.iterator;

import com.credibledoc.log.labelizer.date.DateLabel;
import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;
import org.apache.commons.io.FileUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
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

public class CharIterator implements DataSetIterator {
    private static final Logger logger = LoggerFactory.getLogger(CharIterator.class);
    public static final int NUM_LABELS_2 = 2;
    private static final String ONE_SPACE = " ";
    private static final String NOT_IMPLEMENTED = "Not implemented";

    /**
     * Maps each character to an index ind the input/output. These characters is used in train and test data.
     */
    private Map<Character, Integer> charToIdxMap;

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
     * Random for the {@link #getRandomCharacter()} method.
     */
    private Random rng;

    /**
     * Offsets for the start of next {@link #linesWithDate} example
     */
    private int linesWithDateOffset = 0;

    /**
     * Offsets for the start of next {@link #linesWithoutDate} example
     */
    private int linesWithoutDateOffset = 0;

    /**
     * @param textFilePath     Path to text file to use for generating samples
     * @param textFileEncoding Encoding of the text file. Can try Charset.defaultCharset()
     * @param miniBatchSize    Number of examples per mini-batch
     * @param exampleLength    Number of characters in each input/output vector
     * @param rng              Random number generator, for repeatability if required
     * @throws IOException If text file cannot be loaded
     */
    public CharIterator(String textFilePath, Charset textFileEncoding, int miniBatchSize, int exampleLength,
                        Random rng) throws IOException {
        if (!new File(textFilePath).exists()) {
            throw new IOException("Could not access file (does not exist): " + textFilePath);
        }
        if (miniBatchSize <= 0) {
            throw new IllegalArgumentException("Invalid miniBatchSize (must be > 0)");
        }
        this.exampleLength = exampleLength;
        this.miniBatchSize = miniBatchSize;
        this.rng = rng;

        //Store valid characters is a map for later use in vectorization
        initCharToIdxMap();

        linesWithDate = readFilesToCharArray(textFilePath, textFileEncoding, "date");
        linesWithoutDate = readFilesToCharArray(textFilePath, textFileEncoding, "without");
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
                if (!charToIdxMap.containsKey(nextChar)) {
                    throw new LabelizerRuntimeException("Cannot find character in charToIdxMap. " +
                        "Character: '" + nextChar + "'.");
                }
            }
        }
        return exampleLines;
    }

    private void initCharToIdxMap() {
        charToIdxMap = new HashMap<>();
        char[] chars = getCharacters();
        for (int i = 0; i < chars.length; i++) {
            charToIdxMap.put(chars[i], i);
        }
        logger.info("All used characters in the charToIdxMap: {}", charToIdxMap);
    }

    /**
     * A minimal character set, with a-z, A-Z, 0-9 and common punctuation etc.
     * As per getMinimalCharacterSet(), but with a few extra characters.
     */
    private static char[] getCharacters() {
        List<Character> validChars = new ArrayList<>();
        addChars(validChars, 'a', 'z');
        addChars(validChars, 'A', 'Z');
        addChars(validChars, '0', '9');

        Character[] temp = {'!', '&', '(', ')', '?', '-', '\'', '"', ',', '.', ':', ';', ' ', '\n', '\t', '\r'};
        validChars.addAll(Arrays.asList(temp));

        Character[] additionalChars = {'@', '#', '$', '%', '^', '*', '{', '}', '[', ']', '/', '+', '_',
            '\\', '|', '<', '>', '='};
        validChars.addAll(Arrays.asList(additionalChars));

        char[] out = new char[validChars.size()];
        int i = 0;
        for (Character c : validChars) out[i++] = c;
        return out;
    }

    private static void addChars(List<Character> validChars, char a, char z) {
        for (char c = a; c <= z; c++) {
            validChars.add(c);
        }
    }

    private char convertIndexToCharacter(int idx) {
        for (Map.Entry<Character, Integer> entry : charToIdxMap.entrySet()) {
            if (idx == entry.getValue()) {
                return entry.getKey();
            }
        }
        throw new LabelizerRuntimeException("Cannot find idx: " + idx);
    }

    public int convertCharacterToIndex(char c) {
        return charToIdxMap.get(c);
    }

    public char getRandomCharacter() {
        return convertIndexToCharacter(rng.nextInt() * charToIdxMap.size());
    }

    public boolean hasNext() {
        return hasMoreExamples();
    }

    private boolean hasMoreExamples() {
        return linesWithDate.size() > linesWithDateOffset ||
            linesWithoutDate.size() > linesWithoutDateOffset;
    }

    public StringDataSet next() {
        try {
            return next(miniBatchSize);
        } catch (Exception e) {
            throw new NoSuchElementException(e.getMessage());
        }
    }

    public StringDataSet next(int miniBatches) {
        if (!hasMoreExamples()) {
            throw new NoSuchElementException();
        }
        List<Pair<String, String>> examples = new ArrayList<>(miniBatches);
        // Create pairs where first row is examples and second row is labels, for example
        // 1) 28.2.2019 11:45:00.123 1234567654 abcde 28.2.2018 11:46:00.124...
        // 2) ddddddddddddddddddddddwwwwwwwwwwwwwwwwwwdddddddddddddddddddddd...
        // Every pair has the same length and they equal with exampleLength
        for (int i = 0; i < miniBatches; i++) {
            StringBuilder left = new StringBuilder(exampleLength);
            StringBuilder right = new StringBuilder(exampleLength);

            while (left.length() < exampleLength) {
                prepareDataForLearning(left, right);
            }
            Pair<String, String> pair = new Pair<>(left.toString(), right.toString());
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
        INDArray input = Nd4j.create(new int[]{currMinibatchSize, charToIdxMap.size(), exampleLength}, 'f');
        INDArray labels = Nd4j.create(new int[]{currMinibatchSize, NUM_LABELS_2, exampleLength}, 'f');

        for (int miniBatchIndex = 0; miniBatchIndex < currMinibatchSize; miniBatchIndex++) {
            for (int charIndex = 0; charIndex < examples.get(miniBatchIndex).getLeft().length(); charIndex++) {
                char exampleChar = examples.get(miniBatchIndex).getLeft().charAt(charIndex);
                char labelChar = examples.get(miniBatchIndex).getRight().charAt(charIndex);
                int exampleCharIndex = charToIdxMap.get(exampleChar);
                int labelCharIndex = DateLabel.findIndex(labelChar);
                input.putScalar(new int[]{miniBatchIndex, exampleCharIndex, charIndex}, 1.0);
                labels.putScalar(new int[]{miniBatchIndex, labelCharIndex, charIndex}, 1.0);
            }
            logger.info("MiniBatch: {}: {}", miniBatchIndex, examples.get(miniBatchIndex).getLeft());
            logger.info("MiniBatch: {}: {}", miniBatchIndex, examples.get(miniBatchIndex).getRight());
        }

        List<String> rawData = new ArrayList<>();
        return new StringDataSet(input, labels, rawData);
    }

    private void prepareDataForLearning(StringBuilder left, StringBuilder right) {
        boolean isDateAppended = isDateAppended(left, right);
        boolean withoutDateAppended = isWithoutDateAppended(left, right);
        if (!isDateAppended && !withoutDateAppended) {
            left.append(ONE_SPACE);
            right.append(DateLabel.W_WITHOUT_DATE.getCharacter());
        }
    }

    private boolean isDateAppended(StringBuilder left, StringBuilder right) {
        boolean isDateAppended = false;
        if (linesWithDate.size() - 1 > linesWithDateOffset) {
            String dExample = linesWithDate.get(linesWithDateOffset++);
            int remaining = exampleLength - left.length();
            if (remaining >= dExample.length() + 1) {
                left.append(dExample).append(ONE_SPACE);
                isDateAppended = true;
                for (int k = 0; k < dExample.length(); k++) {
                    right.append(DateLabel.D_DATE.getCharacter());
                }
                right.append(DateLabel.W_WITHOUT_DATE.getCharacter());
            } else {
                linesWithDateOffset--;
            }
        }
        return isDateAppended;
    }

    private boolean isWithoutDateAppended(StringBuilder left, StringBuilder right) {
        boolean withoutDateAppended = false;
        if (linesWithoutDate.size() - 1 > linesWithoutDateOffset) {
            String wExample = linesWithoutDate.get(linesWithoutDateOffset++);
            int remaining = exampleLength - left.length();
            if (remaining >= wExample.length() + 1) {
                left.append(wExample).append(ONE_SPACE);
                withoutDateAppended = true;
                for (int k = 0; k < wExample.length(); k++) {
                    right.append(DateLabel.W_WITHOUT_DATE.getCharacter());
                }
                right.append(DateLabel.W_WITHOUT_DATE.getCharacter());
            } else {
                linesWithoutDateOffset--;
            }
        }
        return withoutDateAppended;
    }

    public int inputColumns() {
        return charToIdxMap.size();
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
