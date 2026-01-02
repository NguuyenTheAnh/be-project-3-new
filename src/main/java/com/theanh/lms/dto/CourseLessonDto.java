package com.theanh.lms.dto;

import com.theanh.common.base.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CourseLessonDto extends BaseDto {
    private Long courseId;
    private Long courseSectionId;
    private Long lessonId;
    private Integer position;
    private Boolean isPreview;
}
