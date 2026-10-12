package com.studentreadiness.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_role_skills", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"job_role_id", "skill_id"})
})
public class JobRoleSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_role_id", nullable = false)
    @JsonIgnore
    private JobRole jobRole;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "required_level", nullable = false)
    private int requiredLevel; // 1 to 5

    @Column(name = "required_proficiency", nullable = false)
    private int requiredProficiency; // Legacy schema compatibility

    @Column(name = "importance_weight", nullable = false)
    private int importanceWeight = 3; // 1 to 5

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public JobRoleSkill() {
    }

    public JobRoleSkill(JobRole jobRole, Skill skill, int requiredLevel, int importanceWeight) {
        this.jobRole = jobRole;
        this.skill = skill;
        this.requiredLevel = requiredLevel;
        this.requiredProficiency = requiredLevel;
        this.importanceWeight = importanceWeight;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JobRole getJobRole() {
        return jobRole;
    }

    public void setJobRole(JobRole jobRole) {
        this.jobRole = jobRole;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public void setRequiredLevel(int requiredLevel) {
        this.requiredLevel = requiredLevel;
        this.requiredProficiency = requiredLevel;
    }

    public int getRequiredProficiency() {
        return requiredProficiency;
    }

    public void setRequiredProficiency(int requiredProficiency) {
        this.requiredProficiency = requiredProficiency;
        this.requiredLevel = requiredProficiency;
    }

    public int getImportanceWeight() {
        return importanceWeight;
    }

    public void setImportanceWeight(int importanceWeight) {
        this.importanceWeight = importanceWeight;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
