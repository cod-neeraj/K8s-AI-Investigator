package org.example;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@ApplicationScoped
public class ClusterService {

    private final KubectlExecutor kubectlExecutor;

    private final ExecutorService healthCheckPool = Executors.newFixedThreadPool(8);

    @Inject
    public ClusterService(KubectlExecutor kubectlExecutor) {
        this.kubectlExecutor = kubectlExecutor;
    }

    public List<String> listContexts() throws Exception {
        List<String> allContexts = listAllContextNames();

        List<CompletableFuture<String>> checks = allContexts.stream()
                .map(ctx -> CompletableFuture.supplyAsync(() -> isReachable(ctx) ? ctx : null, healthCheckPool))
                .collect(Collectors.toList());

        CompletableFuture.allOf(checks.toArray(new CompletableFuture[0])).join();

        return checks.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<String> listAllContextNames() throws Exception {
        String output = kubectlExecutor.runGlobalCommand("config", "get-contexts", "-o", "name");

        List<String> contexts = new ArrayList<>();
        for (String line : output.split("\\r?\\n")) {
            if (!line.isBlank()) {
                contexts.add(line.trim());
            }
        }
        return contexts;
    }

    private boolean isReachable(String context) {
        try {
            kubectlExecutor.runCommand(context, 4, "get", "--raw", "/healthz", "--request-timeout=3s");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @PreDestroy
    void shutdown() {
        healthCheckPool.shutdownNow();
    }
}
