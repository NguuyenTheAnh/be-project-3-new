package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.common.exception.BusinessException;
import com.theanh.lms.dto.ContentReportDto;
import com.theanh.lms.dto.UserDto;
import com.theanh.lms.dto.response.ContentReportResponse;
import com.theanh.lms.entity.ContentReport;
import com.theanh.lms.enums.ReportStatus;
import com.theanh.lms.enums.ReportTargetType;
import com.theanh.lms.repository.ContentReportRepository;
import com.theanh.lms.repository.AnswerRepository;
import com.theanh.lms.repository.QuestionRepository;
import com.theanh.lms.repository.CourseRepository;
import com.theanh.lms.repository.LessonRepository;
import com.theanh.lms.repository.CourseReviewRepository;
import com.theanh.lms.service.ContentReportService;
import com.theanh.lms.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ContentReportServiceImpl extends BaseServiceImpl<ContentReport, ContentReportDto, Long> implements ContentReportService {

    private final ContentReportRepository repository;
    private final CourseReviewRepository courseReviewRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final UserService userService;

    public ContentReportServiceImpl(ContentReportRepository repository,
                                    CourseReviewRepository courseReviewRepository,
                                    QuestionRepository questionRepository,
                                    AnswerRepository answerRepository,
                                    CourseRepository courseRepository,
                                    LessonRepository lessonRepository,
                                    ModelMapper modelMapper, UserService userService) {
        super(repository, modelMapper);
        this.repository = repository;
        this.courseReviewRepository = courseReviewRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.userService = userService;
    }

    @Override
    public ContentReportDto report(Long userId, String targetType, Long targetId, String reason) {
        ReportTargetType type = parseTarget(targetType);
        ensureTargetExists(type, targetId);
        ContentReport report = repository.findByTargetAndUser(type.name(), targetId, userId)
                .orElseGet(() -> {
                    ContentReport r = new ContentReport();
                    r.setReporterUserId(userId);
                    r.setTargetType(type.name());
                    r.setTargetId(targetId);
                    r.setIsActive(Boolean.TRUE);
                    r.setIsDeleted(Boolean.FALSE);
                    return r;
                });
        if (reason != null) {
            report.setReason(reason);
        }
        report.setStatus(ReportStatus.OPEN.name());
        ContentReport saved = repository.save(report);
        return modelMapper.map(saved, ContentReportDto.class);
    }

    @Override
    public Page<ContentReportResponse> listAll(Pageable pageable) {
        return repository.findAllActive(pageable)
                .map(r -> {
                    var res = modelMapper.map(r, ContentReportResponse.class);
                    UserDto reporter = userService.findById(res.getReporterUserId());
                    res.setReporterName(reporter != null ? reporter.getFullName() : "Người dùng ân danh");
                    return res;
                });
    }

    @Override
    public ContentReportDto updateStatus(Long reportId, String status) {
        ReportStatus target = parseStatus(status);
        ContentReport report = repository.findActiveById(reportId)
                .orElseThrow(() -> new BusinessException("data.not_found"));
        report.setStatus(target.name());
        ContentReport saved = repository.save(report);
        return modelMapper.map(saved, ContentReportDto.class);
    }

    @Override
    public Page<ContentReportDto> listMyReports(Long userId, Pageable pageable) {
        return repository.findByReporter(userId, pageable)
                .map(r -> modelMapper.map(r, ContentReportDto.class));
    }

    @Override
    public ContentReportDto getMyReport(Long userId, Long reportId) {
        ContentReport report = repository.findActiveById(reportId)
                .orElseThrow(() -> new BusinessException("data.not_found"));
        if (!report.getReporterUserId().equals(userId)) {
            throw new BusinessException("auth.forbidden");
        }
        return modelMapper.map(report, ContentReportDto.class);
    }

    @Override
    public ContentReportDto updateMyReport(Long userId, Long reportId, String reason) {
        ContentReport report = repository.findActiveById(reportId)
                .orElseThrow(() -> new BusinessException("data.not_found"));
        if (!report.getReporterUserId().equals(userId)) {
            throw new BusinessException("auth.forbidden");
        }
        if (!ReportStatus.OPEN.name().equals(report.getStatus())) {
            throw new BusinessException("data.fail");
        }
        if (StringUtils.hasText(reason)) {
            report.setReason(reason);
        }
        ContentReport saved = repository.save(report);
        return modelMapper.map(saved, ContentReportDto.class);
    }

    private ReportTargetType parseTarget(String raw) {
        try {
            return ReportTargetType.valueOf(raw);
        } catch (Exception ex) {
            throw new BusinessException("data.fail");
        }
    }

    private ReportStatus parseStatus(String raw) {
        try {
            return ReportStatus.valueOf(raw);
        } catch (Exception ex) {
            throw new BusinessException("data.fail");
        }
    }

    private void ensureTargetExists(ReportTargetType type, Long targetId) {
        boolean exists = switch (type) {
            case REVIEW -> courseReviewRepository.findById(targetId)
                    .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
                    .isPresent();
            case QUESTION -> questionRepository.findById(targetId)
                    .filter(q -> !Boolean.TRUE.equals(q.getIsDeleted()))
                    .isPresent();
            case ANSWER -> answerRepository.findById(targetId)
                    .filter(a -> !Boolean.TRUE.equals(a.getIsDeleted()))
                    .isPresent();
            case COURSE -> courseRepository.findById(targetId)
                    .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                    .isPresent();
            case LESSON -> lessonRepository.findById(targetId)
                    .filter(l -> !Boolean.TRUE.equals(l.getIsDeleted()))
                    .isPresent();
        };
        if (!exists) {
            throw new BusinessException("data.not_found");
        }
    }

    @Override
    protected Class<ContentReport> getEntityClass() {
        return ContentReport.class;
    }

    @Override
    protected Class<ContentReportDto> getDtoClass() {
        return ContentReportDto.class;
    }
}
