package com.theanh.lms.controller;

import com.theanh.common.dto.ResponseDto;
import com.theanh.common.util.ResponseConfig;
import com.theanh.lms.dto.EnrollmentDto;
import com.theanh.lms.service.EnrollmentService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/courses/{courseId}/enroll")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<EnrollmentDto>> enroll(@PathVariable @NotNull Long courseId) {
        Long userId = currentUserId();
        return ResponseConfig.success(enrollmentService.enroll(userId, courseId));
    }

    @GetMapping("/me/enrollments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<Page<EnrollmentDto>>> myEnrollments(@RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "10") int size) {
        Long userId = currentUserId();
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        return ResponseConfig.success(enrollmentService.findByUser(userId, pageable));
    }

    @GetMapping("/courses/{courseId}/enrollment-status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<Boolean>> enrollmentStatus(@PathVariable @NotNull Long courseId) {
        Long userId = currentUserId();
        return ResponseConfig.success(enrollmentService.isEnrolled(userId, courseId));
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(auth.getPrincipal().toString());
    }
}
