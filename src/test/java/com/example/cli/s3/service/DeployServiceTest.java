package com.example.cli.s3.service;

import com.example.cli.s3.factory.S3ClientFactory;
import com.example.cli.s3.stubs.ArtifactoryStubs;
import com.example.cli.s3.stubs.SnowBrokerStubs;
import com.example.cli.s3.stubs.beans.TestS3Config;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.robothy.s3.core.exception.BucketNotExistException;
import com.robothy.s3.rest.LocalS3;
import com.robothy.s3.rest.bootstrap.LocalS3Mode;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.shell.test.autoconfigure.ShellTest;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.*;

import static com.example.cli.s3.enums.TargetServer.*;
import static com.github.tomakehurst.wiremock.client.WireMock.listAllStubMappings;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@ShellTest
@ActiveProfiles("test")
@Import(TestS3Config.class)
@AutoConfigureWireMock(port = 0)
@ComponentScan(basePackages = "com.example.cli.s3")
class DeployServiceTest {

    @Value("${snow.broker.base.url}")
    private String wiremockBaseUrl;

    @Autowired
    private DeployService deployService;

    @Autowired
    private S3ClientFactory s3ClientFactory;

    public static LocalS3 localS3;

    @BeforeAll
    public static void setup() {
        localS3 = LocalS3.builder()
                .mode(LocalS3Mode.IN_MEMORY)
                .port(19097)
                .build();

        localS3.start();
    }

    @Test
    public void GivenFolder_ShouldUploadToS3() throws IOException {
        S3Client s3Client = s3ClientFactory.getS3Client(AWS_S3, null);

        String prefix = "snapshot-test";
        String bucketName = "test-bucket";
        String folderPath = "src/test/resources/snapshot-test";

        s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());

        String response = deployService.deploySnapshot(bucketName, prefix, folderPath, AWS_S3, null);

        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();

        List<S3Object> objects = s3Client.listObjectsV2(listObjectsRequest).contents();
        ArrayList<String> keys = new ArrayList<>(objects.stream().map(S3Object::key).toList());

        List<String> expectedKeys = Arrays.asList("snapshot-test/outer.txt",
                "snapshot-test/snapshot/inner.txt", "snapshot-test/outer - Copy.txt");

        //sort the keys in alphabetical order
        Collections.sort(keys);
        Collections.sort(expectedKeys);

        assertEquals(expectedKeys, keys);
        assertEquals(3, objects.size());
        assertEquals("Folder processed and uploaded successfully", response);
    }

    @Test
    public void GivenValidChangeRecord_ShouldDeployToPROD() throws IOException {
        SnowBrokerStubs.validChangeRecordStub();
        ArtifactoryStubs.tarFile();

        // Retrieve all stubs
        List<StubMapping> allStubs = listAllStubMappings().getMappings();

        S3Client s3Client = s3ClientFactory.getS3Client(ECS_S3, null);

        String bucketName = "prod";
        String mcr = "MCR18434340";
        String prefix = "snapshot-release";
        String url = StringUtils.join(wiremockBaseUrl, "/download/prod-payload.tgz");

        s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());

        String response = deployService.deployRelease(url, bucketName, prefix, AWS_S3_PROD, mcr);

        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();

        List<S3Object> objects = s3Client.listObjectsV2(listObjectsRequest).contents();
        ArrayList<String> keys = new ArrayList<>(objects.stream().map(S3Object::key).toList());

        List<String> expectedKeys = Arrays.asList("snapshot-test/outer.txt",
                "snapshot-test/snapshot/inner.txt", "snapshot-test/outer - Copy.txt");

        //sort the keys in alphabetical order
        Collections.sort(keys);
        Collections.sort(expectedKeys);

        assertEquals(expectedKeys, keys);
        assertEquals(3, objects.size());
        assertEquals("Folder processed and uploaded successfully", response);
    }

    @Test
    public void GivenMissingBucket_ShouldReturnErrorMessage() throws IOException {
        String folderPath = "src/test/resources/snapshot-test";

        Exception exception = assertThrows(NoSuchBucketException.class, () -> {
            deployService.deploySnapshot("bucket", "snapshot-test", folderPath, ECS_S3, null);
        });

        String expectedMessage = "Bucket 'bucket' not exist.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void GivenEmptyFolderPath_ShouldReturnErrorMessage() throws IOException {
        String response = deployService.deploySnapshot("test-bucket", "p", "tomato", AWS_S3, null);

        assertEquals("Invalid folder path or folder is empty.", response);
    }

    @AfterAll
    public static void teardown() {
        localS3.shutdown();
    }
}
