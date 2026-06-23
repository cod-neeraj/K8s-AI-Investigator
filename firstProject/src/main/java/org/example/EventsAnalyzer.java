package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class EventsAnalyzer {

    private final KubectlExecutor kubectlExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    public EventsAnalyzer(KubectlExecutor kubectlExecutor) {
        this.kubectlExecutor = kubectlExecutor;
    }

    public List<EventSummary> getWarningEvents(String context) throws Exception {
        String json = kubectlExecutor.runCommand(context, "get", "events", "-A", "-o", "json");
        JsonNode root = objectMapper.readTree(json);

        List<EventSummary> warningEvents = new ArrayList<>();

        JsonNode items = root.get("items");
        if (items == null || !items.isArray()) {
            return warningEvents;
        }

        for (JsonNode item : items) {
            String type = item.at("/type").asText();

            if ("Warning".equalsIgnoreCase(type)) {
                String reason = item.at("/reason").asText();
                String message = item.at("/message").asText();

                String involvedObjectName = item.at("/involvedObject/name").asText();
                String namespace = item.at("/involvedObject/namespace").asText();

                warningEvents.add(new EventSummary(involvedObjectName, namespace, reason, message, type));
            }
        }
        return warningEvents;
    }
}
