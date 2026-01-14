package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.common.exception.BusinessException;
import com.theanh.lms.dto.AnswerDto;
import com.theanh.lms.dto.CourseDto;
import com.theanh.lms.dto.LessonDto;
import com.theanh.lms.dto.QuestionDetailResponse;
import com.theanh.lms.dto.QuestionDto;
import com.theanh.lms.entity.Question;
import com.theanh.lms.enums.RoleName;
import com.theanh.lms.enums.QuestionStatus;
import com.theanh.lms.repository.QuestionRepository;
import com.theanh.lms.service.AnswerService;
import com.theanh.lms.service.CourseInstructorService;
import com.theanh.lms.service.CourseService;
import com.theanh.lms.service.LessonService;
import com.theanh.lms.service.QuestionService;
import com.theanh.lms.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl extends BaseServiceImpl<Question, QuestionDto, Long> implements QuestionService {

    private final QuestionRepository repository;
    private final CourseService courseService;
    private final LessonService lessonService;
    private final AnswerService answerService;
    private final CourseInstructorService courseInstructorService;
    private final UserService userService;

    public QuestionServiceImpl(QuestionRepository repository,
                               CourseService courseService,
                               LessonService lessonService,
                               AnswerService answerService,
                               CourseInstructorService courseInstructorService,
                               UserService userService,
                               ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.repository = repository;
        this.courseService = courseService;
        this.lessonService = lessonService;
        this.answerService = answerService;
        this.courseInstructorService = courseInstructorService;
        this.userService = userService;
    }

    @Override
    public QuestionDto findActiveById(Long id) {
        return repository.findActiveById(id)
                .map(q -> modelMapper.map(q, QuestionDto.class))
                .orElse(null);
    }

    @Override
    public Page<QuestionDto> listByCourse(Long courseId, Pageable pageable) {
        return repository.findByCourse(courseId, pageable)
                .map(q -> modelMapper.map(q, QuestionDto.class));
    }

    @Override
    public Page<QuestionDto> listByCourse(Long courseId, Long lessonId, Pageable pageable) {
        if (lessonId == null) {
            return repository.findByCourseAndNoLesson(courseId, pageable)
                    .map(q -> modelMapper.map(q, QuestionDto.class));
        }
        return repository.findByCourseAndLesson(courseId, lessonId, pageable)
                .map(q -> modelMapper.map(q, QuestionDto.class));
    }

    @Override
    public Page<QuestionDetailResponse> listForManagement(Long userId, Pageable pageable) {
        boolean isAdmin = userId != null && userService.findRoles(userId).contains(RoleName.ADMIN.name());
        Page<Question> questionPage;
        if (isAdmin) {
            questionPage = repository.findAllActive(pageable);
        } else {
            List<Long> courseIds = courseInstructorService.findCourseIdsByInstructor(userId);
            if (CollectionUtils.isEmpty(courseIds)) {
                return Page.empty(pageable);
            }
            questionPage = repository.findByCourseIds(courseIds, pageable);
        }
        List<Question> questions = questionPage.getContent();
        if (CollectionUtils.isEmpty(questions)) {
            return new PageImpl<>(List.of(), pageable, questionPage.getTotalElements());
        }
        List<Long> courseIds = questions.stream()
                .map(Question::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<Long> lessonIds = questions.stream()
                .map(Question::getLessonId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<Long> questionIds = questions.stream()
                .map(Question::getId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, CourseDto> courseMap = courseService.findByIds(courseIds)
                .stream()
                .collect(Collectors.toMap(CourseDto::getId, c -> c));
        Map<Long, LessonDto> lessonMap = lessonService.findByIds(lessonIds)
                .stream()
                .collect(Collectors.toMap(LessonDto::getId, l -> l));
        Map<Long, List<AnswerDto>> answerMap = answerService.findByQuestions(questionIds)
                .stream()
                .collect(Collectors.groupingBy(AnswerDto::getQuestionId));
        List<QuestionDetailResponse> responses = questions.stream().map(q -> {
            QuestionDetailResponse resp = new QuestionDetailResponse();
            resp.setId(q.getId());
            resp.setCourse(courseMap.get(q.getCourseId()));
            resp.setLesson(q.getLessonId() != null ? lessonMap.get(q.getLessonId()) : null);
            resp.setUserId(q.getUserId());
            resp.setCreatedUser(q.getCreatedUser());
            resp.setTitle(q.getTitle());
            resp.setContent(q.getContent());
            resp.setStatus(q.getStatus());
            resp.setAnswers(answerMap.getOrDefault(q.getId(), List.of()));
            return resp;
        }).toList();
        return new PageImpl<>(responses, pageable, questionPage.getTotalElements());
    }

    @Override
    public QuestionDto saveObject(QuestionDto dto) {
        if (dto.getStatus() == null) {
            dto.setStatus(QuestionStatus.OPEN.name());
        }
        return super.saveObject(dto);
    }

    @Override
    protected Class<Question> getEntityClass() {
        return Question.class;
    }

    @Override
    protected Class<QuestionDto> getDtoClass() {
        return QuestionDto.class;
    }
}
