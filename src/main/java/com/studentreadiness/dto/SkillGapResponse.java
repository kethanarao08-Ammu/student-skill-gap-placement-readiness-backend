package com.studentreadiness.dto;

import java.util.List;

public class SkillGapResponse {
    private Long jobRoleId;
    private String jobRoleTitle;
    private double simpleMatchPercentage;
    private double weightedMatchPercentage;
    private String scoringMethodActive;
    private int matchedSkillsCount;
    private int needsImprovementCount;
    private int missingSkillsCount;
    private int totalRequiredSkills;
    private List<SkillGapItem> matchedSkills;
    private List<SkillGapItem> needsImprovementSkills;
    private List<SkillGapItem> missingSkills;
    private List<SkillGapItem> highPriorityGaps;

    public Long getJobRoleId() { return jobRoleId; }
    public void setJobRoleId(Long jobRoleId) { this.jobRoleId = jobRoleId; }

    public String getJobRoleTitle() { return jobRoleTitle; }
    public void setJobRoleTitle(String jobRoleTitle) { this.jobRoleTitle = jobRoleTitle; }

    public double getSimpleMatchPercentage() { return simpleMatchPercentage; }
    public void setSimpleMatchPercentage(double simpleMatchPercentage) { this.simpleMatchPercentage = simpleMatchPercentage; }

    public double getWeightedMatchPercentage() { return weightedMatchPercentage; }
    public void setWeightedMatchPercentage(double weightedMatchPercentage) { this.weightedMatchPercentage = weightedMatchPercentage; }

    public String getScoringMethodActive() { return scoringMethodActive; }
    public void setScoringMethodActive(String scoringMethodActive) { this.scoringMethodActive = scoringMethodActive; }

    public int getMatchedSkillsCount() { return matchedSkillsCount; }
    public void setMatchedSkillsCount(int matchedSkillsCount) { this.matchedSkillsCount = matchedSkillsCount; }

    public int getNeedsImprovementCount() { return needsImprovementCount; }
    public void setNeedsImprovementCount(int needsImprovementCount) { this.needsImprovementCount = needsImprovementCount; }

    public int getMissingSkillsCount() { return missingSkillsCount; }
    public void setMissingSkillsCount(int missingSkillsCount) { this.missingSkillsCount = missingSkillsCount; }

    public int getTotalRequiredSkills() { return totalRequiredSkills; }
    public void setTotalRequiredSkills(int totalRequiredSkills) { this.totalRequiredSkills = totalRequiredSkills; }

    public List<SkillGapItem> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(List<SkillGapItem> matchedSkills) { this.matchedSkills = matchedSkills; }

    public List<SkillGapItem> getNeedsImprovementSkills() { return needsImprovementSkills; }
    public void setNeedsImprovementSkills(List<SkillGapItem> needsImprovementSkills) { this.needsImprovementSkills = needsImprovementSkills; }

    public List<SkillGapItem> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<SkillGapItem> missingSkills) { this.missingSkills = missingSkills; }

    public List<SkillGapItem> getHighPriorityGaps() { return highPriorityGaps; }
    public void setHighPriorityGaps(List<SkillGapItem> highPriorityGaps) { this.highPriorityGaps = highPriorityGaps; }
}
