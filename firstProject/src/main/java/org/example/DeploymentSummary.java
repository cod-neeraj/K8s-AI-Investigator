package org.example;

public class DeploymentSummary {
    private String name;
    private String namespace;
    private int desiredReplicas;
    private int readyReplicas;
    private int availableReplicas;
    private int updatedReplicas;
    private boolean healthy;
    private String statusMessage;

    public DeploymentSummary(String name, String namespace,
                             int desiredReplicas, int readyReplicas,
                             int availableReplicas, int updatedReplicas,
                             boolean healthy, String statusMessage) {
        this.name              = name;
        this.namespace         = namespace;
        this.desiredReplicas   = desiredReplicas;
        this.readyReplicas     = readyReplicas;
        this.availableReplicas = availableReplicas;
        this.updatedReplicas   = updatedReplicas;
        this.healthy           = healthy;
        this.statusMessage     = statusMessage;
    }

    public String getName()             { return name; }
    public String getNamespace()        { return namespace; }
    public int getDesiredReplicas()     { return desiredReplicas; }
    public int getReadyReplicas()       { return readyReplicas; }
    public int getAvailableReplicas()   { return availableReplicas; }
    public int getUpdatedReplicas()     { return updatedReplicas; }
    public boolean isHealthy()          { return healthy; }
    public String getStatusMessage()    { return statusMessage; }
}
