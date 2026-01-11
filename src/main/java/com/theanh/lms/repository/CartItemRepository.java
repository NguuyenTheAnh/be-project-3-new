package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.CartItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends BaseRepository<CartItem, Long> {

    @Query(value = """
            SELECT * FROM cart_item ci
            WHERE ci.cart_id = :cartId
              AND (ci.is_deleted IS NULL OR ci.is_deleted = 0)
            ORDER BY ci.created_date DESC
            """, nativeQuery = true)
    List<CartItem> findByCartId(@Param("cartId") Long cartId);

    @Query(value = """
            SELECT * FROM cart_item ci
            WHERE ci.cart_id = :cartId
              AND ci.course_id = :courseId
              AND (ci.is_deleted IS NULL OR ci.is_deleted = 0)
            LIMIT 1
            """, nativeQuery = true)
    Optional<CartItem> findByCartAndCourse(@Param("cartId") Long cartId,
                                           @Param("courseId") Long courseId);
}
