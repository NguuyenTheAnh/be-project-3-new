package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.UserRole;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends BaseRepository<UserRole, Long> {
    List<UserRole> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
