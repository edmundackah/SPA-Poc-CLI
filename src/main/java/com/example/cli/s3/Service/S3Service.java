package com.example.cli.s3.Service;

import com.example.cli.s3.context.TenantContext;
import com.example.cli.s3.enums.EnvironmentEnums;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.net.URI;
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