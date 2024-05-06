package com.example.cli.s3.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TableRow {

    private String key;
    private String dateModified;
    private String size;
}
