package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.RolePermission;
import org.springframework.stereotype.Repository;

@Repository
public interface RolePermissionRepository extends BaseRepository<RolePermission, Long> {
}
