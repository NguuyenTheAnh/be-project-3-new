package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.ContentReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContentReportRepository extends BaseRepository<ContentReport, Long> {

    @Query(value = """
            SELECT * FROM content_report cr
            WHERE cr.id = :id
              AND (cr.is_deleted IS NULL OR cr.is_deleted = 0)
            """, nativeQuery = true)
    Optional<ContentReport> findActiveById(@Param("id") Long id);

    @Query(value = """
            SELECT * FROM content_report cr
            WHERE cr.target_type = :targetType
              AND cr.target_id = :targetId
              AND cr.reporter_user_id = :userId
              AND (cr.is_deleted IS NULL OR cr.is_deleted = 0)
            """, nativeQuery = true)
    Optional<ContentReport> findByTargetAndUser(@Param("targetType") String targetType,
                                                @Param("targetId") Long targetId,
                                                @Param("userId") Long userId);

    @Query(value = """
            SELECT * FROM content_report cr
            WHERE (cr.is_deleted IS NULL OR cr.is_deleted = 0)
            ORDER BY cr.created_date DESC
            """,
            countQuery = """
            SELECT COUNT(1) FROM content_report cr
            WHERE (cr.is_deleted IS NULL OR cr.is_deleted = 0)
            """,
            nativeQuery = true)
    Page<ContentReport> findAllActive(Pageable pageable);
}
