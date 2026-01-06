package com.theanh.lms.controller;

import com.theanh.common.dto.ResponseDto;
import com.theanh.common.exception.BusinessException;
import com.theanh.common.util.ResponseConfig;
import com.theanh.lms.dto.CourseLessonDto;
import com.theanh.lms.dto.DocumentResponse;
import com.theanh.lms.dto.LessonDetailResponse;
import com.theanh.lms.dto.LessonDocumentDto;
import com.theanh.lms.dto.LessonDto;
import com.theanh.lms.dto.UploadedFileDto;
import com.theanh.lms.enums.CourseStatus;
import com.theanh.lms.service.CourseLessonService;
import com.theanh.lms.service.CourseService;
import com.theanh.lms.service.EnrollmentService;
import com.theanh.lms.service.LessonDocumentService;
import com.theanh.lms.service.LessonService;
import com.theanh.lms.service.UploadedFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;
    private final CourseLessonService courseLessonService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final UploadedFileService uploadedFileService;
    private final LessonDocumentService lessonDocumentService;

    @GetMapping("/{lessonId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseDto<LessonDetailResponse>> getLesson(@PathVariable Long lessonId) {
        Long userId = currentUserId();
        LessonDto lesson = lessonService.findById(lessonId);
        if (lesson == null) {
            throw new BusinessException("data.not_found");
        }
        CourseLessonDto mapping = courseLessonService.findActiveByLessonId(lessonId);
        if (mapping == null) {
            throw new BusinessException("data.not_found");
        }
        // check course published or preview/enrolled
        var course = courseService.findActiveById(mapping.getCourseId());
        if (course == null || CourseStatus.ARCHIVED.name().equals(course.getStatus())) {
            throw new BusinessException("data.not_found");
        }
        boolean previewAllowed = Boolean.TRUE.equals(lesson.getIsFreePreview()) || Boolean.TRUE.equals(mapping.getIsPreview());
        boolean enrolled = enrollmentService.isEnrolled(userId, mapping.getCourseId());
        if (!previewAllowed && !enrolled && !CourseStatus.PUBLISHED.name().equals(course.getStatus())) {
            throw new BusinessException("auth.forbidden");
        }
        if (!previewAllowed && !enrolled) {
            throw new BusinessException("auth.forbidden");
        }
        LessonDetailResponse resp = new LessonDetailResponse();
        resp.setId(lesson.getId());
        resp.setCourseId(mapping.getCourseId());
        resp.setCourseSectionId(mapping.getCourseSectionId());
        resp.setTitle(lesson.getTitle());
        resp.setLessonType(lesson.getLessonType());
        resp.setContentText(lesson.getContentText());
        resp.setDurationSeconds(lesson.getDurationSeconds());
        resp.setIsFreePreview(lesson.getIsFreePreview());
        resp.setIsPreview(mapping.getIsPreview());
        if (lesson.getVideoFileId() != null) {
            try {
                UploadedFileDto file = uploadedFileService.findById(lesson.getVideoFileId());
                resp.setVideoFile(file);
            } catch (Exception ignored) {
            }
        }
        resp.setDocuments(buildLessonDocuments(lessonId));
        return ResponseConfig.success(resp);
    }

    private List<DocumentResponse> buildLessonDocuments(Long lessonId) {
        List<LessonDocumentDto> docs = lessonDocumentService.findByLessonId(lessonId);
        if (docs == null || docs.isEmpty()) {
            return List.of();
        }
        return docs.stream().map(doc -> {
            DocumentResponse dr = new DocumentResponse();
            dr.setId(doc.getId());
            dr.setTitle(doc.getTitle());
            dr.setPosition(doc.getPosition());
            if (doc.getUploadedFileId() != null) {
                try {
                    dr.setFile(uploadedFileService.findById(doc.getUploadedFileId()));
                } catch (Exception ignored) {
                }
            }
            return dr;
        }).toList();
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new BusinessException("auth.unauthorized");
        }
        return Long.parseLong(auth.getPrincipal().toString());
    }
}
