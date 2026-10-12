package com.studentreadiness.enums;

public enum ReadinessCategory {
    NEEDS_STRONG_IMPROVEMENT("Needs Strong Improvement", 0.0, 39.99),
    DEVELOPING("Developing", 40.0, 59.99),
    NEARLY_READY("Nearly Ready", 60.0, 79.99),
    PLACEMENT_READY("Placement Ready", 80.0, 100.0);

    private final String displayName;
    private final double minScore;
    private final double maxScore;

    ReadinessCategory(String displayName, double minScore, double maxScore) {
        this.displayName = displayName;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getMinScore() {
        return minScore;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public static ReadinessCategory fromScore(double score) {
        if (score >= 80.0) {
            return PLACEMENT_READY;
        } else if (score >= 60.0) {
            return NEARLY_READY;
        } else if (score >= 40.0) {
            return DEVELOPING;
        } else {
            return NEEDS_STRONG_IMPROVEMENT;
        }
    }
}
