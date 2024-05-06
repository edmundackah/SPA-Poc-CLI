package com.example.cli.s3.command;

import com.example.cli.s3.Service.S3Service;
import com.example.cli.s3.constants.HelpMessages;
import com.example.cli.s3.context.TenantContext;
import com.example.cli.s3.enums.EnvironmentEnums;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import software.amazon.awssdk.core.exception.SdkClientException;

@Slf4j
@ShellComponent
@ShellCommandGroup("ECS S3 Admin Commands")
public class AdminCommands {

    private final S3Service s3Service;
    private final TenantContext tenantContext;

    @Autowired
    public AdminCommands(S3Service s3Service, TenantContext tenantContext) {
        this.s3Service = s3Service;
        this.tenantContext = tenantContext;
    }

    @ShellMethod(key = "get-objects")
    public String objects(@ShellOption(help = HelpMessages.ENV) String env) throws SdkClientException {
        String bucketName = tenantContext.getBucketName(EnvironmentEnums.fromString(env));
        return s3Service.listBucketContents(bucketName);
    }

    @ShellMethod(key = "create-bucket")
    public String createBucket(@ShellOption String bucketName) {
        s3Service.createBucket(bucketName);
        return "Bucket created successfully!";
    }
}
