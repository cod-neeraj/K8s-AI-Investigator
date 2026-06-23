package org.example;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class LogsCollector {

    private final KubectlExecutor kubectlExecutor;

    @Inject
    public LogsCollector(KubectlExecutor kubectlExecutor) {
        this.kubectlExecutor = kubectlExecutor;
    }

    public String fetchPodLogs(String context, String namespace, String podName) {
        try {
            return kubectlExecutor.runCommand(context, "logs", podName, "-n", namespace, "--tail=50");
        } catch (Exception e) {
            try {
                return kubectlExecutor.runCommand(context, "logs", podName, "-n", namespace, "--previous", "--tail=30");
            } catch (Exception fallbackEx) {
                return "Could not retrieve container logs: " + e.getMessage();
            }
        }
    }
}
