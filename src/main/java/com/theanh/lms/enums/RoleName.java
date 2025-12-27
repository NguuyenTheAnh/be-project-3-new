package com.theanh.lms.enums;

public enum RoleName {
    STUDENT,
    INSTRUCTOR,
    ADMIN;

    public String asAuthority() {
        return "ROLE_" + name();
    }
}
