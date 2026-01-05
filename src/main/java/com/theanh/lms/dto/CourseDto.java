package com.theanh.lms.dto;

import com.theanh.common.base.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CourseDto extends BaseDto {
    private Long categoryId;
    private Long creatorUserId;
    private String title;
    private String slug;
    private String shortDescription;
    private String description;
    private String level;
    private String language;
    private Long thumbnailFileId;
    private Long introVideoFileId;
    private String status;
    private LocalDateTime publishedAt;
    private Long priceCents;
    private BigDecimal ratingAvg;
    private Integer ratingCount;
}
