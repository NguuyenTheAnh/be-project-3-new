package com.theanh.lms.dto;

import lombok.Data;

import java.util.List;

@Data
public class CourseSectionResponse {
    private Long id;
    private String title;
    private Integer position;
    private List<LessonPreviewDto> lessons;
}
