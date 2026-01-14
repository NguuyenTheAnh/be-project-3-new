package com.theanh.lms.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuestionDetailResponse {
    private Long id;
    private CourseDto course;
    private LessonDto lesson;
    private Long userId;
    private String createdUser;
    private String title;
    private String content;
    private String status;
    private List<AnswerAdminResponse> answers;
}
