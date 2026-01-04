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
public class ProgressDto extends BaseDto {
    private Long enrollmentId;
    private Long lessonId;
    private Boolean completed;
    private LocalDateTime completedAt;
    private Integer lastPositionSeconds;
    private LocalDateTime lastAccessedAt;
}
