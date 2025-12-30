package com.theanh.lms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file.storage.presign")
public class PresignProperties {
    private long putExpirySeconds = 600;
    private long getExpirySeconds = 300;

    public long getPutExpirySeconds() {
        return putExpirySeconds;
    }

    public void setPutExpirySeconds(long putExpirySeconds) {
        this.putExpirySeconds = putExpirySeconds;
    }

    public long getGetExpirySeconds() {
        return getExpirySeconds;
    }

    public void setGetExpirySeconds(long getExpirySeconds) {
        this.getExpirySeconds = getExpirySeconds;
    }
}
