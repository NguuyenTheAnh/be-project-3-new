package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.Cart;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends BaseRepository<Cart, Long> {

    @Query(value = """
            SELECT * FROM cart c
            WHERE c.user_id = :userId
              AND c.status = 'ACTIVE'
              AND (c.is_deleted IS NULL OR c.is_deleted = 0)
            LIMIT 1
            """, nativeQuery = true)
    Optional<Cart> findActiveByUser(@Param("userId") Long userId);

    @Query(value = """
            SELECT * FROM cart c
            WHERE c.id = :id
              AND (c.is_deleted IS NULL OR c.is_deleted = 0)
            """, nativeQuery = true)
    Optional<Cart> findActiveById(@Param("id") Long id);
}
