package com.studentreadiness.dto;

public class StudentSkillResponse {
    private Long id;
    private Long skillId;
    private String skillName;
    private String skillCategory;
    private int proficiencyLevel;
    private String proficiencyLabel;

    public static String getProficiencyLabel(int level) {
        return switch (level) {
            case 1 -> "Beginner";
            case 2 -> "Basic";
            case 3 -> "Intermediate";
            case 4 -> "Advanced";
            case 5 -> "Expert";
            default -> "Unknown";
        };
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getSkillCategory() { return skillCategory; }
    public void setSkillCategory(String skillCategory) { this.skillCategory = skillCategory; }

    public int getProficiencyLevel() { return proficiencyLevel; }
    public void setProficiencyLevel(int proficiencyLevel) {
        this.proficiencyLevel = proficiencyLevel;
        this.proficiencyLabel = getProficiencyLabel(proficiencyLevel);
    }

    public String getProficiencyLabel() { return proficiencyLabel; }
    public void setProficiencyLabel(String proficiencyLabel) { this.proficiencyLabel = proficiencyLabel; }
}
