package com.caro.bizkit.common.S3.dto;

public enum UploadCategory {
    PROFILE("profile"),
    QR("qr"),
    AI("ai");

    private final String prefix;

    UploadCategory(String prefix) {
        this.prefix = prefix;
    }

    public String prefix() {
        return prefix;
    }
}
