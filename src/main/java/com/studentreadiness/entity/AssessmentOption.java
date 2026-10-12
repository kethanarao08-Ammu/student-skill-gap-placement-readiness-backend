package com.studentreadiness.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "assessment_options")
public class AssessmentOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnore
    private AssessmentQuestion question;

    @Column(name = "option_text", nullable = false, length = 255)
    private String optionText;

    @Column(name = "option_letter", nullable = false, length = 2)
    private String optionLetter;

    @Column(name = "is_correct", nullable = false)
    private boolean correct = false;

    public AssessmentOption() {
    }

    public AssessmentOption(AssessmentQuestion question, String optionLetter, String optionText, boolean correct) {
        this.question = question;
        this.optionLetter = optionLetter;
        this.optionText = optionText;
        this.correct = correct;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AssessmentQuestion getQuestion() {
        return question;
    }

    public void setQuestion(AssessmentQuestion question) {
        this.question = question;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public String getOptionLetter() {
        return optionLetter;
    }

    public void setOptionLetter(String optionLetter) {
        this.optionLetter = optionLetter;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
}
