package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepository<User, Long> {
    Optional<User> findByEmailAndIsDeletedFalse(String email);

    @Query(value = "SELECT DISTINCT u.* FROM user u " +
            "INNER JOIN user_roles ur ON u.id = ur.user_id " +
            "INNER JOIN role r ON ur.role_id = r.id " +
            "WHERE (u.is_deleted = 0 OR u.is_deleted IS NULL) " +
            "AND r.name = :roleName " +
            "AND (:keyword IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(u.full_name) LIKE LOWER(CONCAT('%', :keyword, '%')))", countQuery = "SELECT COUNT(DISTINCT u.id) FROM user u "
                    +
                    "INNER JOIN user_roles ur ON u.id = ur.user_id " +
                    "INNER JOIN role r ON ur.role_id = r.id " +
                    "WHERE (u.is_deleted = 0 OR u.is_deleted IS NULL) " +
                    "AND r.name = :roleName " +
                    "AND (:keyword IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "     OR LOWER(u.full_name) LIKE LOWER(CONCAT('%', :keyword, '%')))", nativeQuery = true)
    Page<User> findByRoleNameWithKeyword(@Param("roleName") String roleName,
            @Param("keyword") String keyword,
            Pageable pageable);
}
