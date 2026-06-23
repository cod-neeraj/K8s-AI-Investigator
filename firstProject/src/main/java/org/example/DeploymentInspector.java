package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks the rollout status of every Deployment in the cluster.
 * A deployment is healthy when desiredReplicas == readyReplicas == availableReplicas.
 */
@ApplicationScoped
public class DeploymentInspector {

    private final KubectlExecutor kubectlExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    public DeploymentInspector(KubectlExecutor kubectlExecutor) {
        this.kubectlExecutor = kubectlExecutor;
    }

    public List<DeploymentSummary> getDeployments(String context) throws Exception {
        String json = kubectlExecutor.runCommand(context, "get", "deployments", "-A", "-o", "json");
        JsonNode root = objectMapper.readTree(json);

        List<DeploymentSummary> deployments = new ArrayList<>();
        JsonNode items = root.get("items");
        if (items == null || !items.isArray()) return deployments;

        for (JsonNode item : items) {
            String name      = item.at("/metadata/name").asText();
            String namespace = item.at("/metadata/namespace").asText();

            int desired   = item.at("/spec/replicas").asInt(0);
            int ready     = item.at("/status/readyReplicas").asInt(0);
            int available = item.at("/status/availableReplicas").asInt(0);
            int updated   = item.at("/status/updatedReplicas").asInt(0);

            boolean healthy = (desired > 0) && (ready == desired) && (available == desired);

            String statusMsg = healthy ? "Available" : buildStatusMessage(item, desired, ready, available);

            deployments.add(new DeploymentSummary(name, namespace, desired, ready, available, updated, healthy, statusMsg));
        }
        return deployments;
    }

    private String buildStatusMessage(JsonNode item, int desired, int ready, int available) {

        JsonNode conditions = item.at("/status/conditions");
        if (conditions.isArray()) {
            for (JsonNode cond : conditions) {
                String type   = cond.at("/type").asText();
                String status = cond.at("/status").asText();
                if ("Available".equals(type) && "False".equals(status)) {
                    return cond.at("/message").asText("Not available");
                }
                if ("Progressing".equals(type) && "False".equals(status)) {
                    return cond.at("/message").asText("Rollout stalled");
                }
            }
        }
        return String.format("ready %d/%d, available %d/%d", ready, desired, available, desired);
    }
}
