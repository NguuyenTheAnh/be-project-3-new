package com.theanh.lms.service;

import com.theanh.lms.dto.QuizViewResponse;

public interface QuizViewService {

    QuizViewResponse getQuizForLesson(Long userId, Long lessonId);

    QuizViewResponse getQuizForLessonAsInstructor(Long userId, Long lessonId);
}
