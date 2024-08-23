package com.example.cli.s3.command;

import com.example.cli.s3.constants.HelpMessages;
import com.example.cli.s3.enums.TargetServer;
import com.example.cli.s3.service.DeployService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.IOException;

@Slf4j
@ShellComponent
@RequiredArgsConstructor
@ShellCommandGroup("Deploy To S3")
public class DeployCommand {

    @Autowired
    private final DeployService deployService;

    @ShellMethod(key = "deploy-snapshot")
    public String deploySnapshot(@ShellOption(help = HelpMessages.BUCKET_NAME) String bucketName,
                                 @ShellOption(help = HelpMessages.FOLDER_PATH) String folderPath,
                                 @ShellOption(help = HelpMessages.TARGET_SERVER) TargetServer targetServer,
                                 @ShellOption(help = HelpMessages.PREFIX) String prefix) throws IOException {

        return deployService.deploySnapshot(bucketName, prefix, folderPath, targetServer, null);
    }

    @ShellMethod(key = "verify")
    public String verify(@ShellOption(help = HelpMessages.PREFIX) String prefix,
                         @ShellOption(help = HelpMessages.BUCKET_NAME) String bucketName,
                         @ShellOption(help = HelpMessages.TARGET_SERVER) TargetServer targetServer,
                         @ShellOption(defaultValue = "", help = HelpMessages.CHANGE_RECORD) String changeRecord) {

        return deployService.verifyObjectsExist(bucketName, prefix, targetServer, changeRecord);
    }

    @ShellMethod(key = "deploy-release")
    public String deployRelease(@ShellOption(help = HelpMessages.PREFIX) String prefix,
                                @ShellOption(help = HelpMessages.BUCKET_NAME) String bucketName,
                                @ShellOption(help = HelpMessages.ARTIFACT_URL) String url,
                                @ShellOption(help = HelpMessages.TARGET_SERVER) TargetServer targetServer,
                                @ShellOption(defaultValue = "", help = HelpMessages.CHANGE_RECORD) String changeRecord) throws IOException {

        return deployService.deployRelease(url, bucketName, prefix, targetServer, changeRecord);
    }
}
