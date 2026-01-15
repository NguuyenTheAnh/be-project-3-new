package com.theanh.lms.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorDashboardDto {
    private InstructorKpiDto kpi;
    private List<UnansweredQuestionDto> unansweredQuestions;
    private List<TopCourseDto> myCourses;
    private List<CourseCompletionDto> courseCompletions;
}
