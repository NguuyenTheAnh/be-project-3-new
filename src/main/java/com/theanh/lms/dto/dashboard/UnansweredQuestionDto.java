package com.theanh.lms.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnansweredQuestionDto {
    private Long questionId;
    private Long courseId;
    private String courseTitle;
    private Long lessonId;
    private String lessonTitle;
    private String questionTitle;
    private String questionContent;
    private Long askerUserId;
    private String askerFullName;
    private LocalDateTime askedAt;
}
