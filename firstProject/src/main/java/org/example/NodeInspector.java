package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

/**
 * Fetches node-level health from the cluster.
 * Checks each node's Ready condition and any pressure conditions
 * (MemoryPressure, DiskPressure, PIDPressure) that signal node stress.
 */
@ApplicationScoped
public class NodeInspector {

    private final KubectlExecutor kubectlExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    public NodeInspector(KubectlExecutor kubectlExecutor) {
        this.kubectlExecutor = kubectlExecutor;
    }

    public List<NodeSummary> getNodes(String context) throws Exception {
        String json = kubectlExecutor.runCommand(context, "get", "nodes", "-o", "json");
        JsonNode root = objectMapper.readTree(json);

        List<NodeSummary> nodes = new ArrayList<>();
        for (JsonNode item : root.get("items")) {
            String name = item.at("/metadata/name").asText();
            String kubeletVersion = item.at("/status/nodeInfo/kubeletVersion").asText("unknown");
            String os = item.at("/status/nodeInfo/operatingSystem").asText("linux");
            String arch = item.at("/status/nodeInfo/architecture").asText("amd64");
            String cpu = item.at("/status/capacity/cpu").asText("?");
            String mem = item.at("/status/capacity/memory").asText("?");

            List<NodeSummary.NodeCondition> conditions = new ArrayList<>();
            boolean isReady = false;
            JsonNode conditionsNode = item.at("/status/conditions");
            if (conditionsNode.isArray()) {
                for (JsonNode cond : conditionsNode) {
                    String type   = cond.at("/type").asText();
                    String status = cond.at("/status").asText();
                    String msg    = cond.at("/message").asText("");
                    conditions.add(new NodeSummary.NodeCondition(type, status, msg));
                    if ("Ready".equals(type) && "True".equals(status)) {
                        isReady = true;
                    }
                }
            }
            nodes.add(new NodeSummary(name, isReady, kubeletVersion, os, arch, conditions, cpu, mem));
        }
        return nodes;
    }
}
