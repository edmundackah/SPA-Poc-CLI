package com.example.cli.s3.command;

import com.example.cli.s3.constants.HelpMessages;
import com.example.cli.s3.enums.TargetServer;
import com.example.cli.s3.service.DeployService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;

@Slf4j
@Validated
@RequiredArgsConstructor
@Command(group = "Deploy To S3")
public class DeployCommand {

    @Autowired
    private final DeployService deployService;

    @Command(command = "deploy-snapshot")
    public String deploySnapshot(@Option(required = true, description = HelpMessages.PREFIX) String prefix,
                                 @Option(required = true, description = HelpMessages.BUCKET_NAME) String bucketName,
                                 @Option(required = true, description = HelpMessages.FOLDER_PATH) String folderPath,
                                 @Option(required = true, description = HelpMessages.TARGET_SERVER) TargetServer server) throws IOException {

        return deployService.deploySnapshot(bucketName, prefix, folderPath, server, null);
    }

    @Command(command = "verify")
    public String verify(@Option(description = HelpMessages.CHANGE_RECORD) String changeRecord,
                         @Option(required = true, description = HelpMessages.PREFIX) String prefix,
                         @Option(required = true, description = HelpMessages.BUCKET_NAME) String bucketName,
                         @Option(required = true, description = HelpMessages.TARGET_SERVER) TargetServer server) {

        return deployService.verifyObjectsExist(bucketName, prefix, server, changeRecord);
    }

    @Command(command = "remove")
    public String remove(@Option(description = HelpMessages.CHANGE_RECORD) String changeRecord,
                         @Option(required = true, description = HelpMessages.PREFIX) String prefix,
                         @Option(required = true, description = HelpMessages.BUCKET_NAME) String bucketName,
                         @Option(required = true, description = HelpMessages.TARGET_SERVER) TargetServer server) {

        return deployService.remove(bucketName, prefix, server, changeRecord);
    }

    @Command(command = "deploy-release")
    public String deployRelease(@Option(description = HelpMessages.CHANGE_RECORD) String changeRecord,
                                @Option(required = true, description = HelpMessages.PREFIX) String prefix,
                                @Option(required = true, description = HelpMessages.ARTIFACT_URL) String url,
                                @Option(required = true, description = HelpMessages.BUCKET_NAME) String bucketName,
                                @Option(required = true, description = HelpMessages.TARGET_SERVER) TargetServer server) throws IOException {

        return deployService.deployRelease(url, bucketName, prefix, server, changeRecord);
    }
}
