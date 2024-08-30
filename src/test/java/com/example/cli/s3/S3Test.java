package com.example.cli.s3;

import com.robothy.s3.rest.LocalS3;
import com.robothy.s3.rest.bootstrap.LocalS3Mode;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

public class S3Test {

    private LocalS3 localS3;
    private S3Client s3Client;

    @BeforeEach
    public void setUp() {
        // Start the local S3 server
        localS3 = LocalS3.builder()
                .port(19090)
                .mode(LocalS3Mode.IN_MEMORY)
                .build();

        localS3.start();

        // Configure the S3 client to use the local server
        s3Client = S3Client.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("accessKey", "secretKey")))
                .endpointOverride(URI.create("http://localhost:19090"))
                .build();
    }

    @AfterEach
    public void tearDown() {
        // Stop the local S3 server
        localS3.shutdown();
    }

    @Test
    public void testS3BucketCreation() {
        // Test creating a bucket
        String bucketName = "my-test-bucket";
        CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                .bucket(bucketName)
                .build();

        s3Client.createBucket(createBucketRequest);

        ListBucketsResponse bucketsResponse = s3Client.listBuckets();

        // Add more assertions to verify the bucket was created, list buckets, etc.
        assertTrue(bucketsResponse.buckets().stream()
                .anyMatch(b -> b.name().equals(bucketName)));
    }
}

