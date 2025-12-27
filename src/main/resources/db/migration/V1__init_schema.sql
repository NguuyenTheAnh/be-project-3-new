-- =========================================================
-- LMS (Udemy-like) - MySQL 8+ - UTF8MB4
-- No invalidated_token (stateless access token, store refresh_token in DB)
-- All tables share common columns:
--   id, is_active, is_deleted, created_date, updated_date, created_user, updated_user
-- Any other column MUST have COMMENT.
-- =========================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- FILES
-- =========================
create table uploaded_file
(
    id                   bigint auto_increment primary key,

    storage_provider     varchar(50)  null comment 'Storage provider: LOCAL/MINIO/S3',
    bucket               varchar(255) null comment 'Bucket/container name (if any)',
    object_key           varchar(500) not null comment 'Object key/path in storage (unique)',
    original_name        varchar(255) null comment 'Original filename at upload time',
    content_type         varchar(150) null comment 'MIME type',
    size_bytes           bigint       null comment 'File size in bytes',
    checksum_sha256      varchar(64)  null comment 'SHA-256 checksum for integrity/dedup',
    is_public            tinyint(1)   null comment '1=public accessible, 0=requires auth',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    unique key uk_uploaded_file_object_key (object_key),
    key idx_uploaded_file_content_type (content_type)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

-- =========================
-- USERS / RBAC
-- =========================
create table `user`
(
    id                   bigint auto_increment primary key,

    email                varchar(255) not null comment 'Login email (unique)',
    password_hash        varchar(255) not null comment 'Hashed password (BCrypt/Argon2)',
    full_name            varchar(255) null comment 'Display name',
    avatar_file_id       bigint       null comment 'FK -> uploaded_file (avatar image)',
    phone                varchar(50)  null comment 'Phone number',
    date_of_birth        date         null comment 'Date of birth (optional)',
    last_login_at        datetime     null comment 'Last login timestamp',
    status               varchar(50)  null comment 'User status: ACTIVE/BLOCKED/PENDING',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    unique key uk_user_email (email),
    key idx_user_avatar_file (avatar_file_id),
    constraint fk_user_avatar_file foreign key (avatar_file_id) references uploaded_file (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table role
(
    id                   bigint auto_increment primary key,

    name                 varchar(100) not null comment 'Role name (unique): STUDENT/INSTRUCTOR/ADMIN',
    description          varchar(255) null comment 'Role description',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    unique key uk_role_name (name)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table permission
(
    id                   bigint auto_increment primary key,

    code                 varchar(150) not null comment 'Permission code (unique): COURSE_CREATE, COURSE_PUBLISH...',
    description          varchar(255) null comment 'Permission description',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    unique key uk_permission_code (code)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table user_roles
(
    id                   bigint auto_increment primary key,

    user_id              bigint not null comment 'FK -> user',
    role_id              bigint not null comment 'FK -> role',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    unique key uk_user_roles (user_id, role_id),
    key idx_user_roles_role (role_id),
    constraint fk_user_roles_user foreign key (user_id) references `user` (id),
    constraint fk_user_roles_role foreign key (role_id) references role (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table role_permissions
(
    id                   bigint auto_increment primary key,

    role_id              bigint not null comment 'FK -> role',
    permission_id        bigint not null comment 'FK -> permission',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    unique key uk_role_permissions (role_id, permission_id),
    key idx_role_permissions_permission (permission_id),
    constraint fk_role_permissions_role foreign key (role_id) references role (id),
    constraint fk_role_permissions_permission foreign key (permission_id) references permission (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

-- =========================
-- REFRESH TOKEN (Access JWT is stateless)
-- =========================
create table refresh_token
(
    id                   bigint auto_increment primary key,

    user_id              bigint       not null comment 'FK -> user (token owner)',
    token_hash           varchar(255) not null comment 'Refresh token hash (do NOT store raw token)',
    issued_at            datetime     not null comment 'Issued timestamp',
    expires_at           datetime     not null comment 'Expiry timestamp',
    revoked_at           datetime     null comment 'Revoked timestamp (logout/rotation)',
    revoke_reason        varchar(255) null comment 'Reason for revoke',
    replaced_by_id       bigint       null comment 'FK -> refresh_token (new token when rotated)',
    device_id            varchar(255) null comment 'Session/device identifier for multi-device',
    user_agent           varchar(500) null comment 'User-Agent at issuance',
    ip_address           varchar(64)  null comment 'IP address at issuance',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    unique key uk_refresh_token_hash (token_hash),
    key idx_refresh_token_user (user_id),
    key idx_refresh_token_expires (expires_at),
    key idx_refresh_token_device (device_id),
    constraint fk_refresh_token_user foreign key (user_id) references `user` (id),
    constraint fk_refresh_token_replaced foreign key (replaced_by_id) references refresh_token (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

-- =========================
-- CATALOG / COURSE
-- =========================
create table category
(
    id                   bigint auto_increment primary key,

    name                 varchar(255) not null comment 'Category name',
    slug                 varchar(255) null comment 'SEO slug (unique if used)',
    parent_id            bigint       null comment 'FK -> category (parent category)',
    description          varchar(500) null comment 'Category description',
    position             int          null comment 'Display ordering position',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    unique key uk_category_slug (slug),
    key idx_category_parent (parent_id),
    constraint fk_category_parent foreign key (parent_id) references category (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table course
(
    id                   bigint auto_increment primary key,

    category_id          bigint       null comment 'FK -> category',
    creator_user_id      bigint       not null comment 'FK -> user (course creator)',
    title                varchar(255) not null comment 'Course title',
    slug                 varchar(255) null comment 'SEO slug (unique if used)',
    short_description    varchar(500) null comment 'Short description',
    description          text         null comment 'Long description',
    level                varchar(50)  null comment 'Level: BEGINNER/INTERMEDIATE/ADVANCED',
    language             varchar(50)  null comment 'Language code: vi/en...',
    thumbnail_file_id    bigint       null comment 'FK -> uploaded_file (course thumbnail)',
    intro_video_file_id  bigint       null comment 'FK -> uploaded_file (intro video)',
    status               varchar(50)  null comment 'Status: DRAFT/REVIEW/PUBLISHED/ARCHIVED',
    published_at         datetime     null comment 'Published timestamp',
    rating_avg           decimal(3,2) null comment 'Cached average rating',
    rating_count         int          null comment 'Cached rating count',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    unique key uk_course_slug (slug),
    key idx_course_category (category_id),
    key idx_course_creator (creator_user_id),
    key idx_course_thumbnail (thumbnail_file_id),
    key idx_course_intro_video (intro_video_file_id),
    constraint fk_course_category foreign key (category_id) references category (id),
    constraint fk_course_creator foreign key (creator_user_id) references `user` (id),
    constraint fk_course_thumbnail foreign key (thumbnail_file_id) references uploaded_file (id),
    constraint fk_course_intro_video foreign key (intro_video_file_id) references uploaded_file (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table course_instructor
(
    id                   bigint auto_increment primary key,

    course_id            bigint not null comment 'FK -> course',
    user_id              bigint not null comment 'FK -> user (instructor)',
    instructor_role      varchar(50) null comment 'Role: OWNER/CO_INSTRUCTOR/ASSISTANT',
    revenue_share        decimal(5,2) null comment 'Revenue share percent (optional)',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    unique key uk_course_instructor (course_id, user_id),
    key idx_course_instructor_user (user_id),
    constraint fk_course_instructor_course foreign key (course_id) references course (id),
    constraint fk_course_instructor_user foreign key (user_id) references `user` (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table course_section
(
    id                   bigint auto_increment primary key,

    course_id            bigint not null comment 'FK -> course',
    title                varchar(255) not null comment 'Section title',
    position             int          null comment 'Position within course',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    key idx_course_section_course (course_id, position),
    constraint fk_course_section_course foreign key (course_id) references course (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table lesson
(
    id                   bigint auto_increment primary key,

    title                varchar(255) not null comment 'Lesson/lecture title',
    lesson_type          varchar(50)  not null comment 'Type: VIDEO/ARTICLE/QUIZ',
    content_text         longtext     null comment 'Text content for ARTICLE lessons',
    video_file_id        bigint       null comment 'FK -> uploaded_file (video for VIDEO lessons)',
    duration_seconds     int          null comment 'Video duration in seconds',
    is_free_preview      tinyint(1)   null comment '1=free preview, 0=paid content',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    key idx_lesson_video_file (video_file_id),
    constraint fk_lesson_video_file foreign key (video_file_id) references uploaded_file (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table course_lesson
(
    id                   bigint auto_increment primary key,

    course_id            bigint not null comment 'FK -> course',
    course_section_id    bigint null comment 'FK -> course_section (nullable if not grouped)',
    lesson_id            bigint not null comment 'FK -> lesson',
    position             int    null comment 'Position within section/course',
    is_preview           tinyint(1) null comment 'Override preview flag at mapping level',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    unique key uk_course_lesson (course_id, lesson_id),
    key idx_course_lesson_course (course_id),
    key idx_course_lesson_section (course_section_id, position),
    constraint fk_course_lesson_course foreign key (course_id) references course (id),
    constraint fk_course_lesson_section foreign key (course_section_id) references course_section (id),
    constraint fk_course_lesson_lesson foreign key (lesson_id) references lesson (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table course_document
(
    id                   bigint auto_increment primary key,

    course_id            bigint not null comment 'FK -> course',
    uploaded_file_id     bigint not null comment 'FK -> uploaded_file',
    title                varchar(255) null comment 'Document title',
    doc_type             varchar(50)  null comment 'Type: PDF/SLIDE/ATTACHMENT',
    position             int          null comment 'Display position',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    key idx_course_document_course (course_id, position),
    key idx_course_document_file (uploaded_file_id),
    constraint fk_course_document_course foreign key (course_id) references course (id),
    constraint fk_course_document_file foreign key (uploaded_file_id) references uploaded_file (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table lesson_document
(
    id                   bigint auto_increment primary key,

    lesson_id            bigint not null comment 'FK -> lesson',
    uploaded_file_id     bigint not null comment 'FK -> uploaded_file',
    title                varchar(255) null comment 'Document title',
    doc_type             varchar(50)  null comment 'Type: PDF/SLIDE/ATTACHMENT',
    position             int          null comment 'Display position',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    key idx_lesson_document_lesson (lesson_id, position),
    key idx_lesson_document_file (uploaded_file_id),
    constraint fk_lesson_document_lesson foreign key (lesson_id) references lesson (id),
    constraint fk_lesson_document_file foreign key (uploaded_file_id) references uploaded_file (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

-- =========================
-- ENROLLMENT / PROGRESS / REVIEW
-- =========================
create table enrollment
(
    id                   bigint auto_increment primary key,

    user_id              bigint not null comment 'FK -> user (student)',
    course_id            bigint not null comment 'FK -> course',
    enrolled_at          datetime null comment 'Enrollment timestamp',
    status               varchar(50) null comment 'Status: ACTIVE/COMPLETED/CANCELLED',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    unique key uk_enrollment (user_id, course_id),
    key idx_enrollment_course (course_id),
    constraint fk_enrollment_user foreign key (user_id) references `user` (id),
    constraint fk_enrollment_course foreign key (course_id) references course (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table progress
(
    id                   bigint auto_increment primary key,

    enrollment_id        bigint not null comment 'FK -> enrollment',
    lesson_id            bigint not null comment 'FK -> lesson',
    completed            tinyint(1) null comment '1=completed, 0=not completed',
    completed_at         datetime null comment 'Completion timestamp',
    last_position_seconds int null comment 'Last playback position in seconds (video)',
    last_accessed_at     datetime null comment 'Last access timestamp',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    unique key uk_progress (enrollment_id, lesson_id),
    key idx_progress_lesson (lesson_id),
    constraint fk_progress_enrollment foreign key (enrollment_id) references enrollment (id),
    constraint fk_progress_lesson foreign key (lesson_id) references lesson (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table course_review
(
    id                   bigint auto_increment primary key,

    course_id            bigint not null comment 'FK -> course',
    user_id              bigint not null comment 'FK -> user (reviewer)',
    rating               tinyint not null comment 'Star rating 1..5',
    title                varchar(255) null comment 'Review title',
    content              text         null comment 'Review content',
    status               varchar(50)  null comment 'Moderation status: PENDING/APPROVED/REJECTED',
    moderated_by_user_id bigint       null comment 'FK -> user (moderator)',
    moderated_at         datetime     null comment 'Moderated timestamp',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    unique key uk_course_review (course_id, user_id),
    key idx_course_review_course (course_id),
    key idx_course_review_user (user_id),
    constraint fk_course_review_course foreign key (course_id) references course (id),
    constraint fk_course_review_user foreign key (user_id) references `user` (id),
    constraint fk_course_review_moderator foreign key (moderated_by_user_id) references `user` (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

-- =========================
-- QUIZ
-- =========================
create table quiz
(
    id                   bigint auto_increment primary key,

    lesson_id            bigint not null comment 'FK -> lesson (quiz attached to lesson)',
    title                varchar(255) null comment 'Quiz title',
    time_limit_seconds   int          null comment 'Time limit in seconds',
    pass_score           decimal(5,2) null comment 'Pass score (e.g., 0..100)',
    max_attempts         int          null comment 'Maximum attempts allowed',
    shuffle_questions    tinyint(1)   null comment '1=shuffle questions, 0=keep order',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    unique key uk_quiz_lesson (lesson_id),
    constraint fk_quiz_lesson foreign key (lesson_id) references lesson (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table quiz_question
(
    id                   bigint auto_increment primary key,

    quiz_id              bigint not null comment 'FK -> quiz',
    question_text        text   not null comment 'Question text',
    question_type        varchar(50) not null comment 'Type: SINGLE/MULTI/TRUE_FALSE/TEXT',
    position             int    null comment 'Display position',
    points               decimal(6,2) null comment 'Points for this question',
    explanation          text   null comment 'Explanation shown after answering',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    key idx_quiz_question_quiz (quiz_id, position),
    constraint fk_quiz_question_quiz foreign key (quiz_id) references quiz (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table quiz_answer
(
    id                   bigint auto_increment primary key,

    question_id          bigint not null comment 'FK -> quiz_question',
    answer_text          text   not null comment 'Answer option text',
    is_correct           tinyint(1) null comment '1=correct option, 0=incorrect',
    position             int    null comment 'Option position',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    key idx_quiz_answer_question (question_id, position),
    constraint fk_quiz_answer_question foreign key (question_id) references quiz_question (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table quiz_attempt
(
    id                   bigint auto_increment primary key,

    quiz_id              bigint not null comment 'FK -> quiz',
    user_id              bigint not null comment 'FK -> user (attempt owner)',
    started_at           datetime null comment 'Attempt start time',
    submitted_at         datetime null comment 'Attempt submit time',
    score                decimal(8,2) null comment 'Attempt score',
    status               varchar(50)  null comment 'Status: IN_PROGRESS/SUBMITTED/GRADED',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    key idx_quiz_attempt_quiz (quiz_id),
    key idx_quiz_attempt_user (user_id),
    constraint fk_quiz_attempt_quiz foreign key (quiz_id) references quiz (id),
    constraint fk_quiz_attempt_user foreign key (user_id) references `user` (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table quiz_attempt_answer
(
    id                   bigint auto_increment primary key,

    quiz_attempt_id      bigint not null comment 'FK -> quiz_attempt',
    question_id          bigint not null comment 'FK -> quiz_question',
    answer_id            bigint null comment 'FK -> quiz_answer (selected option, if applicable)',
    answer_text          text   null comment 'Free-text answer (if question_type=TEXT)',
    is_correct           tinyint(1) null comment '1=correct, 0=incorrect (auto-graded if possible)',
    points_awarded       decimal(8,2) null comment 'Points awarded for this answer',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    unique key uk_quiz_attempt_answer (quiz_attempt_id, question_id),
    key idx_quiz_attempt_answer_answer (answer_id),
    constraint fk_quiz_attempt_answer_attempt foreign key (quiz_attempt_id) references quiz_attempt (id),
    constraint fk_quiz_attempt_answer_question foreign key (question_id) references quiz_question (id),
    constraint fk_quiz_attempt_answer_answer foreign key (answer_id) references quiz_answer (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

-- =========================
-- TAGS (Search / Discoverability)
-- =========================
create table tag
(
    id                   bigint auto_increment primary key,

    name                 varchar(100) not null comment 'Tag name (unique)',
    slug                 varchar(150) null comment 'SEO slug (unique if used)',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    unique key uk_tag_name (name),
    unique key uk_tag_slug (slug)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table course_tag
(
    id                   bigint auto_increment primary key,

    course_id            bigint not null comment 'FK -> course',
    tag_id               bigint not null comment 'FK -> tag',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    unique key uk_course_tag (course_id, tag_id),
    key idx_course_tag_tag (tag_id),
    constraint fk_course_tag_course foreign key (course_id) references course (id),
    constraint fk_course_tag_tag foreign key (tag_id) references tag (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

-- =========================
-- Q&A (Udemy-like)
-- =========================
create table question
(
    id                   bigint auto_increment primary key,

    course_id            bigint not null comment 'FK -> course',
    lesson_id            bigint null comment 'FK -> lesson (optional; question related to a lesson)',
    user_id              bigint not null comment 'FK -> user (asker)',
    title                varchar(255) not null comment 'Question title',
    content              text         not null comment 'Question content',
    status               varchar(50)  null comment 'Status: OPEN/RESOLVED/CLOSED',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    key idx_question_course (course_id),
    key idx_question_lesson (lesson_id),
    key idx_question_user (user_id),
    constraint fk_question_course foreign key (course_id) references course (id),
    constraint fk_question_lesson foreign key (lesson_id) references lesson (id),
    constraint fk_question_user foreign key (user_id) references `user` (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table answer
(
    id                   bigint auto_increment primary key,

    question_id          bigint not null comment 'FK -> question',
    user_id              bigint not null comment 'FK -> user (answerer)',
    content              text   not null comment 'Answer content',
    is_accepted          tinyint(1) null comment '1=accepted answer, 0=not accepted',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    key idx_answer_question (question_id),
    key idx_answer_user (user_id),
    constraint fk_answer_question foreign key (question_id) references question (id),
    constraint fk_answer_user foreign key (user_id) references `user` (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table question_vote
(
    id                   bigint auto_increment primary key,

    question_id          bigint not null comment 'FK -> question',
    user_id              bigint not null comment 'FK -> user (voter)',
    vote_type            varchar(10) not null comment 'Vote type: UP/DOWN',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    unique key uk_question_vote (question_id, user_id),
    constraint fk_question_vote_question foreign key (question_id) references question (id),
    constraint fk_question_vote_user foreign key (user_id) references `user` (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

-- =========================
-- COMMERCE (Udemy-like)
-- =========================
create table `order`
(
    id                   bigint auto_increment primary key,

    user_id              bigint not null comment 'FK -> user (buyer)',
    total_amount_cents   bigint not null comment 'Total amount after discount (in cents)',
    currency             varchar(10) not null comment 'Currency code: VND/USD...',
    status               varchar(50) null comment 'Status: PENDING/PAID/FAILED/CANCELLED/REFUNDED',
    payment_method       varchar(50) null comment 'Payment method: VNPAY/MOMO/STRIPE...',
    paid_at              datetime null comment 'Paid timestamp',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    key idx_order_user (user_id),
    key idx_order_status (status),
    constraint fk_order_user foreign key (user_id) references `user` (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table order_item
(
    id                   bigint auto_increment primary key,

    order_id             bigint not null comment 'FK -> order',
    course_id            bigint not null comment 'FK -> course (purchased course)',
    price_cents          bigint not null comment 'List price in cents',
    discount_cents       bigint null comment 'Discount amount in cents',
    final_price_cents    bigint not null comment 'Final price in cents',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    unique key uk_order_item (order_id, course_id),
    key idx_order_item_course (course_id),
    constraint fk_order_item_order foreign key (order_id) references `order` (id),
    constraint fk_order_item_course foreign key (course_id) references course (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table payment_transaction
(
    id                   bigint auto_increment primary key,

    order_id             bigint not null comment 'FK -> order',
    provider             varchar(50) not null comment 'Payment provider: VNPAY/MOMO/STRIPE...',
    provider_txn_id      varchar(255) null comment 'Transaction id from provider',
    amount_cents         bigint not null comment 'Transaction amount in cents',
    currency             varchar(10) not null comment 'Currency code',
    status               varchar(50) null comment 'Status: INIT/SUCCESS/FAILED',
    raw_response_json    longtext     null comment 'Raw provider response payload for audit/debug',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    key idx_payment_transaction_order (order_id),
    key idx_payment_transaction_provider_txn (provider_txn_id),
    constraint fk_payment_transaction_order foreign key (order_id) references `order` (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table refund_request
(
    id                   bigint auto_increment primary key,

    order_item_id        bigint not null comment 'FK -> order_item (refunded purchase item)',
    user_id              bigint not null comment 'FK -> user (requester)',
    reason               varchar(500) null comment 'Refund reason',
    status               varchar(50) null comment 'Status: PENDING/APPROVED/REJECTED/PROCESSED',
    requested_at         datetime null comment 'Requested timestamp',
    approved_at          datetime null comment 'Approved timestamp',
    processed_at         datetime null comment 'Processed timestamp',
    refund_amount_cents  bigint   null comment 'Refund amount in cents',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    key idx_refund_request_order_item (order_item_id),
    key idx_refund_request_user (user_id),
    constraint fk_refund_request_order_item foreign key (order_item_id) references order_item (id),
    constraint fk_refund_request_user foreign key (user_id) references `user` (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

-- =========================
-- MODERATION / REPORTING (important for THCS)
-- =========================
create table content_report
(
    id                   bigint auto_increment primary key,

    reporter_user_id     bigint not null comment 'FK -> user (reporter)',
    target_type          varchar(50) not null comment 'Target type: REVIEW/QUESTION/ANSWER/COURSE/LESSON',
    target_id            bigint not null comment 'Target object id',
    reason               varchar(500) null comment 'Report reason',
    status               varchar(50) null comment 'Status: OPEN/IN_REVIEW/RESOLVED/REJECTED',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    key idx_content_report_target (target_type, target_id),
    key idx_content_report_status (status),
    constraint fk_content_report_reporter foreign key (reporter_user_id) references `user` (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table moderation_action
(
    id                   bigint auto_increment primary key,

    report_id            bigint not null comment 'FK -> content_report',
    moderator_user_id    bigint not null comment 'FK -> user (moderator)',
    action               varchar(50) not null comment 'Action: APPROVE/REJECT/HIDE/BLOCK_USER',
    note                 varchar(500) null comment 'Moderator note',
    acted_at             datetime null comment 'Action timestamp',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    key idx_moderation_action_report (report_id),
    constraint fk_moderation_action_report foreign key (report_id) references content_report (id),
    constraint fk_moderation_action_moderator foreign key (moderator_user_id) references `user` (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

-- =========================
-- DOCUMENT VIEW (Analytics-lite)
-- =========================
create table document_view
(
    id                   bigint auto_increment primary key,

    user_id              bigint not null comment 'FK -> user (viewer)',
    uploaded_file_id     bigint not null comment 'FK -> uploaded_file (viewed document)',
    view_count           int    null comment 'Aggregated view count',
    last_viewed_at       datetime null comment 'Last viewed timestamp',

    is_active            tinyint(1)    null,
    is_deleted           tinyint(1)    null,
    created_date         datetime      null,
    updated_date         datetime      null,
    created_user         varchar(255)  null,
    updated_user         varchar(255)  null,

    unique key uk_document_view (user_id, uploaded_file_id),
    key idx_document_view_file (uploaded_file_id),
    constraint fk_document_view_user foreign key (user_id) references `user` (id),
    constraint fk_document_view_file foreign key (uploaded_file_id) references uploaded_file (id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
