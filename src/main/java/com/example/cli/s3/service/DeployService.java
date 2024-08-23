package com.example.cli.s3.service;

import com.example.cli.s3.enums.TargetServer;
import com.example.cli.s3.factory.S3ClientFactory;
import com.example.cli.s3.models.TableRow;
import com.example.cli.s3.utils.FileExtractorUtil;
import com.example.cli.s3.utils.S3Util;
import com.example.cli.s3.utils.Util;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeployService {

    @Autowired
    private final S3Util s3Util;

    @Autowired
    private S3ClientFactory s3ClientFactory;

    @Autowired
    private final FileExtractorUtil fileExtractorUtil;

    public String verifyObjectsExist(String bucketName, String prefix, TargetServer server, String changeRecord) throws SdkClientException {
        // Create request to list objects with the specified prefix
        ListObjectsRequest request = ListObjectsRequest.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();

        ListObjectsResponse response = s3ClientFactory.getS3Client(server, changeRecord).listObjects(request);

        // Data for the ASCII table
        List<TableRow> rows = new ArrayList<>();

        for (S3Object object : response.contents()) {
            rows.add(new TableRow(object.key(), Util.getUKTimestamp(object.lastModified()), object.size().toString()));
        }

        // Create and print the ASCII table
        return AsciiTable.getTable(rows, Arrays.asList(
                new Column().header("Key").with(TableRow::getKey),
                new Column().header("Date Modified").with(TableRow::getDateModified),
                new Column().header("Size (Bytes)").with(TableRow::getSize)
        ));
    }

    public String deploySnapshot(String bucketName, String prefix, String folderPath,
                                 TargetServer server, String changeRecord) throws IOException {
        File folder = new File(folderPath);

        // Validate that the folder exists and is not empty
        if (!folder.exists() || !folder.isDirectory() || folder.listFiles() == null || Objects.requireNonNull(folder.listFiles()).length == 0) {
            return "Invalid folder path or folder is empty.";
        }

        // Upload the contents of the folder to S3
        s3Util.uploadDirectoryToS3(folder, bucketName, prefix, server, changeRecord);

        return "Folder processed and uploaded successfully";
    }

    public String deployRelease(String url, String bucketName, String prefix,
                                TargetServer targetServer, String changeRecord) throws IOException {
        String tarFile = StringUtils.join(UUID.randomUUID().toString(), ".tgz");

        // Download the .tgz file
        File tgzFile = fileExtractorUtil.downloadFile(url, tarFile);

        // Extract the .tgz file
        File extractedDir = fileExtractorUtil.extractTgz(tgzFile, new File("extracted"));

        // Upload the contents of the package/build directory to S3
        File buildDir = new File(extractedDir, "package/build");
        s3Util.uploadDirectoryToS3(buildDir, bucketName, prefix, targetServer, changeRecord);

        // Cleanup
        log.debug("Cleaning up temp files");
        FileUtils.deleteDirectory(new File("extracted"));
        FileUtils.deleteDirectory(new File("final_extracted"));

        tgzFile.delete();
        return "File processed and uploaded successfully";
    }
}
