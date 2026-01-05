package com.theanh.lms.service;

import com.theanh.common.base.BaseService;
import com.theanh.lms.dto.CourseDto;
import com.theanh.lms.entity.Course;

public interface CourseService extends BaseService<Course, CourseDto, Long> {

    CourseDto findActiveById(Long id);

    CourseDto findActivePublishedById(Long id);
}
