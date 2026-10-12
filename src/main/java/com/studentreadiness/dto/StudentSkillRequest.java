package com.studentreadiness.dto;

import jakarta.validation.constraints.*;

public class StudentSkillRequest {

    @NotNull(message = "Skill ID is required")
    private Long skillId;

    @NotNull(message = "Proficiency level is required")
    @Min(value = 1, message = "Proficiency level must be at least 1 (Beginner)")
    @Max(value = 5, message = "Proficiency level must be at most 5 (Expert)")
    private Integer proficiencyLevel;

    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }

    public Integer getProficiencyLevel() { return proficiencyLevel; }
    public void setProficiencyLevel(Integer proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }
}
