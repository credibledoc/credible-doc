package com.credibledoc.log.labelizer.classifier;


import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;
import com.credibledoc.log.labelizer.iterator.LineIterator;
import com.credibledoc.log.labelizer.tokenizer.CharTokenizer;
import com.credibledoc.log.labelizer.tokenizer.CharTokenizerFactory;
import org.apache.commons.io.FileUtils;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.sentenceiterator.AggregatingSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.io.ClassPathResource;
import org.nd4j.linalg.primitives.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final double LEARNING_RATE = 0.0025;
    private static final double MIN_LEARNING_RATE = 0.0001;
    private static final int WINDOW_SIZE_5 = 5;
    private ParagraphVectors paragraphVectors;
    private LabelAwareIterator iterator;
    private TokenizerFactory tokenizerFactory;

    private static final Logger log = LoggerFactory.getLogger(ParagraphVectorsClassifierExample.class);

    public static void main(String[] args) throws Exception {

        ParagraphVectorsClassifierExample app = new ParagraphVectorsClassifierExample();
        app.makeParagraphVectors();
        app.checkUnlabeledData();
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

            tokenizerFactory = new CharTokenizerFactory();
            
            log.info("learningRate: {}", LEARNING_RATE);
            log.info("minLearningRate: {}", MIN_LEARNING_RATE);
            log.info("windowSize: {}", WINDOW_SIZE_5);

            // ParagraphVectors training configuration
            if (paragraphVectors == null) {
                paragraphVectors = new ParagraphVectors.Builder()
                    .learningRate(LEARNING_RATE)
                    .minLearningRate(MIN_LEARNING_RATE)
                    .batchSize(1000)
                    .epochs(20)
                    .iterate(iterator)
                    .trainWordVectors(true)
                    .tokenizerFactory(tokenizerFactory)
                    .windowSize(WINDOW_SIZE_5)
                    .useHierarchicSoftmax(true)
                    .build();
            } else {
                // Train again the same ParagraphVectors
                paragraphVectors = new ParagraphVectors.Builder()
                    .learningRate(LEARNING_RATE)
                    .minLearningRate(MIN_LEARNING_RATE)
                    .batchSize(1000)
                    .epochs(0)
                    .iterate(iterator)
                    .trainWordVectors(false)
                    .tokenizerFactory(tokenizerFactory)
                    .useExistingWordVectors(paragraphVectors)
                    .windowSize(WINDOW_SIZE_5)
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
        ClassPathResource finance = new ClassPathResource("vectors/unlabeled/date/test.txt");

        List<Pair<String, Double>> scoresFinance = calculateScore(finance);
        log.info("Document finance falls into the following categories: ");
        for (Pair<String, Double> score: scoresFinance) {
            log.info("d        {}: {}", score.getFirst(), score.getSecond());
        }
        
    }

    private List<Pair<String, Double>> calculateScore(ClassPathResource classPathResource) throws IOException {
        SentenceIterator unClassifiedIterator = new AggregatingSentenceIterator.Builder()
            .addSentenceIterator(new BasicLineIterator(classPathResource.getFile()))
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

        List<Pair<String, Double>> result = null;
        while (unClassifiedIterator.hasNext()) {
            String sentence = unClassifiedIterator.nextSentence();
            List<List<Pair<String, Double>>> scoreList = new ArrayList<>(sentence.length());
            CharTokenizer charTokenizer = new CharTokenizer(sentence);
            while (charTokenizer.hasMoreTokens()) {
                int listIndex = charTokenizer.getListIndex();
                String token = charTokenizer.nextToken();
                LabelledDocument document = new LabelledDocument();
                document.setContent(token);
                INDArray documentAsCentroid = meansBuilder.documentAsVector(document);
                List<Pair<String, Double>> scores;
                if (documentAsCentroid != null) {
                    scores = seeker.getScores(documentAsCentroid);
                } else {
                    if (scoreList.isEmpty()) {
                        continue;
                    }
                    scores = new ArrayList<>();
                    List<Pair<String, Double>> lastPair = scoreList.get(scoreList.size() - 1);
                    scores.add(new Pair<>(lastPair.get(0).getKey(), lastPair.get(0).getValue()));
                    scores.add(new Pair<>(lastPair.get(1).getKey(), lastPair.get(0).getValue()));
                }
 
                calculateScore(scoreList, listIndex, token, scores);

                if (result == null) {
                    result = scores;
                }
                for (int i = 0; i < scores.size(); i++) {
                    result.get(i).setSecond((scores.get(i).getSecond() + result.get(i).getSecond()) / 2);
                }
            }
            printScoreOfSentence(sentence, scoreList);
        }
        return result;
    }

    private void calculateScore(List<List<Pair<String, Double>>> scoreList, int listIndex, String token,
                                List<Pair<String, Double>> scores) {
        for (int i = 0; i < token.length(); i++) {
            if (scoreList.size() > listIndex + i) {
                List<Pair<String, Double>> nextScores = scoreList.get(listIndex + i);
                for (int k = 0; k < scores.size(); k++) {
                    nextScores.get(k).setSecond((scores.get(k).getSecond() + nextScores.get(k).getSecond()) / 2);
                }
            } else {
                scoreList.add(scores);
            }
        }
    }

    private void printScoreOfSentence(String sentence, List<List<Pair<String, Double>>> scoreList) {
        StringBuilder stringBuilder = new StringBuilder(sentence.length() + 10);
        Map<String, Integer> map = new HashMap<>();
        for (List<Pair<String, Double>> scores : scoreList) {
            double maxScore = scores.get(0).getSecond();
            String label = scores.get(0).getFirst();
            for (int i = 1; i < scores.size(); i++) {
                Pair<String, Double> score = scores.get(i);
                if (score.getSecond().compareTo(maxScore) > 0) {
                    maxScore = score.getSecond();
                    label = score.getFirst();
                }
            }
            String flag = label.substring(0, 1);
            stringBuilder.append(flag);
            if (map.containsKey(flag)) {
                map.put(flag, map.get(flag) + 1);
            } else {
                map.put(flag, 1);
            }
        }
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            stringBuilder.append(" ").append(entry.getKey()).append(":").append(entry.getValue());
        }
        if (log.isInfoEnabled()) {
            log.info("Scores: {}", stringBuilder);
            log.info("String: {}", sentence);
            log.info("Next line");
        }
    }
}

