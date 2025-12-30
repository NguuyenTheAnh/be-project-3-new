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
public class LessonDto extends BaseDto {
    private String title;
    private String lessonType;
    private String contentText;
    private Long videoFileId;
    private Integer durationSeconds;
    private Boolean isFreePreview;
}
