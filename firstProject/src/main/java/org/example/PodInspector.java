package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PodInspector {

    private final KubectlExecutor kubectlExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    public PodInspector(KubectlExecutor kubectlExecutor) {
        this.kubectlExecutor = kubectlExecutor;
    }

    public List<PodSummary> getPods(String context) throws Exception {
        String json = kubectlExecutor.runCommand(context, "get", "pods", "-A", "-o", "json");
        JsonNode root = objectMapper.readTree(json);

        List<PodSummary> pods = new ArrayList<>();
        for (JsonNode item : root.get("items")) {
            String name = item.at("/metadata/name").asText();
            String namespace = item.at("/metadata/namespace").asText();

            // Extract concrete container-level state status if phase is misleading
            String detailedStatus = item.at("/status/phase").asText();
            boolean isHealthy = "Running".equals(detailedStatus) || "Succeeded".equals(detailedStatus);

            JsonNode containerStatuses = item.at("/status/containerStatuses");
            if (containerStatuses.isArray() && !containerStatuses.isEmpty()) {
                for (JsonNode containerStatus : containerStatuses) {
                    // Check if container is waiting on something (like ImagePullBackOff)
                    JsonNode waitingNode = containerStatus.at("/state/waiting");
                    if (!waitingNode.isMissingNode()) {
                        detailedStatus = waitingNode.get("reason").asText();
                        isHealthy = false;
                    }

                    JsonNode terminatedNode = containerStatus.at("/state/terminated");
                    if (!terminatedNode.isMissingNode() && terminatedNode.get("exitCode").asInt() != 0) {
                        detailedStatus = terminatedNode.get("reason").asText();
                        isHealthy = false;
                    }
                }
            }
            pods.add(new PodSummary(name, namespace, detailedStatus, isHealthy));
        }
        return pods;
    }
}
