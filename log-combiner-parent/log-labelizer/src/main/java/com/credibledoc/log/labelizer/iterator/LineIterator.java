package com.credibledoc.log.labelizer.iterator;

import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;
import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.documentiterator.LabelsSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is simple filesystem-based LabelAware iterator.
 * It assumes that you have one or more folders organized in the following way:
 * 1st level subfolder: label name
 * 2nd level: bunch of documents for that label
 *
 * You can have as many label folders as you want, as well.
 *
 * @author Kyrylo Semenko
 */
public class LineIterator implements LabelAwareIterator {
    private static final Logger logger = LoggerFactory.getLogger(LineIterator.class);
    private List<File> files;

    /**
     * Current file index.
     */
    private AtomicInteger filePosition = new AtomicInteger(0);

    /**
     * Current line in a file with {@link #filePosition}.
     */
    private final List<String> lines = Collections.synchronizedList(new ArrayList<String>());

    /**
     * Current file.
     */
    private File fileToRead;
    
    private LabelsSource labelsSource;

    private LineIterator(List<File> files, LabelsSource source) {
        this.files = files;
        this.labelsSource = source;
    }

    @Override
    public boolean hasNextDocument() {
        return !lines.isEmpty() || filePosition.get() < files.size();
    }

    @Override
    public LabelledDocument nextDocument() {
        synchronized (lines) {
            try {
                if (lines.isEmpty()) {
                    fileToRead = files.get(filePosition.getAndIncrement());
                    String line;
                    try (BufferedReader reader = new BufferedReader(new FileReader(fileToRead))) {
                        while ((line = reader.readLine()) != null) {
                            lines.add(line);
                        }
                    }
                }
            } catch (Exception e) {
                throw new LabelizerRuntimeException(e);
            }
            LabelledDocument document = new LabelledDocument();
            String label = fileToRead.getParentFile().getName();
            document.setContent(lines.remove(0));
            document.addLabel(label);
            return document;
        }
    }

    @Override
    public boolean hasNext() {
        return hasNextDocument();
    }

    @Override
    public LabelledDocument next() {
        try {
            return nextDocument();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new NoSuchElementException(e.getMessage());
        }
    }

    @Override
    public void remove() {
        // no-op
    }

    @Override
    public void shutdown() {
        // no-op
    }

    @Override
    public void reset() {
        filePosition.set(0);
        lines.clear();
    }

    @Override
    public LabelsSource getLabelsSource() {
        return labelsSource;
    }

    public static class Builder {
        List<File> foldersToScan = new ArrayList<>();

        /**
         * Root folder for labels -> documents.
         * Each subfolder name will be presented as label, and contents of this folder will be represented as LabelledDocument, with label attached
         *
         * @param folder folder to be scanned for labels and files
         */
        public Builder addSourceFolder(File folder) {
            foldersToScan.add(folder);
            return this;
        }

        public LineIterator build() {
            // search for all files in all folders provided
            List<File> fileList = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            for (File file : foldersToScan) {
                if (file.isDirectory()) {
                    File[] files = file.listFiles();
                    if (files == null || files.length == 0) {
                        continue;
                    }

                    for (File fileLabel : files) {
                        appendLabelOrFile(fileList, labels, fileLabel);
                    }
                }
            }
            LabelsSource source = new LabelsSource(labels);
            return new LineIterator(fileList, source);
        }

        private void appendLabelOrFile(List<File> fileList, List<String> labels, File fileLabel) {
            if (fileLabel.isDirectory()) {
                if (!labels.contains(fileLabel.getName())) {
                    labels.add(fileLabel.getName());
                }

                File[] docs = fileLabel.listFiles();
                if (docs != null && docs.length > 0) {
                    for (File fileDoc : docs) {
                        if (!fileDoc.isDirectory()) {
                            fileList.add(fileDoc);
                        }
                    }
                }
            }
        }
    }
}

