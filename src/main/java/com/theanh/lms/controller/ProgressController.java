package com.theanh.lms.controller;

import com.theanh.common.dto.ResponseDto;
import com.theanh.common.util.ResponseConfig;
import com.theanh.lms.dto.ProgressDto;
import com.theanh.lms.dto.request.ProgressUpdateRequest;
import com.theanh.lms.service.ProgressService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @PatchMapping("/courses/{courseId}/lessons/{lessonId}/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<ProgressDto>> updateProgress(@PathVariable @NotNull Long courseId,
                                                                   @PathVariable @NotNull Long lessonId,
                                                                   @RequestBody @Valid ProgressUpdateRequest request) {
        Long userId = currentUserId();
        ProgressDto dto = progressService.updateProgress(userId, courseId, lessonId,
                request.getLastPositionSeconds(), request.getCompleted());
        return ResponseConfig.success(dto);
    }

    @GetMapping("/courses/{courseId}/lessons/{lessonId}/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<ProgressDto>> getProgress(@PathVariable @NotNull Long courseId,
                                                                @PathVariable @NotNull Long lessonId) {
        Long userId = currentUserId();
        ProgressDto dto = progressService.getProgress(userId, courseId, lessonId);
        return ResponseConfig.success(dto);
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(auth.getPrincipal().toString());
    }
}
