package com.theanh.lms.controller;

import com.theanh.common.dto.ResponseDto;
import com.theanh.common.util.ResponseConfig;
import com.theanh.lms.dto.CourseDetailResponse;
import com.theanh.lms.dto.CourseListItemResponse;
import com.theanh.lms.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping
    public ResponseEntity<ResponseDto<Page<CourseListItemResponse>>> searchCourses(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "tagIds", required = false) String tagIds,
            @RequestParam(value = "level", required = false) String level,
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "sort", required = false) String sort,
            Pageable pageable) {
        List<Long> tagIdList = parseIdList(tagIds);
        Page<CourseListItemResponse> page = catalogService.searchCourses(keyword, categoryId, tagIdList, level, language, sort, pageable);
        return ResponseConfig.success(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<CourseDetailResponse>> getCourse(@PathVariable Long id) {
        return ResponseConfig.success(catalogService.getCourseDetail(id, null));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ResponseDto<CourseDetailResponse>> getCourseBySlug(@PathVariable String slug) {
        return ResponseConfig.success(catalogService.getCourseDetail(null, slug));
    }

    private List<Long> parseIdList(String ids) {
        if (!StringUtils.hasText(ids)) {
            return List.of();
        }
        return Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(Long::parseLong)
                .toList();
    }
}
