package com.theanh.lms.controller;

import com.theanh.common.dto.ResponseDto;
import com.theanh.common.util.ResponseConfig;
import com.theanh.lms.dto.CourseDetailResponse;
import com.theanh.lms.dto.InstructorCourseListResponse;
import com.theanh.lms.dto.request.*;
import com.theanh.lms.service.CourseAuthorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RestController
@RequestMapping("/instructor/courses")
@RequiredArgsConstructor
public class CourseAuthorController {

    private final CourseAuthorService courseAuthorService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<Page<InstructorCourseListResponse>>> listInstructorCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = currentUserId();
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        return ResponseConfig.success(courseAuthorService.listInstructorCourses(userId, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<CourseDetailResponse>> create(@Valid @RequestBody CourseCreateRequest request) {
        Long userId = currentUserId();
        return ResponseConfig.success(courseAuthorService.createCourse(userId, request));
    }

    @PutMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<CourseDetailResponse>> update(@PathVariable Long courseId,
                                                                    @Valid @RequestBody CourseUpdateRequest request) {
        return ResponseConfig.success(courseAuthorService.updateCourse(courseId, request));
    }

    @PatchMapping("/{courseId}/status")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<CourseDetailResponse>> updateStatus(@PathVariable Long courseId,
                                                                          @Valid @RequestBody CourseStatusUpdateRequest request) {
        return ResponseConfig.success(courseAuthorService.updateStatus(courseId, request));
    }

    @PutMapping("/{courseId}/tags")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<CourseDetailResponse>> updateTags(@PathVariable Long courseId,
                                                                        @RequestBody List<Long> tagIds) {
        return ResponseConfig.success(courseAuthorService.updateTags(courseId, tagIds));
    }

    @PostMapping("/{courseId}/publish")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<CourseDetailResponse>> publish(@PathVariable Long courseId) {
        return ResponseConfig.success(courseAuthorService.publishCourse(courseId));
    }

    @PutMapping("/{courseId}/instructors")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<CourseDetailResponse>> updateInstructors(@PathVariable Long courseId,
                                                                               @RequestBody List<CourseInstructorRequest> instructors) {
        return ResponseConfig.success(courseAuthorService.updateInstructors(courseId, instructors));
    }

    @PostMapping("/{courseId}/sections")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<CourseDetailResponse>> addSection(@PathVariable Long courseId,
                                                                        @Valid @RequestBody CourseSectionRequest request) {
        return ResponseConfig.success(courseAuthorService.addSection(courseId, request));
    }

    @PutMapping("/{courseId}/sections/{sectionId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<CourseDetailResponse>> updateSection(@PathVariable Long courseId,
                                                                           @PathVariable Long sectionId,
                                                                           @Valid @RequestBody CourseSectionRequest request) {
        return ResponseConfig.success(courseAuthorService.updateSection(courseId, sectionId, request));
    }

    @DeleteMapping("/{courseId}/sections/{sectionId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<CourseDetailResponse>> deleteSection(@PathVariable Long courseId,
                                                                           @PathVariable Long sectionId) {
        return ResponseConfig.success(courseAuthorService.deleteSection(courseId, sectionId));
    }

    @PostMapping("/{courseId}/lessons")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<CourseDetailResponse>> addLesson(@PathVariable Long courseId,
                                                                       @Valid @RequestBody LessonCreateRequest request) {
        return ResponseConfig.success(courseAuthorService.addLesson(courseId, request));
    }

    @PutMapping("/{courseId}/lessons/{lessonId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<CourseDetailResponse>> updateLesson(@PathVariable Long courseId,
                                                                          @PathVariable Long lessonId,
                                                                          @Valid @RequestBody LessonUpdateRequest request) {
        return ResponseConfig.success(courseAuthorService.updateLesson(courseId, lessonId, request));
    }

    @DeleteMapping("/{courseId}/lessons/{lessonId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<CourseDetailResponse>> deleteLesson(@PathVariable Long courseId,
                                                                          @PathVariable Long lessonId) {
        return ResponseConfig.success(courseAuthorService.deleteLesson(courseId, lessonId));
    }

    @PatchMapping("/{courseId}/sections/reorder")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<CourseDetailResponse>> reorderSections(@PathVariable Long courseId,
                                                                             @Valid @RequestBody ReorderRequest request) {
        return ResponseConfig.success(courseAuthorService.reorderSections(courseId, request));
    }

    @PatchMapping("/{courseId}/lessons/reorder")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<CourseDetailResponse>> reorderLessons(@PathVariable Long courseId,
                                                                            @Valid @RequestBody ReorderRequest request) {
        return ResponseConfig.success(courseAuthorService.reorderLessons(courseId, request));
    }

    @PostMapping("/{courseId}/documents")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<CourseDetailResponse>> addCourseDocument(@PathVariable Long courseId,
                                                                               @Valid @RequestBody CourseDocumentRequest request) {
        return ResponseConfig.success(courseAuthorService.addCourseDocument(courseId, request));
    }

    @PutMapping("/{courseId}/documents/{documentId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<CourseDetailResponse>> updateCourseDocument(@PathVariable Long courseId,
                                                                                  @PathVariable Long documentId,
                                                                                  @Valid @RequestBody CourseDocumentRequest request) {
        return ResponseConfig.success(courseAuthorService.updateCourseDocument(courseId, documentId, request));
    }

    @DeleteMapping("/{courseId}/documents/{documentId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<CourseDetailResponse>> deleteCourseDocument(@PathVariable Long courseId,
                                                                                  @PathVariable Long documentId) {
        return ResponseConfig.success(courseAuthorService.deleteCourseDocument(courseId, documentId));
    }

    @PostMapping("/{courseId}/lessons/{lessonId}/documents")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<CourseDetailResponse>> addLessonDocument(@PathVariable Long courseId,
                                                                               @PathVariable Long lessonId,
                                                                               @Valid @RequestBody LessonDocumentRequest request) {
        return ResponseConfig.success(courseAuthorService.addLessonDocument(courseId, lessonId, request));
    }

    @PutMapping("/{courseId}/lessons/{lessonId}/documents/{documentId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<CourseDetailResponse>> updateLessonDocument(@PathVariable Long courseId,
                                                                                  @PathVariable Long lessonId,
                                                                                  @PathVariable Long documentId,
                                                                                  @Valid @RequestBody LessonDocumentRequest request) {
        return ResponseConfig.success(courseAuthorService.updateLessonDocument(courseId, lessonId, documentId, request));
    }

    @DeleteMapping("/{courseId}/lessons/{lessonId}/documents/{documentId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ResponseDto<CourseDetailResponse>> deleteLessonDocument(@PathVariable Long courseId,
                                                                                  @PathVariable Long lessonId,
                                                                                  @PathVariable Long documentId) {
        return ResponseConfig.success(courseAuthorService.deleteLessonDocument(courseId, lessonId, documentId));
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(auth.getPrincipal().toString());
    }
}
