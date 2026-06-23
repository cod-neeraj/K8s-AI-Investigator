package org.example;

import java.util.List;

public record AiSummaryResult(
        String overallHealth,
        List<AiIssue> issues
) {}
