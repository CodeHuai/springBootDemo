package com.huai.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Passworld {
    PASSWORLD("SALT_PREFIX", "::SpringBootDemo::");

    private String key;
    private String value;
}
