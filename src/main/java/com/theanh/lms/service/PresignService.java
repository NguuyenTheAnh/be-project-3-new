package com.theanh.lms.service;

import com.theanh.lms.dto.UploadedFileDto;
import com.theanh.lms.dto.request.PresignPutRequest;
import com.theanh.lms.dto.response.PresignUrlResponse;

public interface PresignService {

    PresignUrlResponse generatePutUrl(PresignPutRequest request);

    PresignUrlResponse generateGetUrl(Long fileId);

    PresignUrlResponse generateGetUrl(UploadedFileDto file);
}
