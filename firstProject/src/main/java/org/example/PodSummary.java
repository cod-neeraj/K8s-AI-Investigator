package org.example;

public class PodSummary {
    private String name;
    private String namespace;
    private String status;
    private boolean healthy;

    public PodSummary(String name, String namespace, String status, boolean healthy) {
        this.name = name;
        this.namespace = namespace;
        this.status = status;
        this.healthy = healthy;
    }

    // Getters and Setters
    public String getName() { return name; }
    public String getNamespace() { return namespace; }
    public String getStatus() { return status; }
    public boolean isHealthy() { return healthy; }
}