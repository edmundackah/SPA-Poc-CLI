package com.example.cli.s3.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MaintenanceFlag {
    private String flag;
    private String state;
}
