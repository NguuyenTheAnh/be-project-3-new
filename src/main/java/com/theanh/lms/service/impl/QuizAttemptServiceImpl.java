package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.common.exception.BusinessException;
import com.theanh.lms.dto.QuizAnswerDto;
import com.theanh.lms.dto.QuizAttemptAnswerDto;
import com.theanh.lms.dto.QuizAttemptDto;
import com.theanh.lms.dto.QuizDto;
import com.theanh.lms.dto.QuizQuestionDto;
import com.theanh.lms.entity.QuizAttempt;
import com.theanh.lms.entity.QuizAttemptAnswer;
import com.theanh.lms.repository.QuizAttemptAnswerRepository;
import com.theanh.lms.repository.QuizAttemptRepository;
import com.theanh.lms.service.AccessControlService;
import com.theanh.lms.service.EnrollmentService;
import com.theanh.lms.service.QuizAnswerService;
import com.theanh.lms.service.QuizAttemptService;
import com.theanh.lms.service.QuizQuestionService;
import com.theanh.lms.service.QuizService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizAttemptServiceImpl extends BaseServiceImpl<QuizAttempt, QuizAttemptDto, Long> implements QuizAttemptService {

    private final QuizAttemptRepository repository;
    private final QuizAttemptAnswerRepository attemptAnswerRepository;
    private final QuizService quizService;
    private final QuizQuestionService quizQuestionService;
    private final QuizAnswerService quizAnswerService;
    private final EnrollmentService enrollmentService;
    private final AccessControlService accessControlService;

    public QuizAttemptServiceImpl(QuizAttemptRepository repository,
                                  QuizAttemptAnswerRepository attemptAnswerRepository,
                                  QuizService quizService,
                                  QuizQuestionService quizQuestionService,
                                  QuizAnswerService quizAnswerService,
                                  EnrollmentService enrollmentService,
                                  AccessControlService accessControlService,
                                  ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.repository = repository;
        this.attemptAnswerRepository = attemptAnswerRepository;
        this.quizService = quizService;
        this.quizQuestionService = quizQuestionService;
        this.quizAnswerService = quizAnswerService;
        this.enrollmentService = enrollmentService;
        this.accessControlService = accessControlService;
    }

    @Override
    @Transactional
    public QuizAttemptDto startAttempt(Long userId, Long quizId) {
        QuizDto quiz = quizService.findActiveById(quizId);
        if (quiz == null) {
            throw new BusinessException("data.not_found");
        }
        // require enrollment if quiz tied to course
        if (!Boolean.TRUE.equals(accessControlService.canViewLesson(userId, null, quiz.getLessonId()))) {
            throw new BusinessException("data.fail");
        }
        QuizAttempt attempt = QuizAttempt.builder()
                .quizId(quizId)
                .userId(userId)
                .startedAt(LocalDateTime.now())
                .status("IN_PROGRESS")
                .build();
        attempt.setIsActive(Boolean.TRUE);
        attempt.setIsDeleted(Boolean.FALSE);
        QuizAttempt saved = repository.save(attempt);
        return modelMapper.map(saved, QuizAttemptDto.class);
    }

    @Override
    @Transactional
    public QuizAttemptDto submitAttempt(Long userId, Long quizId, Long attemptId, List<QuizAttemptAnswerDto> answers) {
        QuizDto quiz = quizService.findActiveById(quizId);
        if (quiz == null) {
            throw new BusinessException("data.not_found");
        }
        QuizAttempt attempt = repository.findActiveById(attemptId)
                .orElseThrow(() -> new BusinessException("data.not_found"));
        if (!Objects.equals(attempt.getUserId(), userId) || !Objects.equals(attempt.getQuizId(), quizId)) {
            throw new BusinessException("data.fail");
        }
        List<QuizQuestionDto> questions = quizQuestionService.findByQuiz(quizId);
        if (CollectionUtils.isEmpty(questions)) {
            throw new BusinessException("data.fail");
        }
        Map<Long, QuizQuestionDto> questionMap = questions.stream()
                .collect(Collectors.toMap(QuizQuestionDto::getId, q -> q));
        Map<Long, List<QuizAnswerDto>> answersMap = new HashMap<>();
        for (QuizQuestionDto q : questions) {
            answersMap.put(q.getId(), quizAnswerService.findByQuestion(q.getId()));
        }
        BigDecimal total = BigDecimal.ZERO;
        List<QuizAttemptAnswer> toSave = new ArrayList<>();
        for (QuizAttemptAnswerDto ans : answers) {
            QuizQuestionDto question = questionMap.get(ans.getQuestionId());
            if (question == null) {
                continue;
            }
            QuizAttemptAnswer entity = QuizAttemptAnswer.builder()
                    .quizAttemptId(attemptId)
                    .questionId(ans.getQuestionId())
                    .answerId(ans.getAnswerId())
                    .answerText(ans.getAnswerText())
                    .build();
            entity.setIsActive(Boolean.TRUE);
            entity.setIsDeleted(Boolean.FALSE);
            boolean correct = evaluateAnswer(question, ans, answersMap.get(question.getId()));
            entity.setIsCorrect(correct);
            BigDecimal points = correct && question.getPoints() != null ? question.getPoints() : BigDecimal.ZERO;
            entity.setPointsAwarded(points);
            total = total.add(points);
            toSave.add(entity);
        }
        if (!toSave.isEmpty()) {
            attemptAnswerRepository.saveAll(toSave);
        }
        attempt.setScore(total);
        attempt.setStatus("SUBMITTED");
        attempt.setSubmittedAt(LocalDateTime.now());
        QuizAttempt saved = repository.save(attempt);
        return modelMapper.map(saved, QuizAttemptDto.class);
    }

    @Override
    public QuizAttemptDto getAttempt(Long userId, Long quizId, Long attemptId) {
        QuizAttempt attempt = repository.findActiveById(attemptId)
                .orElseThrow(() -> new BusinessException("data.not_found"));
        if (!Objects.equals(attempt.getUserId(), userId) || !Objects.equals(attempt.getQuizId(), quizId)) {
            throw new BusinessException("data.fail");
        }
        return modelMapper.map(attempt, QuizAttemptDto.class);
    }

    @Override
    public List<QuizAttemptDto> listAttempts(Long userId, Long quizId) {
        return repository.findByUserAndQuiz(userId, quizId).stream()
                .map(a -> modelMapper.map(a, QuizAttemptDto.class))
                .toList();
    }

    private boolean evaluateAnswer(QuizQuestionDto question, QuizAttemptAnswerDto ans, List<QuizAnswerDto> options) {
        if (options == null) {
            options = List.of();
        }
        switch (question.getQuestionType()) {
            case "TRUE_FALSE":
            case "SINGLE":
            case "MULTI":
                if (ans.getAnswerId() == null) {
                    return false;
                }
                return options.stream()
                        .filter(o -> Objects.equals(o.getId(), ans.getAnswerId()))
                        .findFirst()
                        .map(opt -> Boolean.TRUE.equals(opt.getIsCorrect()))
                        .orElse(false);
            case "TEXT":
                return false;
            default:
                return false;
        }
    }

    @Override
    protected Class<QuizAttempt> getEntityClass() {
        return QuizAttempt.class;
    }

    @Override
    protected Class<QuizAttemptDto> getDtoClass() {
        return QuizAttemptDto.class;
    }
}
