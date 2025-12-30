package com.theanh.lms.dto;

import com.theanh.common.base.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CourseInstructorDto extends BaseDto {
    private Long courseId;
    private Long userId;
    private String instructorRole;
    private BigDecimal revenueShare;
}
