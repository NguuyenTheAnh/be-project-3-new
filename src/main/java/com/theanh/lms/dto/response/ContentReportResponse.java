package com.theanh.lms.dto.response;

import com.theanh.lms.dto.ContentReportDto;
import lombok.Data;

@Data
public class ContentReportResponse extends ContentReportDto {
    private String reporterName;
}
