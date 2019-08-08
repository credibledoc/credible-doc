package com.credibledoc.log.labelizer.tokenizer;

import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;
import org.deeplearning4j.text.tokenization.tokenizer.TokenPreProcess;
import org.deeplearning4j.text.tokenization.tokenizer.Tokenizer;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;

import java.io.InputStream;

/**
 * Default tokenizer factory for {@link CharTokenizer}.
 * 
 * @author Kyrylo Semenko
 */
public class CharTokenizerFactory implements TokenizerFactory {

    @Override
    public Tokenizer create(String toTokenize) {
        return new CharTokenizer(toTokenize);
    }

    /**
     * Not used.
     */
    @Override
    public Tokenizer create(InputStream toTokenize) {
        throw new LabelizerRuntimeException("Not implemented");
    }

    /**
     * Not used.
     */
    @Override
    public void setTokenPreProcessor(TokenPreProcess preProcessor) {
        throw new LabelizerRuntimeException("Not implemented");
    }

    /**
     * @return 'null'
     */
    @Override
    public TokenPreProcess getTokenPreProcessor() {
        return null;
    }

}

