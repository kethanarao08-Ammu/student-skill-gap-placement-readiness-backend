package com.studentreadiness.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "learning_resources")
public class LearningResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String topic;

    @Column(nullable = false, length = 255)
    private String url;

    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType = "COURSE";

    @Column(name = "estimated_duration", nullable = false, length = 50)
    private String estimatedDuration;

    @Column(name = "difficulty_level", nullable = false, length = 20)
    private String difficultyLevel = "INTERMEDIATE";

    public LearningResource() {
    }

    public LearningResource(Long id, Skill skill, String title, String topic, String url, String resourceType, String estimatedDuration, String difficultyLevel) {
        this.id = id;
        this.skill = skill;
        this.title = title;
        this.topic = topic;
        this.url = url;
        this.resourceType = resourceType;
        this.estimatedDuration = estimatedDuration;
        this.difficultyLevel = difficultyLevel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(String estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }
}
