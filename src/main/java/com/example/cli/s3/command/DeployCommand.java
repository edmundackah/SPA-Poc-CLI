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
    public String deploySnapshot(@ShellOption(help = HelpMessages.ENV) String env,
                                 @ShellOption(help = HelpMessages.BUILD_PATH) String buildPath,
                                 @ShellOption(help = HelpMessages.PREFIX) String prefix) {

        String bucketName = tenantContext.getBucketName(EnvironmentEnums.fromString(env));
        s3Service.putObjects(buildPath, bucketName, prefix);
        return "Upload Successful";
    }

    @ShellMethod(key= "verify")
    public String verify(@ShellOption(help = HelpMessages.ENV) String env,
                         @ShellOption(help = HelpMessages.PREFIX) String prefix) {
        String bucketName = tenantContext.getBucketName(EnvironmentEnums.fromString(env));
        return s3Service.verifyObjectsExist(bucketName, prefix);
    }
}
