package com.studentreadiness.dto;

import com.studentreadiness.enums.SkillStatus;

public class SkillGapItem {
    private Long skillId;
    private String skillName;
    private String skillCategory;
    private int currentLevel;
    private int requiredLevel;
    private int gap;
    private int importanceWeight;
    private SkillStatus status;
    private String priorityReason;

    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getSkillCategory() { return skillCategory; }
    public void setSkillCategory(String skillCategory) { this.skillCategory = skillCategory; }

    public int getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(int currentLevel) { this.currentLevel = currentLevel; }

    public int getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(int requiredLevel) { this.requiredLevel = requiredLevel; }

    public int getGap() { return gap; }
    public void setGap(int gap) { this.gap = gap; }

    public int getImportanceWeight() { return importanceWeight; }
    public void setImportanceWeight(int importanceWeight) { this.importanceWeight = importanceWeight; }

    public SkillStatus getStatus() { return status; }
    public void setStatus(SkillStatus status) { this.status = status; }

    public String getPriorityReason() { return priorityReason; }
    public void setPriorityReason(String priorityReason) { this.priorityReason = priorityReason; }
}
