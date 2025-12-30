package com.theanh.lms.controller;

import com.theanh.common.dto.ResponseDto;
import com.theanh.common.util.ResponseConfig;
import com.theanh.lms.dto.UploadedFileDto;
import com.theanh.lms.dto.request.PresignPutRequest;
import com.theanh.lms.dto.response.PresignUrlResponse;
import com.theanh.lms.service.FileStorageService;
import com.theanh.lms.service.PresignService;
import com.theanh.lms.service.UploadedFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final UploadedFileService uploadedFileService;
    private final FileStorageService fileStorageService;
    private final PresignService presignService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ResponseDto<UploadedFileDto>> upload(@RequestParam("file") MultipartFile file,
                                                               @RequestParam(name = "public", defaultValue = "false") boolean isPublic) {
        UploadedFileDto dto = uploadedFileService.store(file, isPublic);
        // Luôn trả accessUrl là presigned GET (hoặc public URL nếu file public)
        PresignUrlResponse getUrl = presignService.generateGetUrl(dto);
        dto.setAccessUrl(getUrl.getUrl());
        return ResponseConfig.success(dto);
    }

    @GetMapping("/meta/{id}")
    public ResponseEntity<ResponseDto<UploadedFileDto>> getMetadata(@PathVariable Long id) {
        UploadedFileDto dto = uploadedFileService.findById(id);
        PresignUrlResponse getUrl = presignService.generateGetUrl(dto);
        dto.setAccessUrl(getUrl.getUrl());
        return ResponseConfig.success(dto);
    }

    @PostMapping("/presign/put")
    public ResponseEntity<ResponseDto<PresignUrlResponse>> presignPut(@org.springframework.web.bind.annotation.RequestBody @jakarta.validation.Valid PresignPutRequest request) {
        return ResponseConfig.success(presignService.generatePutUrl(request));
    }

    @GetMapping("/{id}/url")
    public ResponseEntity<ResponseDto<PresignUrlResponse>> presignGet(@PathVariable Long id) {
        return ResponseConfig.success(presignService.generateGetUrl(id));
    }
}
