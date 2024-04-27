package com.example.cli.s3.command;

import com.example.cli.s3.Service.S3Service;
import com.example.cli.s3.context.TenantContext;
import com.example.cli.s3.enums.EnvironmentEnums;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;

@Slf4j
@ShellComponent
@ShellCommandGroup("Deploy ECS S3")
public class DeployCommand {

    private final S3Service s3Service;
    private final TenantContext tenantContext;

    @Autowired
    public DeployCommand(S3Service s3Service, TenantContext tenantContext) {
        this.s3Service = s3Service;
        this.tenantContext = tenantContext;
    }

    @ShellMethod(key= "deploy-snapshot")
    public String deploySnapshot(@ShellOption(defaultValue = "dev") String env,
                                 @ShellOption String buildPath,
                                 @ShellOption String prefix) {

        String bucketName = tenantContext.getBucketName(EnvironmentEnums.fromString(env));
        s3Service.putObjects(buildPath, bucketName, prefix);
        return "Upload Successful";
    }
}
