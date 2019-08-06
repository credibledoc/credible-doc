package com.credibledoc.log.labelizer.classifier;


import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;
import com.credibledoc.log.labelizer.iterator.LineIterator;
import org.apache.commons.io.FileUtils;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.sentenceiterator.AggregatingSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.FileSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.io.ClassPathResource;
import org.nd4j.linalg.primitives.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * This is basic example for documents classification done with DL4j ParagraphVectors.
 * The overall idea is to use ParagraphVectors in the same way we use LDA:
 * topic space modelling.
 *
 * In this example we assume we have few labeled categories that we can use
 * for training, and few unlabeled documents. And our goal is to determine,
 * which category these unlabeled documents fall into
 *
 *
 * Please note: This example could be improved by using learning cascade
 * for higher accuracy, but that's beyond basic example paradigm.
 *
 * @author raver119@gmail.com
 */
public class ParagraphVectorsClassifierExample {

    private static final String VECTORS_PARAGRAPH_VECTORS_TXT = "vectors/serialized.wordVectors";
    private ParagraphVectors paragraphVectors;
    private LabelAwareIterator iterator;
    private TokenizerFactory tokenizerFactory;

    private static final Logger log = LoggerFactory.getLogger(ParagraphVectorsClassifierExample.class);

    public static void main(String[] args) throws Exception {

        ParagraphVectorsClassifierExample app = new ParagraphVectorsClassifierExample();
        app.makeParagraphVectors();
        app.checkUnlabeledData();
        /*
                Your output should be like this:

                Document content: Treatment is recommended as soon as the diagnosis is made.
                Document falls into the following categories: 
                        finance: -0.17584866285324097
                        health: 0.4141475260257721
                        science: 0.026913458481431007
                        
                Document content: Initially created as a non-profit organisation, it was transformed into a joint-stock company.
                Document falls into the following categories: 
                        finance: 0.28525084257125854
                        health: -0.10743410140275955
                        science: 0.1450825184583664
         */
    }

    private void makeParagraphVectors() {
        try {
            File paragraphVectorsFile = new File(this.getClass().getResource("/")
                .getPath()
                .replace("target/classes", "src/main/resources/") + VECTORS_PARAGRAPH_VECTORS_TXT);
            if (paragraphVectorsFile.exists()) {
                paragraphVectors = WordVectorSerializer.readParagraphVectors(paragraphVectorsFile);
            }

            ClassPathResource resource = new ClassPathResource("vectors/labeled");

            // build a iterator for our dataset
            iterator = new LineIterator.Builder()
                .addSourceFolder(resource.getFile())
                .build();

            tokenizerFactory = new DefaultTokenizerFactory();
            tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());

            // ParagraphVectors training configuration
            if (paragraphVectors == null) {
                paragraphVectors = new ParagraphVectors.Builder()
                    .learningRate(0.025)
                    .minLearningRate(0.001)
                    .batchSize(1000)
                    .epochs(20)
                    .iterate(iterator)
                    .trainWordVectors(true)
                    .tokenizerFactory(tokenizerFactory)
                    .build();
            } else {
                // Train again the same ParagraphVectors
                paragraphVectors = new ParagraphVectors.Builder()
                    .learningRate(0.0025)
                    .minLearningRate(0.0001)
                    .batchSize(1000)
                    .epochs(1)
                    .iterate(iterator)
                    .trainWordVectors(true)
                    .tokenizerFactory(tokenizerFactory)
                    .useExistingWordVectors(paragraphVectors)
                    .build();
            }

            // Start model training
            paragraphVectors.fit();

            FileUtils.forceMkdirParent(paragraphVectorsFile);
            WordVectorSerializer.writeParagraphVectors(paragraphVectors, paragraphVectorsFile);
        } catch (Exception e) {
            throw new LabelizerRuntimeException(e);
        }
    }

    private void checkUnlabeledData() throws IOException {
      /*
      At this point we assume that we have model built and we can check
      which categories our unlabeled document falls into.
      So we'll start loading our unlabeled documents and checking them
     */
        ClassPathResource finance = new ClassPathResource("vectors/unlabeled/finance/f01.txt");
        ClassPathResource health = new ClassPathResource("vectors/unlabeled/health/f01.txt");
        
        SentenceIterator unClassifiedIterator = new AggregatingSentenceIterator.Builder()
            .addSentenceIterator(new BasicLineIterator(finance.getFile()))
            .addSentenceIterator(new FileSentenceIterator(health.getFile()))
            .build();

     /*
      Now we'll iterate over unlabeled data, and check which label it could be assigned to
      Please note: for many domains it's normal to have 1 document fall into few labels at once,
      with different "weight" for each.
     */
        MeansBuilder meansBuilder = new MeansBuilder(
            (InMemoryLookupTable<VocabWord>) paragraphVectors.getLookupTable(),
            tokenizerFactory);

        LabelSeeker seeker = new LabelSeeker(iterator.getLabelsSource().getLabels(),
            (InMemoryLookupTable<VocabWord>) paragraphVectors.getLookupTable());

        while (unClassifiedIterator.hasNext()) {
            String sentence = unClassifiedIterator.nextSentence();
            LabelledDocument document = new LabelledDocument();
            document.setContent(sentence);
            INDArray documentAsCentroid = meansBuilder.documentAsVector(document);
            List<Pair<String, Double>> scores = seeker.getScores(documentAsCentroid);

             /*
              please note, document.getLabel() is used just to show which document we're looking at now,
              as a substitute for printing out the whole document name.
              So, labels on these two documents are used like titles,
              just to visualize our classification done properly
             */
            log.info("Document content: {}", document.getContent());
            log.info("Document falls into the following categories: ");
            
            for (Pair<String, Double> score: scores) {
                log.info("        {}: {}", score.getFirst(), score.getSecond());
            }
        }
    }
}

