package com.capstone.kakas.crawlingdb.domain.enums;

import java.util.Arrays;

public enum ProductCategory {
    NINTENDO(1),
    PLAYSTATION(2),
    XBOX(3),
    PC(4),
    ETC(5);

    private final int code;

    ProductCategory(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ProductCategory fromCode(int code) {
        return Arrays.stream(ProductCategory.values())
                .filter(c -> c.getCode() == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid category code: " + code));
    }
}