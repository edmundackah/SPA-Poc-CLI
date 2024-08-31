package com.example.cli.s3.util;

import com.example.cli.s3.factory.S3ClientFactory;
import com.example.cli.s3.service.DeployService;
import com.example.cli.s3.stubs.beans.TestS3Config;
import com.robothy.s3.rest.LocalS3;
import com.robothy.s3.rest.bootstrap.LocalS3Mode;
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
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

import static com.example.cli.s3.enums.TargetServer.AWS_S3;

@ShellTest
@ActiveProfiles("test")
@Import(TestS3Config.class)
@AutoConfigureWireMock(port = 0)
@ComponentScan(basePackages = "com.example.cli.s3")
public abstract class BaseS3IntegrationTest {

    @Value("${snow.broker.base.url}")
    public String wiremockBaseUrl;

    public static LocalS3 localS3;

    @BeforeAll
    public static void setup() {
        localS3 = LocalS3.builder()
                .mode(LocalS3Mode.IN_MEMORY)
                .port(19097)
                .build();

        localS3.start();
    }

    // Utility method to create a bucket for tests
    protected static void createBucket(S3Client s3Client, String bucketName) {
        s3Client.createBucket(b -> b.bucket(bucketName));
    }

    // Add other common utility methods as needed...

    @AfterAll
    public static void teardown() {
        localS3.shutdown();
    }
}

