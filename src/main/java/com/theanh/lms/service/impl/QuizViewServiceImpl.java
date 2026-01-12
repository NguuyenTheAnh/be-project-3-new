package com.theanh.lms.service.impl;

import com.theanh.common.exception.BusinessException;
import com.theanh.lms.dto.QuizAnswerDto;
import com.theanh.lms.dto.QuizAnswerResponse;
import com.theanh.lms.dto.QuizQuestionDto;
import com.theanh.lms.dto.QuizQuestionResponse;
import com.theanh.lms.dto.QuizViewResponse;
import com.theanh.lms.dto.QuizDto;
import com.theanh.lms.enums.RoleName;
import com.theanh.lms.service.AccessControlService;
import com.theanh.lms.service.CourseInstructorService;
import com.theanh.lms.service.CourseLessonService;
import com.theanh.lms.service.QuizAnswerService;
import com.theanh.lms.service.QuizQuestionService;
import com.theanh.lms.service.QuizService;
import com.theanh.lms.service.QuizViewService;
import com.theanh.lms.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class QuizViewServiceImpl implements QuizViewService {

    private final QuizService quizService;
    private final QuizQuestionService quizQuestionService;
    private final QuizAnswerService quizAnswerService;
    private final AccessControlService accessControlService;
    private final CourseLessonService courseLessonService;
    private final CourseInstructorService courseInstructorService;
    private final UserService userService;

    public QuizViewServiceImpl(QuizService quizService,
                               QuizQuestionService quizQuestionService,
                               QuizAnswerService quizAnswerService,
                               AccessControlService accessControlService,
                               CourseLessonService courseLessonService,
                               CourseInstructorService courseInstructorService,
                               UserService userService) {
        this.quizService = quizService;
        this.quizQuestionService = quizQuestionService;
        this.quizAnswerService = quizAnswerService;
        this.accessControlService = accessControlService;
        this.courseLessonService = courseLessonService;
        this.courseInstructorService = courseInstructorService;
        this.userService = userService;
    }

    @Override
    public QuizViewResponse getQuizForLesson(Long userId, Long lessonId) {
        if (!Boolean.TRUE.equals(accessControlService.canViewLesson(userId, null, lessonId))) {
            throw new BusinessException("auth.forbidden");
        }
        return buildResponse(lessonId, false);
    }

    @Override
    public QuizViewResponse getQuizForLessonAsInstructor(Long userId, Long lessonId) {
        var mapping = courseLessonService.findActiveByLessonId(lessonId);
        Long courseId = mapping != null ? mapping.getCourseId() : null;
        if (courseId == null) {
            throw new BusinessException("data.not_found");
        }
        boolean isAdmin = userId != null && userService.findRoles(userId).contains(RoleName.ADMIN.name());
        if (!isAdmin && !courseInstructorService.isInstructorOfCourse(userId, courseId)) {
            throw new BusinessException("auth.forbidden");
        }
        return buildResponse(lessonId, true);
    }

    private QuizViewResponse buildResponse(Long lessonId, boolean includeCorrect) {
        QuizDto quiz = quizService.findActiveByLesson(lessonId);
        if (quiz == null) {
            throw new BusinessException("data.not_found");
        }
        QuizViewResponse resp = new QuizViewResponse();
        resp.setId(quiz.getId());
        resp.setLessonId(quiz.getLessonId());
        resp.setTitle(quiz.getTitle());
        resp.setTimeLimitSeconds(quiz.getTimeLimitSeconds());
        resp.setPassScore(quiz.getPassScore());
        resp.setMaxAttempts(quiz.getMaxAttempts());
        resp.setShuffleQuestions(quiz.getShuffleQuestions());
        List<QuizQuestionDto> questions = quizQuestionService.findByQuiz(quiz.getId());
        if (CollectionUtils.isEmpty(questions)) {
            resp.setQuestions(List.of());
            return resp;
        }
        List<QuizQuestionResponse> questionResponses = questions.stream().map(q -> {
            QuizQuestionResponse qr = new QuizQuestionResponse();
            qr.setId(q.getId());
            qr.setQuestionText(q.getQuestionText());
            qr.setQuestionType(q.getQuestionType());
            qr.setPosition(q.getPosition());
            qr.setPoints(q.getPoints());
            qr.setExplanation(q.getExplanation());
            List<QuizAnswerDto> answers = quizAnswerService.findByQuestion(q.getId());
            List<QuizAnswerResponse> answerResponses = answers.stream().map(a -> {
                QuizAnswerResponse ar = new QuizAnswerResponse();
                ar.setId(a.getId());
                ar.setAnswerText(a.getAnswerText());
                ar.setPosition(a.getPosition());
                ar.setIsCorrect(includeCorrect ? a.getIsCorrect() : null);
                return ar;
            }).toList();
            qr.setAnswers(answerResponses);
            return qr;
        }).toList();
        resp.setQuestions(questionResponses);
        return resp;
    }
}
