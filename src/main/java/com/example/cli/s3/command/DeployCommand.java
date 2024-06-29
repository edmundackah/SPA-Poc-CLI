package com.example.cli.s3.command;

import com.example.cli.s3.Service.S3Service;
import com.example.cli.s3.constants.HelpMessages;
import com.example.cli.s3.context.TenantContext;
import com.example.cli.s3.enums.EnvironmentEnums;
import com.example.cli.s3.utils.FileExtractorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.io.IOException;

@Slf4j
@ShellComponent
@ShellCommandGroup("Deploy ECS S3")
public class DeployCommand {

    private final S3Service s3Service;
    private final TenantContext tenantContext;
    private final FileExtractorUtil fileExtractorUtil;

    @Autowired
    public DeployCommand(S3Service s3Service, TenantContext tenantContext, FileExtractorUtil fileExtractorUtil) {
        this.s3Service = s3Service;
        this.fileExtractorUtil = fileExtractorUtil;
        this.tenantContext = tenantContext;
    }

    @ShellMethod(key= "deploy-snapshot")
    public String deploySnapshot(@ShellOption(help = HelpMessages.ENV) String env,
                                 @ShellOption(help = HelpMessages.BUILD_PATH) String buildPath,
                                 @ShellOption(help = HelpMessages.PREFIX) String prefix) throws IOException {

        String bucketName = tenantContext.getBucketName(EnvironmentEnums.fromString(env));
        return s3Service.putObjects(buildPath, bucketName, prefix);
    }

    @ShellMethod(key= "verify")
    public String verify(@ShellOption(help = HelpMessages.ENV) String env,
                         @ShellOption(help = HelpMessages.PREFIX) String prefix) {
        String bucketName = tenantContext.getBucketName(EnvironmentEnums.fromString(env));
        return s3Service.verifyObjectsExist(bucketName, prefix);
    }

    @ShellMethod(key= "deploy-folder")
    public String deployFolder(@ShellOption(help = HelpMessages.ENV) String env,
                                 @ShellOption(help = HelpMessages.BUILD_PATH) String buildPath,
                                 @ShellOption(help = HelpMessages.PREFIX) String prefix) throws IOException {

        String bucketName = tenantContext.getBucketName(EnvironmentEnums.fromString(env));

        File folder = new File(buildPath);

        // Validate that the folder exists and is not empty
        if (!folder.exists() || !folder.isDirectory() || folder.listFiles() == null || folder.listFiles().length == 0) {
            return "Invalid folder path or folder is empty.";
        }

        // Upload the contents of the folder to S3
        s3Service.uploadDirectoryToS3(folder, bucketName, prefix);

        return "Folder processed and uploaded successfully";
    }

    @ShellMethod(key= "deploy-zip")
    public String deployZip(@ShellOption(help = HelpMessages.ENV) String env,
                                 @ShellOption String application,
                                 @ShellOption(help = HelpMessages.PREFIX) String prefix) throws IOException {

        String bucketName = tenantContext.getBucketName(EnvironmentEnums.fromString(env));
        String url = StringUtils.join("http://localhost:1080/download/", application,".zip");
        String zipFile = StringUtils.join(application, ".zip");

        // Download the zip file
        File zip = fileExtractorUtil.downloadFile(url, zipFile);

        // Extract the zip file
        File extractedDir = fileExtractorUtil.extractZip(zip, new File("extracted_zip"));

        // Upload the contents of the extracted directory to S3
        s3Service.uploadDirectoryToS3(extractedDir, bucketName, prefix);

        // Cleanup
        log.debug("Cleaning up temp files");
        FileUtils.deleteDirectory(new File("extracted_zip"));
        zip.delete();

        return "Zip file processed and uploaded successfully";
    }

    @ShellMethod(key= "deploy-tgz")
    public String deployTar(@ShellOption(help = HelpMessages.ENV) String env,
                            @ShellOption String application,
                            @ShellOption(help = HelpMessages.PREFIX) String prefix) throws IOException {

        String bucketName = tenantContext.getBucketName(EnvironmentEnums.fromString(env));

        String url = StringUtils.join("http://localhost:1080/download/", application,".tgz");
        String tarFile = StringUtils.join(application, ".tgz");

        // Download the .tgz file
        File tgzFile = fileExtractorUtil.downloadFile(url, tarFile);

        // Extract the first .tgz file
        File extractedDir = fileExtractorUtil.extractTgz(tgzFile, new File("extracted"));

        // Find and extract the second .tgz file inside the extracted directory
        File innerTgzFile = new File(extractedDir, tarFile);
        File finalExtractedDir = fileExtractorUtil.extractTgz(innerTgzFile, new File("final_extracted"));

        // Upload the contents of the package/build directory to S3
        File buildDir = new File(finalExtractedDir, "package/build");
        s3Service.uploadDirectoryToS3(buildDir, bucketName, prefix);

        // Cleanup
        log.debug("Cleaning up temp files");
        FileUtils.deleteDirectory(new File("extracted"));
        FileUtils.deleteDirectory(new File("final_extracted"));

        tgzFile.delete();
        return "File processed and uploaded successfully";
    }
}
