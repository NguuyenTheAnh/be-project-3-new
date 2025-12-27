package com.theanh.lms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.role-mapping")
public class RoleMappingProperties {

    /**
     * Location of role mapping properties file (e.g., classpath:role.properties).
     */
    private String location = "classpath:role.properties";

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
