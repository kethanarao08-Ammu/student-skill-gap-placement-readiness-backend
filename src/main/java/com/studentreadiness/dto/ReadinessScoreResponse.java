package com.studentreadiness.dto;

import java.util.List;
import java.util.Map;

public class ReadinessScoreResponse {
    private Long jobRoleId;
    private String jobRoleTitle;
    private double overallReadinessScore;
    private String readinessCategory;
    private boolean isProvisional;
    private String provisionalReason;

    // Component scores (normalized 0–100)
    private double skillMatchScore;       // 40% weight
    private double assessmentScore;       // 35% weight
    private double projectScore;          // 15% weight
    private double certificationScore;    // 10% weight

    // Component details
    private Map<String, Double> componentWeights;
    private Map<String, Double> componentScores;
    private List<String> missingComponents;

    public Long getJobRoleId() { return jobRoleId; }
    public void setJobRoleId(Long jobRoleId) { this.jobRoleId = jobRoleId; }

    public String getJobRoleTitle() { return jobRoleTitle; }
    public void setJobRoleTitle(String jobRoleTitle) { this.jobRoleTitle = jobRoleTitle; }

    public double getOverallReadinessScore() { return overallReadinessScore; }
    public void setOverallReadinessScore(double overallReadinessScore) { this.overallReadinessScore = overallReadinessScore; }

    public String getReadinessCategory() { return readinessCategory; }
    public void setReadinessCategory(String readinessCategory) { this.readinessCategory = readinessCategory; }

    public boolean isProvisional() { return isProvisional; }
    public void setProvisional(boolean provisional) { isProvisional = provisional; }

    public String getProvisionalReason() { return provisionalReason; }
    public void setProvisionalReason(String provisionalReason) { this.provisionalReason = provisionalReason; }

    public double getSkillMatchScore() { return skillMatchScore; }
    public void setSkillMatchScore(double skillMatchScore) { this.skillMatchScore = skillMatchScore; }

    public double getAssessmentScore() { return assessmentScore; }
    public void setAssessmentScore(double assessmentScore) { this.assessmentScore = assessmentScore; }

    public double getProjectScore() { return projectScore; }
    public void setProjectScore(double projectScore) { this.projectScore = projectScore; }

    public double getCertificationScore() { return certificationScore; }
    public void setCertificationScore(double certificationScore) { this.certificationScore = certificationScore; }

    public Map<String, Double> getComponentWeights() { return componentWeights; }
    public void setComponentWeights(Map<String, Double> componentWeights) { this.componentWeights = componentWeights; }

    public Map<String, Double> getComponentScores() { return componentScores; }
    public void setComponentScores(Map<String, Double> componentScores) { this.componentScores = componentScores; }

    public List<String> getMissingComponents() { return missingComponents; }
    public void setMissingComponents(List<String> missingComponents) { this.missingComponents = missingComponents; }
}
