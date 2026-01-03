package com.theanh.lms.service;

import com.theanh.lms.dto.CourseDetailResponse;
import com.theanh.lms.dto.request.CourseCreateRequest;
import com.theanh.lms.dto.request.CourseInstructorRequest;
import com.theanh.lms.dto.request.CourseStatusUpdateRequest;
import com.theanh.lms.dto.request.CourseUpdateRequest;
import com.theanh.lms.dto.request.CourseSectionRequest;
import com.theanh.lms.dto.request.CourseDocumentRequest;
import com.theanh.lms.dto.request.LessonCreateRequest;
import com.theanh.lms.dto.request.LessonUpdateRequest;
import com.theanh.lms.dto.request.LessonDocumentRequest;
import com.theanh.lms.dto.request.ReorderRequest;

import java.util.List;

public interface CourseAuthorService {

    CourseDetailResponse createCourse(Long creatorUserId, CourseCreateRequest request);

    CourseDetailResponse updateCourse(Long courseId, CourseUpdateRequest request);

    CourseDetailResponse updateStatus(Long courseId, CourseStatusUpdateRequest request);

    CourseDetailResponse updateTags(Long courseId, List<Long> tagIds);

    CourseDetailResponse updateInstructors(Long courseId, List<CourseInstructorRequest> instructors);

    CourseDetailResponse addSection(Long courseId, CourseSectionRequest request);

    CourseDetailResponse updateSection(Long courseId, Long sectionId, CourseSectionRequest request);

    CourseDetailResponse deleteSection(Long courseId, Long sectionId);

    CourseDetailResponse addLesson(Long courseId, LessonCreateRequest request);

    CourseDetailResponse updateLesson(Long courseId, Long lessonId, LessonUpdateRequest request);

    CourseDetailResponse deleteLesson(Long courseId, Long lessonId);

    CourseDetailResponse reorderSections(Long courseId, ReorderRequest request);

    CourseDetailResponse reorderLessons(Long courseId, ReorderRequest request);

    CourseDetailResponse addCourseDocument(Long courseId, CourseDocumentRequest request);

    CourseDetailResponse updateCourseDocument(Long courseId, Long documentId, CourseDocumentRequest request);

    CourseDetailResponse deleteCourseDocument(Long courseId, Long documentId);

    CourseDetailResponse addLessonDocument(Long courseId, Long lessonId, LessonDocumentRequest request);

    CourseDetailResponse updateLessonDocument(Long courseId, Long lessonId, Long documentId, LessonDocumentRequest request);

    CourseDetailResponse deleteLessonDocument(Long courseId, Long lessonId, Long documentId);
}
