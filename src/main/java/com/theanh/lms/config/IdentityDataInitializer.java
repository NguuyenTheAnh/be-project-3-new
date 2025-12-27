package com.theanh.lms.config;

import com.theanh.lms.enums.RoleName;
import com.theanh.lms.service.RoleService;
import com.theanh.lms.service.UserService;
import com.theanh.lms.dto.request.UserCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class IdentityDataInitializer implements ApplicationRunner {

    private final RoleService roleService;
    private final UserService userService;

    @Override
    public void run(ApplicationArguments args) {
        roleService.ensureRole(RoleName.ADMIN.name(), "Administrator");
        roleService.ensureRole(RoleName.INSTRUCTOR.name(), "Instructor");
        roleService.ensureRole(RoleName.STUDENT.name(), "Student");

        if (userService.findActiveByEmail("admin@lms.local").isEmpty()) {
            UserCreateRequest request = new UserCreateRequest();
            request.setEmail("admin@lms.local");
            request.setPassword("admin");
            request.setFullName("Administrator");
            request.setRoles(Set.of(RoleName.ADMIN.name()));
            userService.createUser(request);
        }
    }
}
