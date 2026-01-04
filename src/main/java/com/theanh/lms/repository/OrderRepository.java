package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.Order;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends BaseRepository<Order, Long> {

    @Query(value = """
            SELECT * FROM `order` o
            WHERE o.id = :id
              AND (o.is_deleted IS NULL OR o.is_deleted = 0)
            """, nativeQuery = true)
    Optional<Order> findActiveById(@Param("id") Long id);

    @Query(value = """
            SELECT * FROM `order` o
            WHERE o.user_id = :userId
              AND o.status = :status
              AND (o.is_deleted IS NULL OR o.is_deleted = 0)
            ORDER BY o.created_date DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<Order> findLatestByUserAndStatus(@Param("userId") Long userId,
                                              @Param("status") String status);

    @Query(value = """
            SELECT * FROM `order` o
            WHERE o.user_id = :userId
              AND (o.is_deleted IS NULL OR o.is_deleted = 0)
            ORDER BY o.created_date DESC
            """,
            countQuery = """
            SELECT COUNT(1) FROM `order` o
            WHERE o.user_id = :userId
              AND (o.is_deleted IS NULL OR o.is_deleted = 0)
            """,
            nativeQuery = true)
    org.springframework.data.domain.Page<Order> findByUser(@Param("userId") Long userId,
                                                           org.springframework.data.domain.Pageable pageable);

    @Query(value = """
            SELECT * FROM `order` o
            WHERE (o.is_deleted IS NULL OR o.is_deleted = 0)
            ORDER BY o.created_date DESC
            """,
            countQuery = """
            SELECT COUNT(1) FROM `order` o
            WHERE (o.is_deleted IS NULL OR o.is_deleted = 0)
            """,
            nativeQuery = true)
    org.springframework.data.domain.Page<Order> findAllActive(org.springframework.data.domain.Pageable pageable);
}
