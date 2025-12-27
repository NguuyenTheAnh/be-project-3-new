package com.theanh.lms.controller;

import com.theanh.lms.dto.UploadedFileDto;
import com.theanh.lms.service.FileStorageService;
import com.theanh.lms.service.UploadedFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileServeController {

    private final UploadedFileService uploadedFileService;
    private final FileStorageService fileStorageService;

    @GetMapping("/{objectKey:.+}")
    public ResponseEntity<Resource> serve(@PathVariable String objectKey) {
        UploadedFileDto metadata = uploadedFileService.getByObjectKey(objectKey);
        Resource resource = fileStorageService.loadAsResource(objectKey);
        String filename = Optional.ofNullable(metadata.getOriginalName()).filter(s -> !s.isBlank()).orElse(objectKey);
        ContentDisposition disposition = ContentDisposition.inline()
                .filename(filename, StandardCharsets.UTF_8)
                .build();
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (metadata.getContentType() != null) {
            try {
                mediaType = MediaType.parseMediaType(metadata.getContentType());
            } catch (IllegalArgumentException ignored) {
                // fallback to application/octet-stream
            }
        }
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(resource);
    }
}
