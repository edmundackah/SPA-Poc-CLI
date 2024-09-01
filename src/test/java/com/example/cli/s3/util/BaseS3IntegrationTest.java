package com.example.cli.s3.util;

import com.example.cli.s3.factory.S3ClientFactory;
import com.example.cli.s3.service.DeployService;
import com.example.cli.s3.stubs.ArtifactoryStubs;
import com.example.cli.s3.stubs.SnowBrokerStubs;
import com.example.cli.s3.stubs.beans.TestS3Config;
import com.robothy.s3.rest.LocalS3;
import com.robothy.s3.rest.bootstrap.LocalS3Mode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
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
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static com.example.cli.s3.enums.TargetServer.AWS_S3;

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
    protected static void uploadDirectory(S3Client s3Client, String bucketName, String dir, String prefix) {
        createBucket(s3Client, bucketName);
        uploadDirectoryToS3(s3Client, bucketName, dir, prefix);
    }

    protected static void uploadDirectoryToExistingBucket(S3Client s3Client, String bucketName, String dir, String prefix) {
        uploadDirectoryToS3(s3Client, bucketName, dir, prefix);
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

