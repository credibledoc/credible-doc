package com.credibledoc.combiner.file;

import com.credibledoc.combiner.application.Application;
import com.credibledoc.combiner.application.ApplicationService;
import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.log.buffered.LogFileReader;

import java.io.File;
import java.util.Date;

public class FileService {

    /**
     * Singleton.
     */
    private static FileService instance;

    /**
     * @return The {@link FileService} singleton.
     */
    public static FileService getInstance() {
        if (instance == null) {
            instance = new FileService();
        }
        return instance;
    }

    /**
     * Recognize, which {@link Application} this file belongs to.
     * @param file the log file
     * @return {@link Application} or throw the new {@link CombinerRuntimeException} if the file not recognized
     */
    public Application findApplication(File file) {
        ApplicationService applicationService = ApplicationService.getInstance();
        try (LogBufferedReader logBufferedReader = new LogBufferedReader(new LogFileReader(file))) {
            String line = logBufferedReader.readLine();
            while (line != null) {
                Application application = applicationService.findApplication(line, logBufferedReader);
                if (application != null) {
                    return application;
                }
                line = logBufferedReader.readLine();
            }
            throw new CombinerRuntimeException("Cannot recognize application type of the file: " + file.getAbsolutePath());
        } catch (Exception e) {
            throw new CombinerRuntimeException(e);
        }
    }

    /**
     * Find out date and time of the first line in a file.
     *
     * @param file        an application log
     * @param application each {@link Application} has its own strategy of date searching
     * @return the most recent date and time
     */
    public Date findDate(File file, Application application) {
        return application.getTactic().findDate(file);
    }

}

