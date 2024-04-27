package com.example.cli.s3.Service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
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
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

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

    public String createBucket(String name) {
        CreateBucketRequest bucketRequest = CreateBucketRequest.builder()
                .bucket(name)
                .build();

        s3Client.createBucket(bucketRequest);
        return "Bucket created successfully!";
    }
}