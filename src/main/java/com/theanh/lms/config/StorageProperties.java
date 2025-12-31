package com.theanh.lms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "file.storage")
public class StorageProperties {

    /**
     * Public base URL used to build accessible links for stored files.
     */
    private String publicBaseUrl = "/files/";

    public String buildPublicBaseUrl() {
        if (!StringUtils.hasText(publicBaseUrl)) {
            return null;
        }
        return publicBaseUrl.endsWith("/") ? publicBaseUrl : publicBaseUrl + "/";
    }

    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }
}
