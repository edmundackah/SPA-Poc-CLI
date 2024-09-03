package com.example.cli.s3.util;

import com.example.cli.s3.stubs.ArtifactoryStubs;
import com.example.cli.s3.stubs.SnowBrokerStubs;
import com.example.cli.s3.stubs.beans.TestS3Config;
import com.robothy.s3.rest.LocalS3;
import com.robothy.s3.rest.bootstrap.LocalS3Mode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.shell.test.autoconfigure.ShellTest;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Slf4j
@ShellTest
@ActiveProfiles("test")
@Import(TestS3Config.class)
@AutoConfigureWireMock(port = 0)
@ComponentScan(basePackages = "com.example.cli.s3")
public abstract class BaseS3IntegrationTest {

    @Value("${ecs.s3.region}")
    private String region;

    @Value("${ecs.s3.endpoint.url}")
    private String endpointUrl;

    @Value("${snow.broker.base.url}")
    public String wiremockBaseUrl;

    public static LocalS3 localS3;

    public S3Client testS3Client;

    @BeforeEach
    public void setup() throws IOException {
        // Creating Wiremock stubs globally
        SnowBrokerStubs.validChangeRecordStub();
        ArtifactoryStubs.tarFile();

        localS3 = LocalS3.builder()
                .mode(LocalS3Mode.IN_MEMORY)
                .port(19097)
                .build();

        localS3.start();

        testS3Client = getTestClient();
    }

    // Utility method to create a bucket for tests
    protected static void createBucket(S3Client s3Client, String bucketName) {
        s3Client.createBucket(b -> b.bucket(bucketName));
    }

    // Utility method to upload folders to S3
    protected static void uploadFile(S3Client s3Client, String bucketName, String dir, String prefix) {
        createBucket(s3Client, bucketName);
        uploadDirectoryToS3(s3Client, bucketName, dir, prefix);
    }

    // Utility method to upload folders to S3
    protected static void uploadDirectory(S3Client s3Client, String bucketName, String dir, String prefix) {
        createBucket(s3Client, bucketName);
        uploadDirectoryToS3(s3Client, bucketName, dir, prefix);
    }

    // Utility function to upload objects
    protected static void uploadDirectoryToExistingBucket(S3Client s3Client, String bucketName, String dir, String prefix) {
        uploadDirectoryToS3(s3Client, bucketName, dir, prefix);
    }

    // Utility function to upload single file
    public static void uploadFileToS3(S3Client s3Client, String bucketName, String objectKey, String filePath) {
        try {
            // Create a PutObjectRequest
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            // Upload the file
            PutObjectResponse response = s3Client.putObject(putObjectRequest, RequestBody.fromFile(Paths.get(filePath)));

            // Optionally, you can use the response to check the status or metadata of the uploaded object
            log.debug("File uploaded successfully. ETag: " + response.eTag());

        } catch (Exception e) {
            log.error("Failed to upload file to S3 ", e);
        }
    }

    protected static byte[] downloadObject(S3Client s3Client, String bucketName, String objectName) {
        try {
            // Create a GetObjectRequest
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectName)
                    .build();

            // Download the object as a byte array
            byte[] objectBytes = s3Client.getObjectAsBytes(getObjectRequest).asByteArray();

            log.debug("Object downloaded successfully as a byte array");
            return objectBytes;

        } catch (Exception e) {
            log.error("Failed to download object from S3", e);
            return null;
        }
    }

    private S3Client getTestClient() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("accessKey", "secretKey")))
                .endpointOverride(URI.create(endpointUrl))
                .build();
    }

    private static void uploadDirectoryToS3(S3Client s3Client, String bucketName, String dir, String prefix) {
        try (Stream<Path> paths = Files.walk(Paths.get(dir))) {

            paths.filter(Files::isRegularFile).forEach(filePath -> {
                String key = prefix + "/" + filePath.toString()
                        .replace(dir, "")
                        .replace(File.separator, "/");

                uploadFile(s3Client, bucketName, filePath, key);
            });
        } catch (Exception e) {
            log.error("Error while uploading to S3", e);
        }
    }

    private static void uploadFile(S3Client s3Client, String bucketName, Path filePath, String key) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromFile(filePath));
            log.debug("Uploaded: {}  to S3 as {}", filePath, key);
        } catch (S3Exception e) {
            log.error(e.awsErrorDetails().errorMessage());
        }
    }

    @AfterEach
    public void cleanup() {
        localS3.shutdown();
    }

    @AfterAll
    public static void teardown() {
        localS3.shutdown();
    }
}

