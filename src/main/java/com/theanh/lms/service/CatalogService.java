package com.theanh.lms.service;

import com.theanh.lms.dto.CategoryDto;
import com.theanh.lms.dto.CourseDetailResponse;
import com.theanh.lms.dto.CourseListItemResponse;
import com.theanh.lms.dto.TagDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CatalogService {

    List<CategoryDto> listCategories();

    List<TagDto> listTags();

    Page<CourseListItemResponse> searchCourses(String keyword,
                                               Long categoryId,
                                               List<Long> tagIds,
                                               String level,
                                               String language,
                                               String sort,
                                               Pageable pageable);

    CourseDetailResponse getCourseDetail(Long idOrNull, String slugOrNull);
}
