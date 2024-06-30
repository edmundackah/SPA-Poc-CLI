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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpointUrl))
                .region(Region.of(awsRegion))// Must be valid AWS Region
                .forcePathStyle(true)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    public String putObjects(String buildPath, String bucketName, String prefix) throws SdkClientException, IOException {
        File directory = new File(buildPath);
        File[] files = directory.listFiles();

        if (files == null) {
            throw new IllegalArgumentException("No objects found at " + directory.getPath());
        } else {
            for (File file : files) {
                String objectKey = StringUtils.join(prefix, file.getName());

                PutObjectRequest request = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .build();

                s3Client.putObject(request, RequestBody.fromFile(file));
                log.debug("Uploaded: {}", objectKey);
            }
            return "Upload task complete";
        }
    }

    public String listBucketContents(String bucketName) throws SdkClientException {
        log.info("Using bucket {}", bucketName);

        ListObjectsRequest request = ListObjectsRequest.builder()
                .bucket(String.valueOf(bucketName))
                .build();

        return s3Client.listObjects(request)
                .contents().stream()
                .map(S3Object::key)
                .collect(Collectors.joining("\n"));
    }

    public String verifyObjectsExist(String bucketName, String prefix) throws SdkClientException {
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

    public void createBucket(String name) throws SdkClientException {
        CreateBucketRequest bucketRequest = CreateBucketRequest.builder()
                .bucket(name)
                .build();

        s3Client.createBucket(bucketRequest);
    }

    public void uploadDirectoryToS3(File directory, String bucketName, String prefix) throws IOException {

        try (Stream<Path> paths = Files.walk(directory.toPath())) {
            paths.filter(Files::isRegularFile)
                    .forEach(filePath -> uploadFileToS3(s3Client, bucketName, prefix, directory.toPath(), filePath));
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    private void uploadFileToS3(S3Client s3Client, String bucketName, String prefix, Path rootDir, Path filePath) {
        String key = prefix + "/" + rootDir.relativize(filePath).toString().replace("\\", "/");

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.putObject(putRequest, filePath);

        log.info("Uploaded: {} to S3 bucket {} with key {}", filePath, bucketName, key);
    }

}