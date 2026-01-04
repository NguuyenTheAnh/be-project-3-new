package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.OrderItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends BaseRepository<OrderItem, Long> {

    @Query(value = """
            SELECT * FROM order_item oi
            WHERE oi.order_id = :orderId
              AND (oi.is_deleted IS NULL OR oi.is_deleted = 0)
            """, nativeQuery = true)
    List<OrderItem> findByOrder(@Param("orderId") Long orderId);

    @Query(value = """
            SELECT * FROM order_item oi
            WHERE oi.order_id = :orderId
              AND oi.course_id = :courseId
              AND (oi.is_deleted IS NULL OR oi.is_deleted = 0)
            """, nativeQuery = true)
    java.util.Optional<OrderItem> findByOrderAndCourse(@Param("orderId") Long orderId, @Param("courseId") Long courseId);
}
