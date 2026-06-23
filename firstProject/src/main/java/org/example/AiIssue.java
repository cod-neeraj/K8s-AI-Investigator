package org.example;

public record AiIssue(
        String affectedResource,
        String rootCause,
        int confidence,
        String severity,
        String suggestedFix
) {}
