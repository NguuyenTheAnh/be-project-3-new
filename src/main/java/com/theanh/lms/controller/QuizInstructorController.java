package com.theanh.lms.controller;

import com.theanh.common.dto.ResponseDto;
import com.theanh.common.util.ResponseConfig;
import com.theanh.lms.dto.QuizAnswerDto;
import com.theanh.lms.dto.QuizDto;
import com.theanh.lms.dto.QuizQuestionDto;
import com.theanh.lms.dto.request.QuizAnswerRequest;
import com.theanh.lms.dto.request.QuizQuestionRequest;
import com.theanh.lms.dto.request.QuizRequest;
import com.theanh.lms.enums.QuestionType;
import com.theanh.lms.service.QuizAnswerService;
import com.theanh.lms.service.QuizQuestionService;
import com.theanh.lms.service.QuizService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/instructor")
@RequiredArgsConstructor
public class QuizInstructorController {

    private final QuizService quizService;
    private final QuizQuestionService quizQuestionService;
    private final QuizAnswerService quizAnswerService;

    @PostMapping("/lessons/{lessonId}/quiz")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<QuizDto>> createQuiz(@PathVariable Long lessonId,
                                                           @RequestBody @Valid QuizRequest request) {
        QuizDto dto = new QuizDto();
        dto.setLessonId(lessonId);
        dto.setTitle(request.getTitle());
        dto.setTimeLimitSeconds(request.getTimeLimitSeconds());
        dto.setPassScore(request.getPassScore());
        dto.setMaxAttempts(request.getMaxAttempts());
        dto.setShuffleQuestions(request.getShuffleQuestions());
        return ResponseConfig.success(quizService.saveObject(dto));
    }

    @PutMapping("/quizzes/{quizId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<QuizDto>> updateQuiz(@PathVariable Long quizId,
                                                           @RequestBody @Valid QuizRequest request) {
        QuizDto dto = quizService.findById(quizId);
        if (request.getLessonId() != null) {
            dto.setLessonId(request.getLessonId());
        }
        if (request.getTitle() != null) {
            dto.setTitle(request.getTitle());
        }
        if (request.getTimeLimitSeconds() != null) {
            dto.setTimeLimitSeconds(request.getTimeLimitSeconds());
        }
        if (request.getPassScore() != null) {
            dto.setPassScore(request.getPassScore());
        }
        if (request.getMaxAttempts() != null) {
            dto.setMaxAttempts(request.getMaxAttempts());
        }
        if (request.getShuffleQuestions() != null) {
            dto.setShuffleQuestions(request.getShuffleQuestions());
        }
        return ResponseConfig.success(quizService.saveObject(dto));
    }

    @DeleteMapping("/quizzes/{quizId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<QuizDto>> deleteQuiz(@PathVariable Long quizId) {
        quizService.deleteById(quizId);
        return ResponseConfig.success(null);
    }

    @PostMapping("/quizzes/{quizId}/questions")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<QuizQuestionDto>> createQuestion(@PathVariable Long quizId,
                                                                       @RequestBody @Valid QuizQuestionRequest request) {
        QuestionType type = parseQuestionType(request.getQuestionType());
        QuizQuestionDto dto = new QuizQuestionDto();
        dto.setQuizId(quizId);
        dto.setQuestionText(request.getQuestionText());
        dto.setQuestionType(type.name());
        dto.setPosition(request.getPosition());
        dto.setPoints(request.getPoints());
        dto.setExplanation(request.getExplanation());
        return ResponseConfig.success(quizQuestionService.saveObject(dto));
    }

    @PutMapping("/questions/{questionId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<QuizQuestionDto>> updateQuestion(@PathVariable Long questionId,
                                                                       @RequestBody @Valid QuizQuestionRequest request) {
        QuizQuestionDto dto = quizQuestionService.findById(questionId);
        if (request.getQuestionText() != null) {
            dto.setQuestionText(request.getQuestionText());
        }
        if (request.getQuestionType() != null) {
            QuestionType type = parseQuestionType(request.getQuestionType());
            dto.setQuestionType(type.name());
        }
        if (request.getPosition() != null) {
            dto.setPosition(request.getPosition());
        }
        if (request.getPoints() != null) {
            dto.setPoints(request.getPoints());
        }
        if (request.getExplanation() != null) {
            dto.setExplanation(request.getExplanation());
        }
        return ResponseConfig.success(quizQuestionService.saveObject(dto));
    }

    @DeleteMapping("/questions/{questionId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<QuizQuestionDto>> deleteQuestion(@PathVariable Long questionId) {
        quizQuestionService.deleteById(questionId);
        return ResponseConfig.success(null);
    }

    @PostMapping("/questions/{questionId}/answers")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<QuizAnswerDto>> createAnswer(@PathVariable Long questionId,
                                                                   @RequestBody @Valid QuizAnswerRequest request) {
        QuizAnswerDto dto = new QuizAnswerDto();
        dto.setQuestionId(questionId);
        dto.setAnswerText(request.getAnswerText());
        dto.setIsCorrect(request.getIsCorrect());
        dto.setPosition(request.getPosition());
        return ResponseConfig.success(quizAnswerService.saveObject(dto));
    }

    @PutMapping("/answers/{answerId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<QuizAnswerDto>> updateAnswer(@PathVariable Long answerId,
                                                                   @RequestBody @Valid QuizAnswerRequest request) {
        QuizAnswerDto dto = quizAnswerService.findById(answerId);
        if (request.getAnswerText() != null) {
            dto.setAnswerText(request.getAnswerText());
        }
        if (request.getIsCorrect() != null) {
            dto.setIsCorrect(request.getIsCorrect());
        }
        if (request.getPosition() != null) {
            dto.setPosition(request.getPosition());
        }
        return ResponseConfig.success(quizAnswerService.saveObject(dto));
    }

    @DeleteMapping("/answers/{answerId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<QuizAnswerDto>> deleteAnswer(@PathVariable Long answerId) {
        quizAnswerService.deleteById(answerId);
        return ResponseConfig.success(null);
    }

    @GetMapping("/quizzes/{quizId}/questions")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<List<QuizQuestionDto>>> listQuestions(@PathVariable Long quizId) {
        return ResponseConfig.success(quizQuestionService.findByQuiz(quizId));
    }

    @GetMapping("/questions/{questionId}/answers")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<List<QuizAnswerDto>>> listAnswers(@PathVariable Long questionId) {
        return ResponseConfig.success(quizAnswerService.findByQuestion(questionId));
    }

    private QuestionType parseQuestionType(String raw) {
        try {
            return QuestionType.valueOf(raw);
        } catch (Exception ex) {
            throw new com.theanh.common.exception.BusinessException("data.fail");
        }
    }
}
