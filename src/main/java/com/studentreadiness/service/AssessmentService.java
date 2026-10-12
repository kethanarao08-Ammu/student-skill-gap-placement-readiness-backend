package com.studentreadiness.service;

import com.studentreadiness.dto.AssessmentResultResponse;
import com.studentreadiness.dto.AssessmentSubmitRequest;
import com.studentreadiness.dto.QuestionDisplayDto;
import com.studentreadiness.entity.*;
import com.studentreadiness.enums.ReadinessCategory;
import com.studentreadiness.exception.AccessDeniedException;
import com.studentreadiness.exception.ResourceNotFoundException;
import com.studentreadiness.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentQuestionRepository questionRepository;
    private final AssessmentOptionRepository optionRepository;
    private final AssessmentAttemptRepository attemptRepository;
    private final UserRepository userRepository;

    public AssessmentService(AssessmentRepository assessmentRepository,
                             AssessmentQuestionRepository questionRepository,
                             AssessmentOptionRepository optionRepository,
                             AssessmentAttemptRepository attemptRepository,
                             UserRepository userRepository) {
        this.assessmentRepository = assessmentRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.attemptRepository = attemptRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Assessment> getAllActiveAssessments() {
        return assessmentRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public Assessment getAssessmentById(Long id) {
        return assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", "id", id));
    }

    @Transactional(readOnly = true)
    public List<QuestionDisplayDto> getAssessmentQuestions(Long assessmentId) {
        Assessment assessment = getAssessmentById(assessmentId);
        List<AssessmentQuestion> questions = questionRepository.findByAssessmentId(assessment.getId());

        return questions.stream().map(q -> {
            QuestionDisplayDto dto = new QuestionDisplayDto();
            dto.setId(q.getId());
            dto.setQuestionText(q.getQuestionText());
            dto.setCategory(q.getCategory());

            // Convert options WITHOUT revealing correctness or explanation
            List<QuestionDisplayDto.OptionDisplayDto> options = q.getOptions().stream()
                    .map(opt -> new QuestionDisplayDto.OptionDisplayDto(
                            opt.getId(),
                            opt.getOptionLetter(),
                            opt.getOptionText()
                    ))
                    .collect(Collectors.toList());

            dto.setOptions(options);
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public AssessmentResultResponse submitAssessment(Long userId, AssessmentSubmitRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Assessment assessment = getAssessmentById(request.getAssessmentId());

        List<AssessmentQuestion> questions = questionRepository.findByAssessmentId(assessment.getId());
        if (questions.isEmpty()) {
            throw new ResourceNotFoundException("No questions found for assessment " + assessment.getId());
        }

        Map<Long, Long> answers = request.getAnswers() != null ? request.getAnswers() : Collections.emptyMap();

        int correctCount = 0;
        List<AssessmentResponse> responses = new ArrayList<>();
        List<AssessmentResultResponse.QuestionResultItem> resultItems = new ArrayList<>();
        Map<String, int[]> categoryStats = new HashMap<>(); // category -> [correct, total]

        AssessmentAttempt attempt = new AssessmentAttempt();
        attempt.setUser(user);
        attempt.setAssessment(assessment);
        attempt.setTotalQuestions(questions.size());

        for (AssessmentQuestion question : questions) {
            String category = question.getCategory() != null ? question.getCategory() : "General";
            categoryStats.putIfAbsent(category, new int[]{0, 0});
            categoryStats.get(category)[1]++;

            AssessmentOption correctOption = question.getOptions().stream()
                    .filter(AssessmentOption::isCorrect)
                    .findFirst()
                    .orElse(null);

            Long selectedOptionId = answers.get(question.getId());
            AssessmentOption selectedOption = null;
            boolean isCorrect = false;

            if (selectedOptionId != null) {
                selectedOption = optionRepository.findById(selectedOptionId).orElse(null);
                if (selectedOption != null && correctOption != null && selectedOption.getId().equals(correctOption.getId())) {
                    isCorrect = true;
                    correctCount++;
                    categoryStats.get(category)[0]++;
                }
            }

            AssessmentResponse resp = new AssessmentResponse(attempt, question, selectedOption, isCorrect);
            responses.add(resp);

            // Populate result item for review
            AssessmentResultResponse.QuestionResultItem item = new AssessmentResultResponse.QuestionResultItem();
            item.setQuestionId(question.getId());
            item.setQuestionText(question.getQuestionText());
            item.setCategory(category);
            item.setSelectedOptionText(selectedOption != null
                    ? selectedOption.getOptionLetter() + ". " + selectedOption.getOptionText()
                    : "Not answered");
            item.setCorrectOptionText(correctOption != null
                    ? correctOption.getOptionLetter() + ". " + correctOption.getOptionText()
                    : "N/A");
            item.setCorrect(isCorrect);
            item.setExplanation(question.getExplanation());
            resultItems.add(item);
        }

        double scorePercentage = BigDecimal.valueOf((correctCount * 100.0) / questions.size())
                .setScale(2, RoundingMode.HALF_UP).doubleValue();

        ReadinessCategory readinessCategory = ReadinessCategory.fromScore(scorePercentage);

        attempt.setCorrectAnswers(correctCount);
        attempt.setScorePercentage(BigDecimal.valueOf(scorePercentage));
        attempt.setReadinessCategory(readinessCategory.getDisplayName());
        attempt.setResponses(responses);

        AssessmentAttempt savedAttempt = attemptRepository.save(attempt);

        // Calculate category percentages
        Map<String, Integer> categoryPerf = new LinkedHashMap<>();
        for (Map.Entry<String, int[]> entry : categoryStats.entrySet()) {
            int catCorrect = entry.getValue()[0];
            int catTotal = entry.getValue()[1];
            int pct = catTotal > 0 ? (int) Math.round((catCorrect * 100.0) / catTotal) : 0;
            categoryPerf.put(entry.getKey(), pct);
        }

        AssessmentResultResponse result = new AssessmentResultResponse();
        result.setAttemptId(savedAttempt.getId());
        result.setAssessmentId(assessment.getId());
        result.setAssessmentTitle(assessment.getTitle());
        result.setScorePercentage(scorePercentage);
        result.setTotalQuestions(questions.size());
        result.setCorrectAnswers(correctCount);
        result.setIncorrectAnswers(questions.size() - correctCount);
        result.setReadinessCategory(readinessCategory.getDisplayName());
        result.setAttemptedAt(savedAttempt.getAttemptedAt());
        result.setCategoryPerformance(categoryPerf);
        result.setQuestionResults(resultItems);

        return result;
    }

    @Transactional(readOnly = true)
    public List<AssessmentAttempt> getAttemptHistory(Long userId) {
        return attemptRepository.findByUserIdOrderByAttemptedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public AssessmentResultResponse getAttemptResult(Long userId, Long attemptId) {
        AssessmentAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("AssessmentAttempt", "id", attemptId));

        if (!attempt.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to view this assessment attempt");
        }

        List<AssessmentResultResponse.QuestionResultItem> resultItems = new ArrayList<>();
        Map<String, int[]> categoryStats = new HashMap<>();

        for (AssessmentResponse r : attempt.getResponses()) {
            AssessmentQuestion q = r.getQuestion();
            String category = q.getCategory() != null ? q.getCategory() : "General";
            categoryStats.putIfAbsent(category, new int[]{0, 0});
            categoryStats.get(category)[1]++;
            if (r.isCorrect()) {
                categoryStats.get(category)[0]++;
            }

            AssessmentOption correctOption = q.getOptions().stream()
                    .filter(AssessmentOption::isCorrect)
                    .findFirst()
                    .orElse(null);

            AssessmentResultResponse.QuestionResultItem item = new AssessmentResultResponse.QuestionResultItem();
            item.setQuestionId(q.getId());
            item.setQuestionText(q.getQuestionText());
            item.setCategory(category);
            item.setSelectedOptionText(r.getSelectedOption() != null
                    ? r.getSelectedOption().getOptionLetter() + ". " + r.getSelectedOption().getOptionText()
                    : "Not answered");
            item.setCorrectOptionText(correctOption != null
                    ? correctOption.getOptionLetter() + ". " + correctOption.getOptionText()
                    : "N/A");
            item.setCorrect(r.isCorrect());
            item.setExplanation(q.getExplanation());
            resultItems.add(item);
        }

        Map<String, Integer> categoryPerf = new LinkedHashMap<>();
        for (Map.Entry<String, int[]> entry : categoryStats.entrySet()) {
            int catCorrect = entry.getValue()[0];
            int catTotal = entry.getValue()[1];
            int pct = catTotal > 0 ? (int) Math.round((catCorrect * 100.0) / catTotal) : 0;
            categoryPerf.put(entry.getKey(), pct);
        }

        AssessmentResultResponse result = new AssessmentResultResponse();
        result.setAttemptId(attempt.getId());
        result.setAssessmentId(attempt.getAssessment().getId());
        result.setAssessmentTitle(attempt.getAssessment().getTitle());
        result.setScorePercentage(attempt.getScorePercentage().doubleValue());
        result.setTotalQuestions(attempt.getTotalQuestions());
        result.setCorrectAnswers(attempt.getCorrectAnswers());
        result.setIncorrectAnswers(attempt.getTotalQuestions() - attempt.getCorrectAnswers());
        result.setReadinessCategory(attempt.getReadinessCategory());
        result.setAttemptedAt(attempt.getAttemptedAt());
        result.setCategoryPerformance(categoryPerf);
        result.setQuestionResults(resultItems);

        return result;
    }
}
