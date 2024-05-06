package com.example.cli.s3.Service;

import com.example.cli.s3.models.TableRow;
import com.example.cli.s3.utils.Utils;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.Upload;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class S3Service {
    private final S3Client s3Client;

    @Autowired
    public S3Service(@Value("${s3.region}") String awsRegion,
                    @Value("${s3.accessKeyId}") String accessKeyId,
                    @Value("${s3.endpoint.url}") String endpointUrl,
                    @Value("${s3.secretAccessKey}") String secretAccessKey) {

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider
                .create(credentials);

        log.info("Connecting with url {}", endpointUrl);
        log.info("Connecting with region {}", Region.of(awsRegion));

        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpointUrl))
                .region(Region.of(awsRegion))// Must be valid AWS Region
                .forcePathStyle(true)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    public void putObjects(String buildPath, String bucketName, String prefix) {
        File directory = new File(buildPath);
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {

                Map<String, String> metadata = new HashMap<>();
                metadata.put("x-amz-meta-myVal", "test");

                String objectKey = StringUtils.join(prefix, file.getName());

                PutObjectRequest request = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .metadata(metadata)
                        .build();

                s3Client.putObject(request, RequestBody.fromFile(file));
                log.debug("Uploaded: {}", objectKey);
            }
        }
    }

    public String listBucketContents(String bucketName) {
        log.info("Using bucket {}", bucketName);

        ListObjectsRequest request = ListObjectsRequest.builder()
                .bucket(String.valueOf(bucketName))
                .build();

        return s3Client.listObjects(request)
                .contents().stream()
                .map(S3Object::key)
                .collect(Collectors.joining("\n"));
    }

    public String verifyObjectsExist(String bucketName, String prefix) {
        // Create request to list objects with the specified prefix
        ListObjectsRequest request = ListObjectsRequest.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();

        ListObjectsResponse response = s3Client.listObjects(request);

        // Data for the ASCII table
        List<TableRow> rows = new ArrayList<>();

        for (S3Object object : response.contents()) {
            rows.add(new TableRow(object.key(), Utils.getUKTimestamp(object.lastModified()), object.size().toString()));
        }

        // Create and print the ASCII table
        return AsciiTable.getTable(rows, Arrays.asList(
                new Column().header("Key").with(TableRow::getKey),
                new Column().header("Date Modified").with(TableRow::getDateModified),
                new Column().header("Size (Bytes)").with(TableRow::getSize)
        ));
    }

    public void createBucket(String name) {
        CreateBucketRequest bucketRequest = CreateBucketRequest.builder()
                .bucket(name)
                .build();

        s3Client.createBucket(bucketRequest);
    }
}