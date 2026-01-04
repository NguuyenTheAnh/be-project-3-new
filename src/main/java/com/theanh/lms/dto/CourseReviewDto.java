package com.theanh.lms.dto;

import com.theanh.common.base.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CourseReviewDto extends BaseDto {
    private Long courseId;
    private Long userId;
    private Integer rating;
    private String title;
    private String content;
    private String status;
    private Long moderatedByUserId;
    private LocalDateTime moderatedAt;
}
