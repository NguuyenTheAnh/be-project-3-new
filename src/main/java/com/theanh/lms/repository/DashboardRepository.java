package com.theanh.lms.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DashboardRepository {

    private final EntityManager entityManager;

    private static final String NOT_DELETED = " (is_deleted = 0 OR is_deleted IS NULL) ";

    // ==================== ADMIN KPI ====================
    public Long countTotalUsers() {
        Query query = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM `user` WHERE " + NOT_DELETED);
        Object result = query.getSingleResult();
        return result != null ? ((Number) result).longValue() : 0L;
    }

    public Long countTotalInstructors() {
        Query query = entityManager.createNativeQuery(
                "SELECT COUNT(DISTINCT ur.user_id) " +
                        "FROM user_roles ur " +
                        "INNER JOIN role r ON ur.role_id = r.id " +
                        "WHERE r.name = 'INSTRUCTOR' " +
                        "AND (ur.is_deleted = 0 OR ur.is_deleted IS NULL)");
        Object result = query.getSingleResult();
        return result != null ? ((Number) result).longValue() : 0L;
    }

    public Long countTotalCourses() {
        Query query = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM course WHERE " + NOT_DELETED);
        Object result = query.getSingleResult();
        return result != null ? ((Number) result).longValue() : 0L;
    }

    public Long countPublishedCourses() {
        Query query = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM course WHERE status = 'PUBLISHED' AND " + NOT_DELETED);
        Object result = query.getSingleResult();
        return result != null ? ((Number) result).longValue() : 0L;
    }

    public Long countEnrollmentsSince(LocalDateTime startDate) {
        Query query = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM enrollment " +
                        "WHERE enrolled_at >= :startDate AND " + NOT_DELETED)
                .setParameter("startDate", startDate);
        Object result = query.getSingleResult();
        return result != null ? ((Number) result).longValue() : 0L;
    }

    public Long sumRevenueBetween(LocalDateTime startDate, LocalDateTime endDate) {
        Query query = entityManager.createNativeQuery(
                "SELECT COALESCE(SUM(o.total_amount_cents), 0) " +
                        "FROM `order` o " +
                        "WHERE o.status = 'PAID' " +
                        "AND o.paid_at >= :startDate AND o.paid_at < :endDate " +
                        "AND (o.is_deleted = 0 OR o.is_deleted IS NULL)")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate);
        Object result = query.getSingleResult();
        return result != null ? ((Number) result).longValue() : 0L;
    }

    // ==================== ADMIN TRENDS ====================
    @SuppressWarnings("unchecked")
    public List<Object[]> getRevenueTrend(LocalDateTime startDate, int limit) {
        Query query = entityManager.createNativeQuery(
                "SELECT DATE(o.paid_at) AS date, " +
                        "COALESCE(SUM(o.total_amount_cents), 0) AS revenue, " +
                        "COUNT(o.id) AS orderCount " +
                        "FROM `order` o " +
                        "WHERE o.status = 'PAID' AND o.paid_at >= :startDate " +
                        "AND (o.is_deleted = 0 OR o.is_deleted IS NULL) " +
                        "GROUP BY DATE(o.paid_at) " +
                        "ORDER BY DATE(o.paid_at) DESC " +
                        "LIMIT :limit")
                .setParameter("startDate", startDate)
                .setParameter("limit", limit);

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> getEnrollmentTrend(LocalDateTime startDate, int limit) {
        Query query = entityManager.createNativeQuery(
                "SELECT DATE(e.enrolled_at) AS date, " +
                        "COUNT(e.id) AS enrollmentCount " +
                        "FROM enrollment e " +
                        "WHERE e.enrolled_at >= :startDate " +
                        "AND (e.is_deleted = 0 OR e.is_deleted IS NULL) " +
                        "GROUP BY DATE(e.enrolled_at) " +
                        "ORDER BY DATE(e.enrolled_at) DESC " +
                        "LIMIT :limit")
                .setParameter("startDate", startDate)
                .setParameter("limit", limit);

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> getCourseFunnel() {
        Query query = entityManager.createNativeQuery(
                "SELECT c.status, COUNT(c.id) AS courseCount " +
                        "FROM course c " +
                        "WHERE (c.is_deleted = 0 OR c.is_deleted IS NULL) " +
                        "GROUP BY c.status " +
                        "ORDER BY courseCount DESC");
        return query.getResultList();
    }

    // ==================== ADMIN QUEUES ====================
    @SuppressWarnings("unchecked")
    public List<Object[]> getRefundQueue(int limit) {
        // refund_request: order_item_id, user_id, reason, status, requested_at,
        // refund_amount_cents
        Query query = entityManager.createNativeQuery(
                "SELECT rr.id AS refundRequestId, " +
                        "rr.order_item_id AS orderItemId, " +
                        "rr.user_id AS userId, " +
                        "u.full_name AS userFullName, " +
                        "rr.reason AS reason, " +
                        "rr.refund_amount_cents AS refundAmountCents, " +
                        "rr.status AS status, " +
                        "rr.requested_at AS requestedAt " +
                        "FROM refund_request rr " +
                        "INNER JOIN `user` u ON rr.user_id = u.id " +
                        "WHERE rr.status = 'PENDING' " +
                        "AND (rr.is_deleted = 0 OR rr.is_deleted IS NULL) " +
                        "ORDER BY rr.requested_at DESC " +
                        "LIMIT :limit")
                .setParameter("limit", limit);

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> getContentReportQueue(int limit) {
        // content_report: target_type, target_id
        Query query = entityManager.createNativeQuery(
                "SELECT cr.id AS reportId, " +
                        "cr.target_type AS targetType, " +
                        "cr.target_id AS targetId, " +
                        "cr.reporter_user_id AS reporterUserId, " +
                        "u.full_name AS reporterFullName, " +
                        "cr.reason AS reason, " +
                        "cr.status AS status, " +
                        "cr.created_date AS reportedAt " +
                        "FROM content_report cr " +
                        "INNER JOIN `user` u ON cr.reporter_user_id = u.id " +
                        "WHERE cr.status IN ('OPEN', 'IN_REVIEW') " +
                        "AND (cr.is_deleted = 0 OR cr.is_deleted IS NULL) " +
                        "ORDER BY cr.created_date DESC " +
                        "LIMIT :limit")
                .setParameter("limit", limit);

        return query.getResultList();
    }

    // ==================== ADMIN TOP TABLES ====================
    @SuppressWarnings("unchecked")
    public List<Object[]> getTopCoursesByRevenue(int limit) {
        Query query = entityManager.createNativeQuery(
                "SELECT c.id AS courseId, c.title AS courseTitle, " +
                        "COUNT(DISTINCT e.id) AS enrollmentCount, " +
                        "COALESCE(SUM(CASE WHEN o.status = 'PAID' THEN oi.final_price_cents ELSE 0 END), 0) AS revenue, "
                        +
                        "c.rating_avg AS ratingAvg, c.rating_count AS ratingCount " +
                        "FROM course c " +
                        "LEFT JOIN enrollment e ON c.id = e.course_id AND (e.is_deleted = 0 OR e.is_deleted IS NULL) " +
                        "LEFT JOIN order_item oi ON c.id = oi.course_id AND (oi.is_deleted = 0 OR oi.is_deleted IS NULL) "
                        +
                        "LEFT JOIN `order` o ON oi.order_id = o.id AND (o.is_deleted = 0 OR o.is_deleted IS NULL) " +
                        "WHERE (c.is_deleted = 0 OR c.is_deleted IS NULL) " +
                        "AND c.status = 'PUBLISHED' " +
                        "GROUP BY c.id, c.title, c.rating_avg, c.rating_count " +
                        "ORDER BY revenue DESC " +
                        "LIMIT :limit")
                .setParameter("limit", limit);

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> getTopCoursesByEnrollment(int limit) {
        Query query = entityManager.createNativeQuery(
                "SELECT c.id AS courseId, c.title AS courseTitle, " +
                        "COUNT(DISTINCT e.id) AS enrollmentCount, " +
                        "COALESCE(SUM(CASE WHEN o.status = 'PAID' THEN oi.final_price_cents ELSE 0 END), 0) AS revenue, "
                        +
                        "c.rating_avg AS ratingAvg, c.rating_count AS ratingCount " +
                        "FROM course c " +
                        "LEFT JOIN enrollment e ON c.id = e.course_id AND (e.is_deleted = 0 OR e.is_deleted IS NULL) " +
                        "LEFT JOIN order_item oi ON c.id = oi.course_id AND (oi.is_deleted = 0 OR oi.is_deleted IS NULL) "
                        +
                        "LEFT JOIN `order` o ON oi.order_id = o.id AND (o.is_deleted = 0 OR o.is_deleted IS NULL) " +
                        "WHERE (c.is_deleted = 0 OR c.is_deleted IS NULL) " +
                        "AND c.status = 'PUBLISHED' " +
                        "GROUP BY c.id, c.title, c.rating_avg, c.rating_count " +
                        "ORDER BY enrollmentCount DESC " +
                        "LIMIT :limit")
                .setParameter("limit", limit);

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> getLowRatingCourses(int minRatingCount, int limit) {
        Query query = entityManager.createNativeQuery(
                "SELECT c.id AS courseId, c.title AS courseTitle, " +
                        "COUNT(DISTINCT e.id) AS enrollmentCount, " +
                        "COALESCE(SUM(CASE WHEN o.status = 'PAID' THEN oi.final_price_cents ELSE 0 END), 0) AS revenue, "
                        +
                        "c.rating_avg AS ratingAvg, c.rating_count AS ratingCount " +
                        "FROM course c " +
                        "LEFT JOIN enrollment e ON c.id = e.course_id AND (e.is_deleted = 0 OR e.is_deleted IS NULL) " +
                        "LEFT JOIN order_item oi ON c.id = oi.course_id AND (oi.is_deleted = 0 OR oi.is_deleted IS NULL) "
                        +
                        "LEFT JOIN `order` o ON oi.order_id = o.id AND (o.is_deleted = 0 OR o.is_deleted IS NULL) " +
                        "WHERE (c.is_deleted = 0 OR c.is_deleted IS NULL) " +
                        "AND c.status = 'PUBLISHED' " +
                        "AND c.rating_count >= :minRatingCount " +
                        "GROUP BY c.id, c.title, c.rating_avg, c.rating_count " +
                        "ORDER BY ratingAvg ASC " +
                        "LIMIT :limit")
                .setParameter("minRatingCount", minRatingCount)
                .setParameter("limit", limit);

        return query.getResultList();
    }

    // ==================== INSTRUCTOR KPI ====================
    public Long countInstructorPublishedCourses(Long instructorId) {
        Query query = entityManager.createNativeQuery(
                "SELECT COUNT(DISTINCT c.id) " +
                        "FROM course c " +
                        "WHERE c.status = 'PUBLISHED' " +
                        "AND (c.creator_user_id = :instructorId " +
                        "     OR EXISTS (SELECT 1 FROM course_instructor ci " +
                        "                WHERE ci.course_id = c.id AND ci.user_id = :instructorId " +
                        "                AND (ci.is_deleted = 0 OR ci.is_deleted IS NULL))) " +
                        "AND (c.is_deleted = 0 OR c.is_deleted IS NULL)")
                .setParameter("instructorId", instructorId);

        Object result = query.getSingleResult();
        return result != null ? ((Number) result).longValue() : 0L;
    }

    public Long countInstructorTotalStudents(Long instructorId) {
        Query query = entityManager.createNativeQuery(
                "SELECT COUNT(DISTINCT e.user_id) " +
                        "FROM enrollment e " +
                        "INNER JOIN course c ON e.course_id = c.id " +
                        "WHERE (c.creator_user_id = :instructorId " +
                        "       OR EXISTS (SELECT 1 FROM course_instructor ci " +
                        "                  WHERE ci.course_id = c.id AND ci.user_id = :instructorId " +
                        "                  AND (ci.is_deleted = 0 OR ci.is_deleted IS NULL))) " +
                        "AND (c.is_deleted = 0 OR c.is_deleted IS NULL) " +
                        "AND (e.is_deleted = 0 OR e.is_deleted IS NULL)")
                .setParameter("instructorId", instructorId);

        Object result = query.getSingleResult();
        return result != null ? ((Number) result).longValue() : 0L;
    }

    public Long countInstructorEnrollmentsSince(Long instructorId, LocalDateTime startDate) {
        Query query = entityManager.createNativeQuery(
                "SELECT COUNT(e.id) " +
                        "FROM enrollment e " +
                        "INNER JOIN course c ON e.course_id = c.id " +
                        "WHERE (c.creator_user_id = :instructorId " +
                        "       OR EXISTS (SELECT 1 FROM course_instructor ci " +
                        "                  WHERE ci.course_id = c.id AND ci.user_id = :instructorId " +
                        "                  AND (ci.is_deleted = 0 OR ci.is_deleted IS NULL))) " +
                        "AND e.enrolled_at >= :startDate " +
                        "AND (c.is_deleted = 0 OR c.is_deleted IS NULL) " +
                        "AND (e.is_deleted = 0 OR e.is_deleted IS NULL)")
                .setParameter("instructorId", instructorId)
                .setParameter("startDate", startDate);

        Object result = query.getSingleResult();
        return result != null ? ((Number) result).longValue() : 0L;
    }

    public Long sumInstructorRevenueSince(Long instructorId, LocalDateTime startDate) {
        Query query = entityManager.createNativeQuery(
                "SELECT COALESCE(SUM(oi.final_price_cents), 0) " +
                        "FROM order_item oi " +
                        "INNER JOIN `order` o ON oi.order_id = o.id " +
                        "INNER JOIN course c ON oi.course_id = c.id " +
                        "WHERE o.status = 'PAID' AND o.paid_at >= :startDate " +
                        "AND (c.creator_user_id = :instructorId " +
                        "     OR EXISTS (SELECT 1 FROM course_instructor ci " +
                        "                WHERE ci.course_id = c.id AND ci.user_id = :instructorId " +
                        "                AND (ci.is_deleted = 0 OR ci.is_deleted IS NULL))) " +
                        "AND (o.is_deleted = 0 OR o.is_deleted IS NULL) " +
                        "AND (oi.is_deleted = 0 OR oi.is_deleted IS NULL) " +
                        "AND (c.is_deleted = 0 OR c.is_deleted IS NULL)")
                .setParameter("instructorId", instructorId)
                .setParameter("startDate", startDate);

        Object result = query.getSingleResult();
        return result != null ? ((Number) result).longValue() : 0L;
    }

    public Double getInstructorAvgRating(Long instructorId) {
        Query query = entityManager.createNativeQuery(
                "SELECT AVG(c.rating_avg) " +
                        "FROM course c " +
                        "WHERE c.status = 'PUBLISHED' " +
                        "AND (c.creator_user_id = :instructorId " +
                        "     OR EXISTS (SELECT 1 FROM course_instructor ci " +
                        "                WHERE ci.course_id = c.id AND ci.user_id = :instructorId " +
                        "                AND (ci.is_deleted = 0 OR ci.is_deleted IS NULL))) " +
                        "AND c.rating_count > 0 " +
                        "AND (c.is_deleted = 0 OR c.is_deleted IS NULL)")
                .setParameter("instructorId", instructorId);

        Object result = query.getSingleResult();
        return result != null ? ((Number) result).doubleValue() : null;
    }

    // ==================== INSTRUCTOR LISTS ====================
    @SuppressWarnings("unchecked")
    public List<Object[]> getInstructorUnansweredQuestions(Long instructorId, int limit) {
        Query query = entityManager.createNativeQuery(
                "SELECT q.id AS questionId, q.course_id AS courseId, c.title AS courseTitle, " +
                        "q.lesson_id AS lessonId, l.title AS lessonTitle, q.title AS questionTitle, " +
                        "q.content AS questionContent, q.user_id AS askerUserId, u.full_name AS askerFullName, " +
                        "q.created_date AS askedAt " +
                        "FROM question q " +
                        "INNER JOIN course c ON q.course_id = c.id " +
                        "LEFT JOIN lesson l ON q.lesson_id = l.id " +
                        "INNER JOIN `user` u ON q.user_id = u.id " +
                        "WHERE (c.creator_user_id = :instructorId " +
                        "       OR EXISTS (SELECT 1 FROM course_instructor ci " +
                        "                  WHERE ci.course_id = c.id AND ci.user_id = :instructorId " +
                        "                  AND (ci.is_deleted = 0 OR ci.is_deleted IS NULL))) " +
                        "AND NOT EXISTS (SELECT 1 FROM answer a " +
                        "                WHERE a.question_id = q.id " +
                        "                AND (a.is_deleted = 0 OR a.is_deleted IS NULL)) " +
                        "AND (q.is_deleted = 0 OR q.is_deleted IS NULL) " +
                        "AND (c.is_deleted = 0 OR c.is_deleted IS NULL) " +
                        "ORDER BY q.created_date DESC " +
                        "LIMIT :limit")
                .setParameter("instructorId", instructorId)
                .setParameter("limit", limit);

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> getInstructorCourses(Long instructorId, int limit) {
        Query query = entityManager.createNativeQuery(
                "SELECT c.id AS courseId, c.title AS courseTitle, " +
                        "COUNT(DISTINCT e.id) AS enrollmentCount, " +
                        "COALESCE(SUM(CASE WHEN o.status = 'PAID' THEN oi.final_price_cents ELSE 0 END), 0) AS revenue, "
                        +
                        "c.rating_avg AS ratingAvg, c.rating_count AS ratingCount " +
                        "FROM course c " +
                        "LEFT JOIN enrollment e ON c.id = e.course_id AND (e.is_deleted = 0 OR e.is_deleted IS NULL) " +
                        "LEFT JOIN order_item oi ON c.id = oi.course_id AND (oi.is_deleted = 0 OR oi.is_deleted IS NULL) "
                        +
                        "LEFT JOIN `order` o ON oi.order_id = o.id AND (o.is_deleted = 0 OR o.is_deleted IS NULL) " +
                        "WHERE (c.creator_user_id = :instructorId " +
                        "       OR EXISTS (SELECT 1 FROM course_instructor ci " +
                        "                  WHERE ci.course_id = c.id AND ci.user_id = :instructorId " +
                        "                  AND (ci.is_deleted = 0 OR ci.is_deleted IS NULL))) " +
                        "AND (c.is_deleted = 0 OR c.is_deleted IS NULL) " +
                        "GROUP BY c.id, c.title, c.rating_avg, c.rating_count " +
                        "ORDER BY enrollmentCount DESC " +
                        "LIMIT :limit")
                .setParameter("instructorId", instructorId)
                .setParameter("limit", limit);

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> getInstructorCourseCompletions(Long instructorId, int limit) {
        // completionRate = completedLessons / (enrollments * totalLessons)
        Query query = entityManager.createNativeQuery(
                "SELECT c.id AS courseId, c.title AS courseTitle, " +
                        "COUNT(DISTINCT cl.lesson_id) AS totalLessons, " +
                        "COUNT(DISTINCT e.id) AS totalEnrollments, " +
                        "COUNT(DISTINCT CASE WHEN p.completed = 1 THEN CONCAT(p.enrollment_id, '-', p.lesson_id) END) AS totalCompletedLessons, "
                        +
                        "CASE WHEN COUNT(DISTINCT e.id) * COUNT(DISTINCT cl.lesson_id) > 0 " +
                        "     THEN (COUNT(DISTINCT CASE WHEN p.completed = 1 THEN CONCAT(p.enrollment_id, '-', p.lesson_id) END) * 100.0 / "
                        +
                        "           (COUNT(DISTINCT e.id) * COUNT(DISTINCT cl.lesson_id))) " +
                        "     ELSE 0 END AS completionRate " +
                        "FROM course c " +
                        "LEFT JOIN course_lesson cl ON c.id = cl.course_id AND (cl.is_deleted = 0 OR cl.is_deleted IS NULL) "
                        +
                        "LEFT JOIN enrollment e ON c.id = e.course_id AND (e.is_deleted = 0 OR e.is_deleted IS NULL) " +
                        "LEFT JOIN progress p ON p.enrollment_id = e.id AND p.lesson_id = cl.lesson_id " +
                        "     AND (p.is_deleted = 0 OR p.is_deleted IS NULL) " +
                        "WHERE (c.creator_user_id = :instructorId " +
                        "       OR EXISTS (SELECT 1 FROM course_instructor ci " +
                        "                  WHERE ci.course_id = c.id AND ci.user_id = :instructorId " +
                        "                  AND (ci.is_deleted = 0 OR ci.is_deleted IS NULL))) " +
                        "AND (c.is_deleted = 0 OR c.is_deleted IS NULL) " +
                        "GROUP BY c.id, c.title " +
                        "ORDER BY completionRate DESC " +
                        "LIMIT :limit")
                .setParameter("instructorId", instructorId)
                .setParameter("limit", limit);

        return query.getResultList();
    }
}
