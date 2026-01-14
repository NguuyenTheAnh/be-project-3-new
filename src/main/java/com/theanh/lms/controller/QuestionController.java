package com.theanh.lms.controller;

import com.theanh.common.dto.ResponseDto;
import com.theanh.common.exception.BusinessException;
import com.theanh.common.util.ResponseConfig;
import com.theanh.lms.dto.AnswerDto;
import com.theanh.lms.dto.QuestionDetailResponse;
import com.theanh.lms.dto.QuestionDto;
import com.theanh.lms.dto.QuestionVoteDto;
import com.theanh.lms.dto.request.AnswerRequest;
import com.theanh.lms.dto.request.QuestionRequest;
import com.theanh.lms.dto.request.QuestionVoteRequest;
import com.theanh.lms.enums.QuestionStatus;
import com.theanh.lms.enums.RoleName;
import com.theanh.lms.service.AnswerService;
import com.theanh.lms.service.CourseInstructorService;
import com.theanh.lms.service.QuestionService;
import com.theanh.lms.service.QuestionVoteService;
import com.theanh.lms.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.annotation.security.PermitAll;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final AnswerService answerService;
    private final QuestionVoteService questionVoteService;
    private final CourseInstructorService courseInstructorService;
    private final UserService userService;

    @PostMapping("/courses/{courseId}/questions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<QuestionDto>> createQuestion(@PathVariable Long courseId,
                                                                   @RequestBody @Valid QuestionRequest request) {
        Long userId = currentUserId();
        QuestionDto dto = new QuestionDto();
        dto.setCourseId(courseId);
        dto.setLessonId(request.getLessonId());
        dto.setUserId(userId);
        dto.setTitle(request.getTitle());
        dto.setContent(request.getContent());
        dto.setStatus(QuestionStatus.OPEN.name());
        return ResponseConfig.success(questionService.saveObject(dto));
    }

    @PutMapping("/questions/{questionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<QuestionDto>> updateQuestion(@PathVariable Long questionId,
                                                                   @RequestBody QuestionRequest request) {
        QuestionDto dto = questionService.findById(questionId);
        if (request.getCourseId() != null) {
            dto.setCourseId(request.getCourseId());
        }
        if (request.getLessonId() != null) {
            dto.setLessonId(request.getLessonId());
        }
        if (request.getTitle() != null) {
            dto.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            dto.setContent(request.getContent());
        }
        return ResponseConfig.success(questionService.saveObject(dto));
    }

    @PermitAll
    @GetMapping("/courses/{courseId}/questions")
    public ResponseEntity<ResponseDto<Page<QuestionDto>>> listByCourse(@PathVariable Long courseId,
                                                                       @RequestParam(required = false) Long lessonId,
                                                                       @RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        return ResponseConfig.success(questionService.listByCourse(courseId, lessonId, pageable));
    }

    @GetMapping("/admin/questions")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<Page<QuestionDetailResponse>>> listForManagement(@RequestParam(defaultValue = "0") int page,
                                                                                        @RequestParam(defaultValue = "10") int size) {
        Long userId = currentUserId();
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        return ResponseConfig.success(questionService.listForManagement(userId, pageable));
    }

    @PermitAll
    @GetMapping("/questions/{questionId}")
    public ResponseEntity<ResponseDto<QuestionDto>> getQuestion(@PathVariable Long questionId) {
        return ResponseConfig.success(questionService.findActiveById(questionId));
    }

    @PostMapping("/questions/{questionId}/answers")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<AnswerDto>> createAnswer(@PathVariable Long questionId,
                                                               @RequestBody @Valid AnswerRequest request) {
        Long userId = currentUserId();
        AnswerDto dto = new AnswerDto();
        dto.setQuestionId(questionId);
        dto.setUserId(userId);
        dto.setContent(request.getContent());
        dto.setIsAccepted(Boolean.FALSE);
        return ResponseConfig.success(answerService.saveObject(dto));
    }

    @PermitAll
    @GetMapping("/questions/{questionId}/answers")
    public ResponseEntity<ResponseDto<List<AnswerDto>>> listAnswers(@PathVariable Long questionId) {
        return ResponseConfig.success(answerService.findByQuestion(questionId));
    }

    @PatchMapping("/questions/{questionId}/answers/{answerId}/accept")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<AnswerDto>> acceptAnswer(@PathVariable Long questionId,
                                                               @PathVariable Long answerId) {
        AnswerDto answer = answerService.findActiveById(answerId);
        if (answer == null || !questionId.equals(answer.getQuestionId())) {
            throw new com.theanh.common.exception.BusinessException("data.not_found");
        }
        // reset other answers to not accepted
        List<AnswerDto> answers = answerService.findByQuestion(questionId);
        answers.forEach(a -> {
            if (Boolean.TRUE.equals(a.getIsAccepted()) && !a.getId().equals(answerId)) {
                a.setIsAccepted(Boolean.FALSE);
                answerService.saveObject(a);
            }
        });
        answer.setIsAccepted(Boolean.TRUE);
        return ResponseConfig.success(answerService.saveObject(answer));
    }

    @PostMapping("/questions/{questionId}/vote")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<QuestionVoteDto>> vote(@PathVariable Long questionId,
                                                             @RequestBody @Valid QuestionVoteRequest request) {
        Long userId = currentUserId();
        return ResponseConfig.success(questionVoteService.upsertVote(userId, questionId, request.getVoteType()));
    }

    @DeleteMapping("/admin/questions/{questionId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<Void>> deleteQuestion(@PathVariable Long questionId) {
        Long userId = currentUserId();
        QuestionDto question = questionService.findActiveById(questionId);
        if (question == null) {
            throw new BusinessException("data.not_found");
        }
        ensureCanManageCourse(userId, question.getCourseId());
        questionService.deleteById(questionId);
        return ResponseConfig.success(null);
    }

    @DeleteMapping("/admin/answers/{answerId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<Void>> deleteAnswer(@PathVariable Long answerId) {
        Long userId = currentUserId();
        AnswerDto answer = answerService.findActiveById(answerId);
        if (answer == null) {
            throw new BusinessException("data.not_found");
        }
        QuestionDto question = questionService.findActiveById(answer.getQuestionId());
        if (question == null) {
            throw new BusinessException("data.not_found");
        }
        ensureCanManageCourse(userId, question.getCourseId());
        answerService.deleteById(answerId);
        return ResponseConfig.success(null);
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(auth.getPrincipal().toString());
    }

    private void ensureCanManageCourse(Long userId, Long courseId) {
        if (userId == null || courseId == null) {
            throw new BusinessException("auth.forbidden");
        }
        boolean isAdmin = userService.findRoles(userId).contains(RoleName.ADMIN.name());
        if (!isAdmin && !courseInstructorService.isInstructorOfCourse(userId, courseId)) {
            throw new BusinessException("auth.forbidden");
        }
    }
}
