package com.theanh.lms.service;

import com.theanh.common.base.BaseService;
import com.theanh.lms.dto.ContentReportDto;
import com.theanh.lms.dto.response.ContentReportResponse;
import com.theanh.lms.entity.ContentReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContentReportService extends BaseService<ContentReport, ContentReportDto, Long> {

    ContentReportDto report(Long userId, String targetType, Long targetId, String reason);

    Page<ContentReportResponse> listAll(Pageable pageable);

    ContentReportDto updateStatus(Long reportId, String status);

    Page<ContentReportDto> listMyReports(Long userId, Pageable pageable);

    ContentReportDto getMyReport(Long userId, Long reportId);

    ContentReportDto updateMyReport(Long userId, Long reportId, String reason);
}
