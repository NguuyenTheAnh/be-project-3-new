package com.theanh.lms.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CourseListItemResponse {
    private Long id;
    private String title;
    private String slug;
    private String shortDescription;
    private String level;
    private String language;
    private Long priceCents;
    private BigDecimal ratingAvg;
    private Integer ratingCount;
    private CategoryDto category;
    private List<TagDto> tags;
    private UploadedFileDto thumbnail;
}
