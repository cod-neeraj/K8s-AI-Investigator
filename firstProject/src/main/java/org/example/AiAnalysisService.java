package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AiAnalysisService {

    @ConfigProperty(name = "openrouter.api.key")
    String apiKey;

    @ConfigProperty(name = "openrouter.model")
    String model;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiSummaryResult analyze(InvestigationResult investigation) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "OPENROUTER_API_KEY is not set. Get a free key at openrouter.ai and set it " +
                "as an environment variable before starting the app.");
        }

        String prompt = buildPrompt(investigation);
        String rawModelText = callOpenRouter(prompt);
        return parseAiResponse(rawModelText);
    }

    private String buildPrompt(InvestigationResult investigation) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a senior Kubernetes SRE and troubleshooting expert. Analyze the cluster data below ")
          .append("and identify root causes of real problems. Do NOT invent issues that aren't supported by data.\n\n");

        // --- Nodes ---
        sb.append("NODES:\n");
        if (investigation.getNodes() == null || investigation.getNodes().isEmpty()) {
            sb.append("(no node data)\n");
        } else {
            for (NodeSummary node : investigation.getNodes()) {
                sb.append("- ").append(node.getName())
                  .append(" ready=").append(node.isReady())
                  .append(" kubelet=").append(node.getKubeletVersion());
                for (NodeSummary.NodeCondition c : node.getConditions()) {
                    // Flag any pressure conditions that are True (bad)
                    if (!c.getType().equals("Ready") && "True".equals(c.getStatus())) {
                        sb.append(" [").append(c.getType()).append("!]");
                    }
                }
                sb.append("\n");
            }
        }

        // --- Deployments ---
        sb.append("\nDEPLOYMENTS:\n");
        boolean anyUnhealthyDeploy = false;
        if (investigation.getDeployments() != null) {
            for (DeploymentSummary dep : investigation.getDeployments()) {
                if (!dep.isHealthy()) {
                    anyUnhealthyDeploy = true;
                    sb.append("- ").append(dep.getNamespace()).append("/").append(dep.getName())
                      .append(" desired=").append(dep.getDesiredReplicas())
                      .append(" ready=").append(dep.getReadyReplicas())
                      .append(" available=").append(dep.getAvailableReplicas())
                      .append(" status=").append(dep.getStatusMessage()).append("\n");
                }
            }
        }
        if (!anyUnhealthyDeploy) sb.append("(all deployments healthy)\n");

        // --- Unhealthy pods ---
        sb.append("\nUNHEALTHY PODS:\n");
        boolean anyUnhealthy = false;
        if (investigation.getPods() != null) {
            for (PodSummary pod : investigation.getPods()) {
                if (!pod.isHealthy()) {
                    anyUnhealthy = true;
                    sb.append("- ").append(pod.getNamespace()).append("/").append(pod.getName())
                      .append(" status=").append(pod.getStatus()).append("\n");
                }
            }
        }
        if (!anyUnhealthy) sb.append("(none — all pods healthy)\n");

        // --- Warning events ---
        sb.append("\nWARNING EVENTS:\n");
        if (investigation.getEvents() == null || investigation.getEvents().isEmpty()) {
            sb.append("(none)\n");
        } else {
            for (EventSummary ev : investigation.getEvents()) {
                sb.append("- ").append(ev.getNamespace()).append("/").append(ev.getName())
                  .append(" reason=").append(ev.getReason())
                  .append(" message=").append(truncate(ev.getMessage(), 200)).append("\n");
            }
        }

        // --- Logs from unhealthy pods ---
        sb.append("\nLOGS FROM UNHEALTHY PODS:\n");
        if (investigation.getLogs() == null || investigation.getLogs().isEmpty()) {
            sb.append("(none)\n");
        } else {
            for (Map.Entry<String, String> entry : investigation.getLogs().entrySet()) {
                sb.append("--- ").append(entry.getKey()).append(" ---\n");
                sb.append(truncate(entry.getValue(), 1500)).append("\n");
            }
        }

        sb.append("\nRespond with ONLY raw JSON, no markdown fences, no extra commentary, ")
          .append("matching exactly this shape:\n")
          .append("{\n")
          .append("  \"overallHealth\": \"healthy\" or \"degraded\",\n")
          .append("  \"issues\": [\n")
          .append("    {\n")
          .append("      \"affectedResource\": \"namespace/name\",\n")
          .append("      \"rootCause\": \"concise explanation of what is wrong and why\",\n")
          .append("      \"confidence\": 0-100,\n")
          .append("      \"severity\": \"low\" or \"medium\" or \"high\",\n")
          .append("      \"suggestedFix\": \"concrete kubectl or config action to resolve this\"\n")
          .append("    }\n")
          .append("  ]\n")
          .append("}\n")
          .append("If everything is healthy, return overallHealth: \"healthy\" and an empty issues array.");

        return sb.toString();
    }

    private String truncate(String text, int maxChars) {
        if (text == null) return "";
        return text.length() <= maxChars ? text : text.substring(0, maxChars) + "... [truncated]";
    }

    private String callOpenRouter(String prompt) throws Exception {
        Map<String, Object> requestBodyMap = Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );
        String requestBody = objectMapper.writeValueAsString(requestBodyMap);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("OpenRouter call failed (HTTP " + response.statusCode() + "): " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode contentNode = root.at("/choices/0/message/content");
        if (contentNode.isMissingNode()) {
            throw new RuntimeException("Unexpected OpenRouter response shape: " + response.body());
        }
        return contentNode.asText();
    }

    private AiSummaryResult parseAiResponse(String rawModelText) throws Exception {
        String cleanedJson = extractJson(rawModelText);
        return objectMapper.readValue(cleanedJson, AiSummaryResult.class);
    }

    private String extractJson(String text) {
        String trimmed = text.trim()
                .replaceAll("(?s)^```json", "")
                .replaceAll("(?s)^```", "")
                .replaceAll("(?s)```$", "")
                .trim();

        int start = trimmed.indexOf('{');
        int end   = trimmed.lastIndexOf('}');
        if (start == -1 || end == -1 || end < start) {
            throw new RuntimeException("Could not find JSON object in AI response: " + text);
        }
        return trimmed.substring(start, end + 1);
    }
}
