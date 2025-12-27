package com.theanh.lms.service;

import com.theanh.common.base.BaseService;
import com.theanh.lms.dto.RoleDto;
import com.theanh.lms.entity.Role;

import java.util.Optional;

public interface RoleService extends BaseService<Role, RoleDto, Long> {
    Optional<Role> findByName(String name);

    Role ensureRole(String name, String description);
}
