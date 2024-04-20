package com.example.cli.s3.enums;

import java.util.HashMap;
import java.util.Map;

public enum EnvironmentEnums {

    DEV,
    STAGING,
    PROD;

    private static final Map<String, EnvironmentEnums> lookup = new HashMap<>();

    static {
        for (EnvironmentEnums env : EnvironmentEnums.values()) {
            lookup.put(env.toString(), env);
        }
    }

    public static EnvironmentEnums fromString(String text) {
        return lookup.get(text);
    }

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
