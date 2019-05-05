package com.credibledoc.combiner.application;

import com.credibledoc.combiner.application.identifier.ApplicationIdentifier;
import com.credibledoc.combiner.application.identifier.ApplicationIdentifierService;
import com.credibledoc.combiner.exception.CombinerRuntimeException;
import com.credibledoc.combiner.log.buffered.LogBufferedReader;
import com.credibledoc.combiner.node.applicationlog.ApplicationLog;
import com.credibledoc.combiner.node.applicationlog.ApplicationLogService;
import com.credibledoc.combiner.node.log.NodeLog;
import com.credibledoc.combiner.node.log.NodeLogService;
import com.credibledoc.combiner.tactic.Tactic;
import com.credibledoc.combiner.tactic.TacticService;

import java.util.List;

/**
 * A service for working with {@link Application}s.
 * @author Kyrylo Semenko
 */
public class ApplicationService {

    /**
     * Singleton.
     */
    private static ApplicationService instance;

    /**
     * @return The {@link ApplicationService} singleton.
     */
    public static ApplicationService getInstance() {
        if (instance == null) {
            instance = new ApplicationService();
        }
        return instance;
    }

    /**
     * Recognize, which {@link Application} the line belongs to.
     * @param line the line from the log file
     * @param logBufferedReader the {@link LogBufferedReader} read the line
     * @return {@link Application} or 'null' if not found
     */
    public Application findApplication(String line, LogBufferedReader logBufferedReader) {
        ApplicationIdentifierService applicationIdentifierService = ApplicationIdentifierService.getInstance();
        for (ApplicationIdentifier applicationIdentifier : applicationIdentifierService.getApplicationIdentifiers()) {
            if (applicationIdentifier.identifyApplication(line, logBufferedReader)) {
                return applicationIdentifier.getApplication();
            }
        }
        return null;
    }

    /**
     * Recognize, which {@link Application} the line belongs to.
     * @param logBufferedReader links to a {@link Application}
     * @return {@link Application} or throw exception
     */
    public Application findApplication(LogBufferedReader logBufferedReader) {
        for (ApplicationLog applicationLog : ApplicationLogService.getInstance().getApplicationLogs()) {
            for (NodeLog nodeLog : NodeLogService.getInstance().findNodeLogs(applicationLog)) {
                if (nodeLog.getLogBufferedReader() == logBufferedReader) {
                    return applicationLog.getApplication();
                }
            }
        }
        throw new CombinerRuntimeException("Application cannot be found. LogBufferedReader: " + logBufferedReader);
    }

    /**
     * Find out {@link ApplicationLog}. Create a new one if it not exists.
     * @param applicationLogs collection of {@link ApplicationLog}s
     * @param application search parameter
     * @return searched or created {@link ApplicationLog}
     */
    public ApplicationLog findOrCreate(List<ApplicationLog> applicationLogs, Application application) {
        for (ApplicationLog applicationLog : applicationLogs) {
            if (application == applicationLog.getApplication()) {
                return applicationLog;
            }
        }
        ApplicationLog applicationLog = new ApplicationLog();
        ApplicationLogService.getInstance().addApplicationLog(applicationLog);
        applicationLog.setApplication(application);
        applicationLogs.add(applicationLog);
        return applicationLog;
    }

    /**
     * Find out {@link NodeLog#getLogBufferedReader()} from report
     * that equals with the first parameter
     *
     * @param logBufferedReader for {@link NodeLog} searching
     * @return A {@link Application#getTactic()} instance from the {@link TacticService}.
     */
    public Tactic findSpecificTactic(LogBufferedReader logBufferedReader) {
        for (ApplicationLog applicationLog : ApplicationLogService.getInstance().getApplicationLogs()) {
            for (NodeLog nodeLog : NodeLogService.getInstance().findNodeLogs(applicationLog)) {
                if (nodeLog.getLogBufferedReader() == logBufferedReader) {
                    return applicationLog.getApplication().getTactic();
                }
            }
        }
        throw new CombinerRuntimeException("Tactic cannot be found");
    }
}
