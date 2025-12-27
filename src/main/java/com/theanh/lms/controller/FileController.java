package com.theanh.lms.controller;

import com.theanh.common.dto.ResponseDto;
import com.theanh.common.util.ResponseConfig;
import com.theanh.lms.dto.UploadedFileDto;
import com.theanh.lms.service.FileStorageService;
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

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ResponseDto<UploadedFileDto>> upload(@RequestParam("file") MultipartFile file,
                                                               @RequestParam(name = "public", defaultValue = "false") boolean isPublic) {
        UploadedFileDto dto = uploadedFileService.store(file, isPublic);
        return ResponseConfig.success(dto);
    }

    @GetMapping("/meta/{id}")
    public ResponseEntity<ResponseDto<UploadedFileDto>> getMetadata(@PathVariable Long id) {
        UploadedFileDto dto = uploadedFileService.findById(id);
        dto.setAccessUrl(fileStorageService.buildPublicUrl(dto.getObjectKey()));
        return ResponseConfig.success(dto);
    }
}
