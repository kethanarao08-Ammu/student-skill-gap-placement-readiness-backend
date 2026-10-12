package com.studentreadiness.dto;

public class LearningProgressUpdateRequest {
    private String status; // NOT_STARTED, IN_PROGRESS, COMPLETED

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
