package com.example.cli.s3.command;

import com.example.cli.s3.Service.S3Service;
import com.example.cli.s3.context.TenantContext;
import com.example.cli.s3.enums.EnvironmentEnums;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class HelloWorldCommand {

    private final S3Service s3Service;
    private final TenantContext tenantContext;

    @Autowired
    public HelloWorldCommand(S3Service s3Service, TenantContext tenantContext) {
        this.s3Service = s3Service;
        this.tenantContext = tenantContext;
    }

    @ShellMethod(key = "greet")
    public String greet(@ShellOption(defaultValue = "bob") String name) {
        return "Hello there, " + name + "!";
    }

    @ShellMethod(key = "marco")
    public String marco() {
        return "polo";
    }

    @ShellMethod(key = "get-objects")
    public String objects(@ShellOption(defaultValue = "dev") String environment) {
        String bucketName = tenantContext.getBucketName(EnvironmentEnums.fromString(environment));
        return s3Service.listBucketContents(bucketName);
    }

    @ShellMethod(key = "create-bucket")
    public String createBucket(@ShellOption String bucketName) {
        return s3Service.createBucket(bucketName);
    }
}
