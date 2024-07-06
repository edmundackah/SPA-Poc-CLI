package com.example.cli.s3.enums;

import lombok.Getter;

@Getter
public enum FlagState {
    ON("on"),
    OFF("off");

    private final String value;

    FlagState(String value) {
        this.value = value;
    }

}

