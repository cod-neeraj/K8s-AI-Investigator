package org.example;

public class EventSummary {
    private String name;
    private String namespace;
    private String reason;
    private String message;
    private String type;

    public EventSummary(String name, String namespace, String reason, String message, String type) {
        this.name = name;
        this.namespace = namespace;
        this.reason = reason;
        this.message = message;
        this.type = type;
    }

    // Getters
    public String getName() { return name; }
    public String getNamespace() { return namespace; }
    public String getReason() { return reason; }
    public String getMessage() { return message; }
    public String getType() { return type; }
}