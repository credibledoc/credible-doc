package com.credibledoc.log.labelizer.tokenizer;

import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;
import org.deeplearning4j.text.tokenization.tokenizer.TokenPreProcess;
import org.deeplearning4j.text.tokenization.tokenizer.Tokenizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This tokenizer returns single characters from the {@link #nextToken()} method.
 * 
 * @author Kyrylo Semenko
 */
public class CharTokenizer implements Tokenizer {
    private static final int MAX_TOKEN_LENGTH_1 = 1;
    private List<String> tokenList = Collections.synchronizedList(new ArrayList<String>());
    
    private AtomicInteger lastIndex = new AtomicInteger(0);
    
    private AtomicInteger listIndex = new AtomicInteger(0);

    public CharTokenizer(String tokens) {
        tokenList.addAll(Arrays.asList(tokens.replaceAll("\\s", "|").split("")));
    }

    @Override
    public boolean hasMoreTokens() {
        return !tokenList.isEmpty();
    }

    @Override
    public int countTokens() {
        return tokenList.size();
    }

    /**
     * Get a sentence, for example <b>To|be|or|not|to|be</b> and return its parts (tokens), for example
     * <pre>
     *     T
     *     To
     *     To|
     *     To|b
     *     To|be
     *     To|be|
     *     o
     *     o|
     *     o|b
     *     o|be
     *     o|be|
     *     o|be|o
     *     ...
     *     |be|or
     *     be|or|
     *     e|or|n
     *     |or|no
     *     or|not
     *     r|not|
     *     |not|t
     *     not|to
     *     ot|to|
     *     t|to|b
     *     |to|be
     *     to|be
     *     o|be
     *     |be
     *     be
     *     e
     * </pre>
     * Every token contains max {@link #MAX_TOKEN_LENGTH_1} characters and all spaces replaced with pipe (|).
     */
    @Override
    public String nextToken() {
        StringBuilder stringBuilder = new StringBuilder(MAX_TOKEN_LENGTH_1);
        int index = 0;
        while (index <= lastIndex.get() && tokenList.size() > index) {
            stringBuilder.append(tokenList.get(index));
            index++;
        }
        if (lastIndex.get() == MAX_TOKEN_LENGTH_1 - 1) {
            tokenList.remove(0);
            listIndex.incrementAndGet();
            lastIndex.set(0);
        } else {
            lastIndex.incrementAndGet();
        }
        return stringBuilder.toString();
    }

    @Override
    public List<String> getTokens() {
        List<String> tokens = new ArrayList<>();
        while (hasMoreTokens()) {
            tokens.add(nextToken());
        }
        return tokens;
    }

    /**
     * Not used.
     */
    @Override
    public void setTokenPreProcessor(TokenPreProcess tokenPreProcessor) {
        throw new LabelizerRuntimeException("Not implemented");
    }

    /**
     * @return The {@link #listIndex} field value.
     */
    public int getListIndex() {
        return listIndex.get();
    }
}
