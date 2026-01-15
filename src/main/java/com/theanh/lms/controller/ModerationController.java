package com.theanh.lms.controller;

import com.theanh.common.dto.ResponseDto;
import com.theanh.common.util.ResponseConfig;
import com.theanh.lms.dto.ContentReportDto;
import com.theanh.lms.dto.ModerationActionDto;
import com.theanh.lms.dto.request.ContentReportRequest;
import com.theanh.lms.dto.request.ContentReportUpdateRequest;
import com.theanh.lms.dto.request.ModerationActionRequest;
import com.theanh.lms.dto.response.ContentReportResponse;
import com.theanh.lms.service.ContentReportService;
import com.theanh.lms.service.ModerationActionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ModerationController {

    private final ContentReportService contentReportService;
    private final ModerationActionService moderationActionService;

    @PostMapping("/reports")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<ContentReportDto>> report(@RequestBody @Valid ContentReportRequest request) {
        Long userId = currentUserId();
        ContentReportDto dto = contentReportService.report(userId, request.getTargetType(), request.getTargetId(), request.getReason());
        return ResponseConfig.success(dto);
    }

    @GetMapping("/admin/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto<Page<ContentReportResponse>>> listReports(@RequestParam(defaultValue = "0") int page,
                                                                                @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        return ResponseConfig.success(contentReportService.listAll(pageable));
    }

    @GetMapping("/reports/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<Page<ContentReportDto>>> listMyReports(@RequestParam(defaultValue = "0") int page,
                                                                             @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        return ResponseConfig.success(contentReportService.listMyReports(currentUserId(), pageable));
    }

    @GetMapping("/reports/{reportId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<ContentReportDto>> getMyReport(@PathVariable @NotNull Long reportId) {
        return ResponseConfig.success(contentReportService.getMyReport(currentUserId(), reportId));
    }

    @PutMapping("/reports/{reportId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<ContentReportDto>> updateMyReport(@PathVariable @NotNull Long reportId,
                                                                        @RequestBody ContentReportUpdateRequest request) {
        return ResponseConfig.success(contentReportService.updateMyReport(currentUserId(), reportId, request.getReason()));
    }

    @PatchMapping("/admin/reports/{reportId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto<ContentReportDto>> updateStatus(@PathVariable @NotNull Long reportId,
                                                                      @RequestParam String status) {
        return ResponseConfig.success(contentReportService.updateStatus(reportId, status));
    }

    @PostMapping("/admin/reports/{reportId}/actions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto<ModerationActionDto>> addAction(@PathVariable @NotNull Long reportId,
                                                                      @RequestBody @Valid ModerationActionRequest request) {
        ModerationActionDto dto = new ModerationActionDto();
        dto.setReportId(reportId);
        dto.setModeratorUserId(currentUserId());
        dto.setAction(request.getAction());
        dto.setNotes(request.getNotes());
        return ResponseConfig.success(moderationActionService.saveObject(dto));
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(auth.getPrincipal().toString());
    }
}
