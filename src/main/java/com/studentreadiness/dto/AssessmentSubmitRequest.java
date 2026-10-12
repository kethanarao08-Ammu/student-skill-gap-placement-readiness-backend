package com.studentreadiness.dto;

import java.util.List;
import java.util.Map;

public class AssessmentSubmitRequest {
    private Long assessmentId;
    // Map of questionId -> selectedOptionId (null if skipped)
    private Map<Long, Long> answers;

    public Long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }

    public Map<Long, Long> getAnswers() { return answers; }
    public void setAnswers(Map<Long, Long> answers) { this.answers = answers; }
}
