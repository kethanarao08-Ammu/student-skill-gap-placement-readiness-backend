package com.studentreadiness.dto;

import java.util.List;

public class QuestionDisplayDto {
    private Long id;
    private String questionText;
    private String category;
    private List<OptionDisplayDto> options;

    public static class OptionDisplayDto {
        private Long id;
        private String optionLetter;
        private String optionText;

        public OptionDisplayDto() {}

        public OptionDisplayDto(Long id, String optionLetter, String optionText) {
            this.id = id;
            this.optionLetter = optionLetter;
            this.optionText = optionText;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getOptionLetter() { return optionLetter; }
        public void setOptionLetter(String optionLetter) { this.optionLetter = optionLetter; }

        public String getOptionText() { return optionText; }
        public void setOptionText(String optionText) { this.optionText = optionText; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<OptionDisplayDto> getOptions() { return options; }
    public void setOptions(List<OptionDisplayDto> options) { this.options = options; }
}
