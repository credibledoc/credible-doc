package com.credibledoc.substitution.doc.filesmerger.node.applicationlog;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import com.credibledoc.substitution.doc.filesmerger.node.file.NodeFile;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This service contains methods for working with {@link ApplicationLog}s.
 *
 * @author Kyrylo Semenko
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ApplicationLogService {

    @NonNull
    private final ApplicationLogRepository applicationLogRepository;

    /**
     * Collect all {@link ApplicationLog}s of {@link NodeFile}s
     * from argument.
     * @return all {@link ApplicationLog}s which belongs to the
     * {@link NodeFile}s.
     */
    public List<ApplicationLog> getApplicationLogs(Collection<NodeFile> nodeFiles) {
        List<ApplicationLog> result = new ArrayList<>();
        for (NodeFile nodeFile : nodeFiles) {
            ApplicationLog applicationLog = nodeFile.getNodeLog().getApplicationLog();
            if (!result.contains(applicationLog)) {
                result.add(applicationLog);
            }
        }
        return result;
    }

    /**
     * Add the {@link ApplicationLog} to the {@link ApplicationLogRepository}
     * @param applicationLog for addition
     */
    public void addApplicationLog(ApplicationLog applicationLog) {
        applicationLogRepository.getApplicationLogs().add(applicationLog);
    }

    /**
     * Call the {@link ApplicationLogRepository#getApplicationLogs()} method
     * @return All items from the {@link ApplicationLogRepository}
     */
    public List<ApplicationLog> getApplicationLogs() {
        return applicationLogRepository.getApplicationLogs();
    }
}
