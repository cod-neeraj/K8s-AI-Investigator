package org.example;

import java.util.List;
import java.util.Map;

/**
 * The full evidence payload collected during one investigation run.
 * Contains pods, logs from unhealthy pods, cluster warning events,
 * node health, and deployment rollout status.
 */
public class InvestigationResult {
    private List<PodSummary>        pods;
    private Map<String, String>     logs;
    private List<EventSummary>      events;
    private List<NodeSummary>       nodes;
    private List<DeploymentSummary> deployments;

    public InvestigationResult(List<PodSummary> pods,
                               Map<String, String> logs,
                               List<EventSummary> events,
                               List<NodeSummary> nodes,
                               List<DeploymentSummary> deployments) {
        this.pods        = pods;
        this.logs        = logs;
        this.events      = events;
        this.nodes       = nodes;
        this.deployments = deployments;
    }

    public InvestigationResult() {}

    public List<PodSummary>        getPods()        { return pods; }
    public Map<String, String>     getLogs()        { return logs; }
    public List<EventSummary>      getEvents()      { return events; }
    public List<NodeSummary>       getNodes()       { return nodes; }
    public List<DeploymentSummary> getDeployments() { return deployments; }

    public void setPods(List<PodSummary> pods)               { this.pods = pods; }
    public void setLogs(Map<String, String> logs)            { this.logs = logs; }
    public void setEvents(List<EventSummary> events)         { this.events = events; }
    public void setNodes(List<NodeSummary> nodes)            { this.nodes = nodes; }
    public void setDeployments(List<DeploymentSummary> deps) { this.deployments = deps; }
}
