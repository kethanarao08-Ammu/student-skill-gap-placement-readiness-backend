package com.studentreadiness.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class JobRoleSkillRequest {

    @NotNull(message = "Skill ID is required")
    private Long skillId;

    @NotNull(message = "Required level is required")
    @Min(value = 1, message = "Required level must be at least 1")
    @Max(value = 5, message = "Required level must be at most 5")
    private Integer requiredLevel;

    @NotNull(message = "Importance weight is required")
    @Min(value = 1, message = "Importance weight must be at least 1")
    @Max(value = 5, message = "Importance weight must be at most 5")
    private Integer importanceWeight;

    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }

    public Integer getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(Integer requiredLevel) { this.requiredLevel = requiredLevel; }

    public Integer getImportanceWeight() { return importanceWeight; }
    public void setImportanceWeight(Integer importanceWeight) { this.importanceWeight = importanceWeight; }
}
