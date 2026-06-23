package org.example;

import java.util.List;

public class NodeSummary {
    private String name;
    private boolean ready;
    private String kubeletVersion;
    private String os;
    private String architecture;
    private List<NodeCondition> conditions;
    private String cpuCapacity;
    private String memoryCapacity;

    public NodeSummary(String name, boolean ready, String kubeletVersion,
                       String os, String architecture,
                       List<NodeCondition> conditions,
                       String cpuCapacity, String memoryCapacity) {
        this.name = name;
        this.ready = ready;
        this.kubeletVersion = kubeletVersion;
        this.os = os;
        this.architecture = architecture;
        this.conditions = conditions;
        this.cpuCapacity = cpuCapacity;
        this.memoryCapacity = memoryCapacity;
    }

    public String getName()             { return name; }
    public boolean isReady()            { return ready; }
    public String getKubeletVersion()   { return kubeletVersion; }
    public String getOs()               { return os; }
    public String getArchitecture()     { return architecture; }
    public List<NodeCondition> getConditions() { return conditions; }
    public String getCpuCapacity()      { return cpuCapacity; }
    public String getMemoryCapacity()   { return memoryCapacity; }

    /** Nested model for a single node condition (e.g. MemoryPressure=False). */
    public static class NodeCondition {
        private String type;
        private String status;   // "True" / "False" / "Unknown"
        private String message;

        public NodeCondition(String type, String status, String message) {
            this.type = type;
            this.status = status;
            this.message = message;
        }

        public String getType()    { return type; }
        public String getStatus()  { return status; }
        public String getMessage() { return message; }
    }
}
