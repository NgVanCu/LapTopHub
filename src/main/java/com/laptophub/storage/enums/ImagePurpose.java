package com.laptophub.storage.enums;

public enum ImagePurpose {

    PRODUCT_IMAGE("products"),

    BRAND_LOGO("brands");

    private final String path;

    ImagePurpose(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }
}