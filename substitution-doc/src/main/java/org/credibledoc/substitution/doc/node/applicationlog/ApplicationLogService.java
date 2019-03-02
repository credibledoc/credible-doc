package org.credibledoc.substitution.doc.node.applicationlog;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
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
     * Call the {@link ApplicationLogRepository#getApplicationLogs()} method
     * @return all {@link ApplicationLog}s.
     */
    public List<ApplicationLog> getApplicationLogs() {
        return applicationLogRepository.getApplicationLogs();
    }

    public void addApplicationLog(ApplicationLog applicationLog) {
        applicationLogRepository.getApplicationLogs().add(applicationLog);
    }

}
