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

// TODO Kyrylo Semenko - zrusit
/**
 * A service for working with {@link Tactic}s.
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
     * Recognize, which {@link Tactic} the line belongs to.
     * @param line the line from the log file
     * @param logBufferedReader the {@link LogBufferedReader} read the line
     * @return {@link Tactic} or 'null' if not found
     */
    public Tactic findTactic(String line, LogBufferedReader logBufferedReader) {
        ApplicationIdentifierService applicationIdentifierService = ApplicationIdentifierService.getInstance();
        for (ApplicationIdentifier applicationIdentifier : applicationIdentifierService.getApplicationIdentifiers()) {
            if (applicationIdentifier.identifyApplication(line, logBufferedReader)) {
                return applicationIdentifier.getTactic();
            }
        }
        return null;
    }

    /**
     * Recognize, which {@link Tactic} the line belongs to.
     * @param logBufferedReader links to a {@link Tactic}
     * @return {@link Tactic} or throw exception
     */
    public Tactic findTactic(LogBufferedReader logBufferedReader) {
        for (ApplicationLog applicationLog : ApplicationLogService.getInstance().getApplicationLogs()) {
            for (NodeLog nodeLog : NodeLogService.getInstance().findNodeLogs(applicationLog)) {
                if (nodeLog.getLogBufferedReader() == logBufferedReader) {
                    return applicationLog.getTactic();
                }
            }
        }
        throw new CombinerRuntimeException("Tactic cannot be found. LogBufferedReader: " + logBufferedReader);
    }

    /**
     * Find out {@link ApplicationLog}. Create a new one if it not exists.
     * @param applicationLogs collection of {@link ApplicationLog}s
     * @param tactic search parameter
     * @return searched or created {@link ApplicationLog}
     */
    public ApplicationLog findOrCreate(List<ApplicationLog> applicationLogs, Tactic tactic) {
        for (ApplicationLog applicationLog : applicationLogs) {
            if (tactic == applicationLog.getTactic()) {
                return applicationLog;
            }
        }
        ApplicationLog applicationLog = new ApplicationLog();
        ApplicationLogService.getInstance().addApplicationLog(applicationLog);
        applicationLog.setTactic(tactic);
        applicationLogs.add(applicationLog);
        return applicationLog;
    }

    /**
     * Find out {@link NodeLog#getLogBufferedReader()} from report
     * that equals with the first parameter
     *
     * @param logBufferedReader for {@link NodeLog} searching
     * @return A {@link Tactic} instance from the {@link TacticService}.
     */
    public Tactic findSpecificTactic(LogBufferedReader logBufferedReader) {
        for (ApplicationLog applicationLog : ApplicationLogService.getInstance().getApplicationLogs()) {
            for (NodeLog nodeLog : NodeLogService.getInstance().findNodeLogs(applicationLog)) {
                if (nodeLog.getLogBufferedReader() == logBufferedReader) {
                    return applicationLog.getTactic();
                }
            }
        }
        throw new CombinerRuntimeException("Tactic cannot be found");
    }
}
