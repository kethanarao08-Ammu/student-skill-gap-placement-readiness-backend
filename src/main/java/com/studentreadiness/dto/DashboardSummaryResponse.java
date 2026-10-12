package com.studentreadiness.dto;

import java.util.List;
import java.util.Map;

public class DashboardSummaryResponse {
    private String fullName;
    private String email;
    private Long userId;

    // Profile completeness (0-100%)
    private int profileCompletionPercentage;

    // Target role
    private Long targetJobRoleId;
    private String targetJobRoleTitle;

    // Key scores
    private double skillMatchPercentage;
    private double overallReadinessScore;
    private String readinessCategory;
    private double latestAssessmentScore;
    private String latestAssessmentCategory;
    private boolean hasCompletedAssessment;

    // Quick stats
    private int totalSkillsAdded;
    private int totalProjects;
    private int totalCertifications;
    private int completedLearningItems;
    private int totalLearningItems;

    // Top 3 strongest skills
    private List<Map<String, Object>> strongestSkills;

    // Top 3 critical gaps
    private List<Map<String, Object>> topSkillGaps;

    // Pending recommendations (up to 3)
    private List<LearningRecommendationResponse> pendingRecommendations;

    // Assessment history (last 3)
    private List<Map<String, Object>> recentAssessments;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public int getProfileCompletionPercentage() { return profileCompletionPercentage; }
    public void setProfileCompletionPercentage(int profileCompletionPercentage) { this.profileCompletionPercentage = profileCompletionPercentage; }

    public Long getTargetJobRoleId() { return targetJobRoleId; }
    public void setTargetJobRoleId(Long targetJobRoleId) { this.targetJobRoleId = targetJobRoleId; }

    public String getTargetJobRoleTitle() { return targetJobRoleTitle; }
    public void setTargetJobRoleTitle(String targetJobRoleTitle) { this.targetJobRoleTitle = targetJobRoleTitle; }

    public double getSkillMatchPercentage() { return skillMatchPercentage; }
    public void setSkillMatchPercentage(double skillMatchPercentage) { this.skillMatchPercentage = skillMatchPercentage; }

    public double getOverallReadinessScore() { return overallReadinessScore; }
    public void setOverallReadinessScore(double overallReadinessScore) { this.overallReadinessScore = overallReadinessScore; }

    public String getReadinessCategory() { return readinessCategory; }
    public void setReadinessCategory(String readinessCategory) { this.readinessCategory = readinessCategory; }

    public double getLatestAssessmentScore() { return latestAssessmentScore; }
    public void setLatestAssessmentScore(double latestAssessmentScore) { this.latestAssessmentScore = latestAssessmentScore; }

    public String getLatestAssessmentCategory() { return latestAssessmentCategory; }
    public void setLatestAssessmentCategory(String latestAssessmentCategory) { this.latestAssessmentCategory = latestAssessmentCategory; }

    public boolean isHasCompletedAssessment() { return hasCompletedAssessment; }
    public void setHasCompletedAssessment(boolean hasCompletedAssessment) { this.hasCompletedAssessment = hasCompletedAssessment; }

    public int getTotalSkillsAdded() { return totalSkillsAdded; }
    public void setTotalSkillsAdded(int totalSkillsAdded) { this.totalSkillsAdded = totalSkillsAdded; }

    public int getTotalProjects() { return totalProjects; }
    public void setTotalProjects(int totalProjects) { this.totalProjects = totalProjects; }

    public int getTotalCertifications() { return totalCertifications; }
    public void setTotalCertifications(int totalCertifications) { this.totalCertifications = totalCertifications; }

    public int getCompletedLearningItems() { return completedLearningItems; }
    public void setCompletedLearningItems(int completedLearningItems) { this.completedLearningItems = completedLearningItems; }

    public int getTotalLearningItems() { return totalLearningItems; }
    public void setTotalLearningItems(int totalLearningItems) { this.totalLearningItems = totalLearningItems; }

    public List<Map<String, Object>> getStrongestSkills() { return strongestSkills; }
    public void setStrongestSkills(List<Map<String, Object>> strongestSkills) { this.strongestSkills = strongestSkills; }

    public List<Map<String, Object>> getTopSkillGaps() { return topSkillGaps; }
    public void setTopSkillGaps(List<Map<String, Object>> topSkillGaps) { this.topSkillGaps = topSkillGaps; }

    public List<LearningRecommendationResponse> getPendingRecommendations() { return pendingRecommendations; }
    public void setPendingRecommendations(List<LearningRecommendationResponse> pendingRecommendations) { this.pendingRecommendations = pendingRecommendations; }

    public List<Map<String, Object>> getRecentAssessments() { return recentAssessments; }
    public void setRecentAssessments(List<Map<String, Object>> recentAssessments) { this.recentAssessments = recentAssessments; }
}
