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
public class QuestionDto extends BaseDto {
    private Long courseId;
    private Long lessonId;
    private Long userId;
    private String title;
    private String content;
    private String status;
}
