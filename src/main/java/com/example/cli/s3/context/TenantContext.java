package com.example.cli.s3.context;

import com.example.cli.s3.enums.EnvironmentEnums;
import com.example.cli.s3.properties.S3BucketProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TenantContext {

    private final S3BucketProperties s3BucketProperties;

    @Autowired
    public TenantContext(S3BucketProperties s3BucketProperties) {
        this.s3BucketProperties = s3BucketProperties;
    }

    public String getBucketName(EnvironmentEnums environment) {
        return switch (environment) {
            case DEV -> s3BucketProperties.getDev();
            case PROD -> s3BucketProperties.getProd();
            case STAGING -> s3BucketProperties.getStaging();
            default -> throw new IllegalArgumentException("Unsupported environment: " + environment);
        };
    }

    //Overloaded method, defaults to DEV if environment is not set
    public String getBucketName() {
        return s3BucketProperties.getDev();
    }
}
