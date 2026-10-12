package com.studentreadiness.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AssessmentResultResponse {
    private Long attemptId;
    private Long assessmentId;
    private String assessmentTitle;
    private double scorePercentage;
    private int totalQuestions;
    private int correctAnswers;
    private int incorrectAnswers;
    private String readinessCategory;
    private LocalDateTime attemptedAt;
    private Map<String, Integer> categoryPerformance;
    private List<QuestionResultItem> questionResults;

    public static class QuestionResultItem {
        private Long questionId;
        private String questionText;
        private String category;
        private String selectedOptionText;
        private String correctOptionText;
        private boolean correct;
        private String explanation;

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }

        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getSelectedOptionText() { return selectedOptionText; }
        public void setSelectedOptionText(String selectedOptionText) { this.selectedOptionText = selectedOptionText; }

        public String getCorrectOptionText() { return correctOptionText; }
        public void setCorrectOptionText(String correctOptionText) { this.correctOptionText = correctOptionText; }

        public boolean isCorrect() { return correct; }
        public void setCorrect(boolean correct) { this.correct = correct; }

        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
    }

    public Long getAttemptId() { return attemptId; }
    public void setAttemptId(Long attemptId) { this.attemptId = attemptId; }

    public Long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }

    public String getAssessmentTitle() { return assessmentTitle; }
    public void setAssessmentTitle(String assessmentTitle) { this.assessmentTitle = assessmentTitle; }

    public double getScorePercentage() { return scorePercentage; }
    public void setScorePercentage(double scorePercentage) { this.scorePercentage = scorePercentage; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public int getIncorrectAnswers() { return incorrectAnswers; }
    public void setIncorrectAnswers(int incorrectAnswers) { this.incorrectAnswers = incorrectAnswers; }

    public String getReadinessCategory() { return readinessCategory; }
    public void setReadinessCategory(String readinessCategory) { this.readinessCategory = readinessCategory; }

    public LocalDateTime getAttemptedAt() { return attemptedAt; }
    public void setAttemptedAt(LocalDateTime attemptedAt) { this.attemptedAt = attemptedAt; }

    public Map<String, Integer> getCategoryPerformance() { return categoryPerformance; }
    public void setCategoryPerformance(Map<String, Integer> categoryPerformance) { this.categoryPerformance = categoryPerformance; }

    public List<QuestionResultItem> getQuestionResults() { return questionResults; }
    public void setQuestionResults(List<QuestionResultItem> questionResults) { this.questionResults = questionResults; }
}
