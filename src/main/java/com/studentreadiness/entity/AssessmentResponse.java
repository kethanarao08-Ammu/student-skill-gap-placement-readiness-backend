package com.studentreadiness.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "assessment_responses")
public class AssessmentResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    @JsonIgnore
    private AssessmentAttempt attempt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "question_id", nullable = false)
    private AssessmentQuestion question;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "selected_option_id")
    private AssessmentOption selectedOption;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(name = "marks_awarded", nullable = false)
    private int marksAwarded;

    public AssessmentResponse() {
    }

    public AssessmentResponse(AssessmentAttempt attempt, AssessmentQuestion question, AssessmentOption selectedOption, boolean correct) {
        this.attempt = attempt;
        this.question = question;
        this.selectedOption = selectedOption;
        this.correct = correct;
        this.marksAwarded = correct ? 1 : 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AssessmentAttempt getAttempt() {
        return attempt;
    }

    public void setAttempt(AssessmentAttempt attempt) {
        this.attempt = attempt;
    }

    public AssessmentQuestion getQuestion() {
        return question;
    }

    public void setQuestion(AssessmentQuestion question) {
        this.question = question;
    }

    public AssessmentOption getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(AssessmentOption selectedOption) {
        this.selectedOption = selectedOption;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
        this.marksAwarded = correct ? 1 : 0;
    }

    public int getMarksAwarded() {
        return marksAwarded;
    }

    public void setMarksAwarded(int marksAwarded) {
        this.marksAwarded = marksAwarded;
    }
}
