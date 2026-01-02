package com.theanh.lms.controller;

import com.theanh.common.dto.ResponseDto;
import com.theanh.common.util.ResponseConfig;
import com.theanh.lms.dto.UploadedFileDto;
import com.theanh.lms.dto.request.FileAbortRequest;
import com.theanh.lms.dto.request.FileCompleteRequest;
import com.theanh.lms.dto.request.PresignPutRequest;
import com.theanh.lms.dto.response.PresignUrlResponse;
import com.theanh.lms.service.PresignService;
import com.theanh.lms.service.UploadedFileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final UploadedFileService uploadedFileService;
    private final PresignService presignService;

    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<UploadedFileDto>> upload(@RequestParam("file") MultipartFile file,
                                                               @RequestParam(name = "public", defaultValue = "false") boolean isPublic) {
        UploadedFileDto dto = uploadedFileService.store(file, isPublic);
        PresignUrlResponse getUrl = presignService.generateGetUrl(dto);
        dto.setAccessUrl(getUrl.getUrl());
        return ResponseConfig.success(dto);
    }

    @GetMapping("/meta/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<UploadedFileDto>> getMetadata(@PathVariable Long id) {
        UploadedFileDto dto = uploadedFileService.findById(id);
        PresignUrlResponse getUrl = presignService.generateGetUrl(dto);
        dto.setAccessUrl(getUrl.getUrl());
        return ResponseConfig.success(dto);
    }

    @PostMapping("/presign/put")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<PresignUrlResponse>> presignPut(@RequestBody @Valid PresignPutRequest request) {
        return ResponseConfig.success(presignService.generatePutUrl(request));
    }

    @PostMapping("/complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<UploadedFileDto>> complete(@RequestBody @Valid FileCompleteRequest request) {
        UploadedFileDto dto = uploadedFileService.markReady(request.getFileId());
        PresignUrlResponse getUrl = presignService.generateGetUrl(dto);
        dto.setAccessUrl(getUrl.getUrl());
        return ResponseConfig.success(dto);
    }

    @PostMapping("/abort")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<UploadedFileDto>> abort(@RequestBody @Valid FileAbortRequest request) {
        return ResponseConfig.success(uploadedFileService.abort(request.getFileId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<UploadedFileDto>> delete(@PathVariable Long id) {
        return ResponseConfig.success(uploadedFileService.abort(id));
    }

    @GetMapping("/{id}/url")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<PresignUrlResponse>> presignGet(@PathVariable Long id) {
        return ResponseConfig.success(presignService.generateGetUrl(id));
    }
}