package com.theanh.lms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

@ConfigurationProperties(prefix = "file.storage")
public class StorageProperties {

    /**
     * Base path on the local filesystem where files are stored.
     */
    private String basePath = "./storage";

    /**
     * Public base URL used to build accessible links for stored files.
     */
    private String publicBaseUrl = "/files/";

    public Path resolvedBasePath() {
        return Paths.get(basePath).toAbsolutePath().normalize();
    }

    public String buildPublicBaseUrl() {
        if (!StringUtils.hasText(publicBaseUrl)) {
            return null;
        }
        return publicBaseUrl.endsWith("/") ? publicBaseUrl : publicBaseUrl + "/";
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }
}
