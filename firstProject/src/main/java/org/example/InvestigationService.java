package org.example;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates a full cluster investigation in four steps:
 *  1. Pod status triage (all namespaces)
 *  2. Targeted log harvesting (unhealthy pods only)
 *  3. Cluster-wide warning event collection
 *  4. Node health check
 *  5. Deployment rollout status
 */
@ApplicationScoped
public class InvestigationService {

    private final PodInspector        podInspector;
    private final LogsCollector       logsCollector;
    private final EventsAnalyzer      eventsAnalyzer;
    private final NodeInspector       nodeInspector;
    private final DeploymentInspector deploymentInspector;

    @Inject
    public InvestigationService(PodInspector podInspector,
                                LogsCollector logsCollector,
                                EventsAnalyzer eventsAnalyzer,
                                NodeInspector nodeInspector,
                                DeploymentInspector deploymentInspector) {
        this.podInspector        = podInspector;
        this.logsCollector       = logsCollector;
        this.eventsAnalyzer      = eventsAnalyzer;
        this.nodeInspector       = nodeInspector;
        this.deploymentInspector = deploymentInspector;
    }

    public InvestigationResult runFullInvestigation(String context) throws Exception {

        List<PodSummary> pods = podInspector.getPods(context);

        Map<String, String> logsPayload = new HashMap<>();
        for (PodSummary pod : pods) {
            if (!pod.isHealthy()) {
                String logContent = logsCollector.fetchPodLogs(context, pod.getNamespace(), pod.getName());
                logsPayload.put(pod.getNamespace() + "/" + pod.getName(), logContent);
            }
        }
        List<EventSummary> warningEvents = eventsAnalyzer.getWarningEvents(context);

        List<NodeSummary> nodes = nodeInspector.getNodes(context);

        List<DeploymentSummary> deployments = deploymentInspector.getDeployments(context);

        return new InvestigationResult(pods, logsPayload, warningEvents, nodes, deployments);
    }
}
