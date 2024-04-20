package com.example.cli.s3.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "s3.bucket")
public class S3BucketProperties {
    private String dev;
    private String prod;
    private String staging;
}