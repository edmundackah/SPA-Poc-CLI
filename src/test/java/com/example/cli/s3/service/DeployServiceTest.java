package com.example.cli.s3.service;

import com.example.cli.s3.enums.TargetServer;
import com.example.cli.s3.factory.S3ClientFactory;
import com.example.cli.s3.stubs.ArtifactoryStubs;
import com.example.cli.s3.stubs.SnowBrokerStubs;
import com.example.cli.s3.stubs.beans.TestS3Config;
import com.example.cli.s3.util.BaseS3IntegrationTest;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.robothy.s3.core.exception.BucketNotExistException;
import com.robothy.s3.rest.LocalS3;
import com.robothy.s3.rest.bootstrap.LocalS3Mode;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.example.cli.s3.enums.TargetServer.*;
import static com.github.tomakehurst.wiremock.client.WireMock.listAllStubMappings;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

class DeployServiceTest extends BaseS3IntegrationTest {

    @Autowired
    private DeployService deployService;

    @ParameterizedTest
    @CsvSource({
            "AWS_S3, aws-test-bucket",
            "ECS_S3, ecs-test-bucket",
    })
    public void GivenFolder_ShouldUploadToS3(TargetServer server, String bucketName) throws IOException {
        String prefix = "snapshot-test";
        String folderPath = "src/test/resources/snapshot-test";

        createBucket(testS3Client, bucketName);

        String response = deployService.deploySnapshot(bucketName, prefix, folderPath, server, null);

        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();

        List<S3Object> objects = testS3Client.listObjectsV2(listObjectsRequest).contents();
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

    @ParameterizedTest
    @CsvSource({
            "AWS_S3_PROD, inc-prod-bucket, MCR18434340",
            "AWS_S3_PROD, aws-prod-bucket, INC23434114",
            "ECS_S3_PROD, mcr-prod-bucket, MCR18434340",
            "ECS_S3_PROD, ecs-prod-bucket, INC23434114",
    })
    public void GivenValidChangeRecord_ShouldDeployToProd(TargetServer server, String bucketName, String changeRecord) throws IOException {
        String prefix = "snapshot-release";
        String url = StringUtils.join(wiremockBaseUrl, "/download/prod-payload.tgz");

        createBucket(testS3Client, bucketName);

        String response = deployService.deployRelease(url, bucketName, prefix, server, changeRecord);

        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();

        List<S3Object> objects = testS3Client.listObjectsV2(listObjectsRequest).contents();
        ArrayList<String> keys = new ArrayList<>(objects.stream().map(S3Object::key).toList());

        List<String> expectedKeys = Arrays.asList("snapshot-release/FAEHu3AUcAsYpjH.jfif",
                "snapshot-release/NORTHWIND.sqlite.sql", "snapshot-release/preview.png",
                "snapshot-release/New folder (2)/notefield-parctice.bsdesign",
                "snapshot-release/monday-04-jul-2022-15-27-09-1656962867598 (1).png",
                "snapshot-release/New folder (2)/deep folder/Login Prototype.bsdesign");

        //sort the keys in alphabetical order
        Collections.sort(keys);
        Collections.sort(expectedKeys);

        assertEquals(expectedKeys, keys);
        assertEquals(6, objects.size());
        assertEquals("File processed and uploaded successfully", response);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "AWS_S3, aws-bucket, null, removal-test",
            "ECS_S3, ecs-bucket, null, removal-test",
            "AWS_S3_PROD, inc-prod-bucket, MCR18434340, removal-test",
            "AWS_S3_PROD, aws-prod-bucket, INC23434114, removal-test",
            "ECS_S3_PROD, mcr-prod-bucket, MCR18434340, removal-test",
            "ECS_S3_PROD, ecs-prod-bucket, INC23434114, removal-test",
    }, nullValues = {"null"})
    public void GivenMatchingPrefix_ShouldOnlyRemoveThoseObjects(TargetServer server, String bucketName, String changeRecord, String prefix) throws IOException {
        // Create test environment
        String controlPrefix = "control";
        uploadDirectory(testS3Client, bucketName, "src/test/resources/snapshot-test", prefix);
        uploadDirectoryToExistingBucket(testS3Client, bucketName, "src/test/resources/snapshot-test", controlPrefix);

        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        // Check there are 6 objects in test bucket
        assertEquals(6, testS3Client.listObjectsV2(listObjectsRequest).contents().size());

        String response = deployService.remove(bucketName, prefix, server, changeRecord);

        // Check the bucket only contains control objects
        assertEquals(3, testS3Client.listObjectsV2(listObjectsRequest).contents().size());

        List<S3Object> objects = testS3Client.listObjectsV2(listObjectsRequest).contents();
        boolean hasControlObjects = new ArrayList<>(objects.stream()
                .map(S3Object::key)
                .toList()).stream()
                .anyMatch((p) -> p.startsWith(prefix));

        assertFalse(hasControlObjects);

        String expected = StringUtils
                .join("All objects with prefix starting with 'removal-test' deleted from bucket '", bucketName, "'");

        assertEquals(expected, response);
    }

    @Test
    public void GivenMissingBucket_ShouldReturnErrorMessage() {
        String folderPath = "src/test/resources/snapshot-test";

        Exception exception = assertThrows(NoSuchBucketException.class, () -> {
            deployService.deploySnapshot("bucket", "snapshot-test", folderPath, ECS_S3, null);
        });

        String expectedMessage = "Bucket 'bucket' not exist.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "AWS_S3, aws-bucket, null, test17",
            "ECS_S3, ecs-bucket, null, test17",
            "AWS_S3_PROD, inc-prod-bucket, MCR18434340, test17",
            "AWS_S3_PROD, aws-prod-bucket, INC23434114, test17",
            "ECS_S3_PROD, mcr-prod-bucket, MCR18434340, test17",
            "ECS_S3_PROD, ecs-prod-bucket, INC23434114, test17",
    }, nullValues = {"null"})
    public void GivenPrefixWithNoMatches_ShouldRenderEmptyTable(TargetServer server, String bucket, String changeRecord, String prefix) {
        createBucket(testS3Client, bucket);

        String response = deployService.verifyObjectsExist(bucket, prefix, server, changeRecord)
                .replaceAll("\\r\\n?", "\n"); // normalise EOL characters

        String expected = """
                +-----+---------------+--------------+
                | Key | Date Modified | Size (Bytes) |
                +-----+---------------+--------------+
                +-----+---------------+--------------+""";

        assertEquals(expected, response);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "AWS_S3, aws-bucket1, null, test17",
            "ECS_S3, ecs-bucket1, null, test17",
            "AWS_S3_PROD, inc-prod-bucket1, MCR18434340, test17",
            "AWS_S3_PROD, aws-prod-bucket1, INC23434114, test17",
            "ECS_S3_PROD, mcr-prod-bucket1, MCR18434340, test17",
            "ECS_S3_PROD, ecs-prod-bucket1, INC23434114, test17",
    }, nullValues = {"null"})
    public void GivenPrefixWithMatches_ShouldRenderTable(TargetServer server, String bucket, String changeRecord, String prefix) {
        uploadDirectory(testS3Client, bucket, "src/test/resources/snapshot-test", prefix);

        String response = deployService.verifyObjectsExist(bucket, prefix, server, changeRecord)
                .replaceAll("\\r\\n?", "\n"); // normalise EOL characters

        System.out.println(response);

        // Regex pattern to match the header
        String headerRegex = "\\+[-]+\\+[-]+\\+[-]+\\+\n" + // Matches the top line
                "\\|\\s*Key\\s*\\|\\s*Date Modified\\s*\\|\\s*Size \\(Bytes\\)\\s*\\|\n" + // Matches the header row
                "\\+[-]+\\+[-]+\\+[-]+\\+"; // Matches the bottom line

        // Assert that the header exists
        Pattern pattern = Pattern.compile(headerRegex);
        Matcher matcher = pattern.matcher(response);
        assertTrue(matcher.find());

        // Expected values
        List<String> expectedKeys = Arrays.asList(
                "test17/src/test/resources/snapshot-test/outer - Copy.txt",
                "test17/src/test/resources/snapshot-test/outer.txt",
                "test17/src/test/resources/snapshot-test/snapshot/inner.txt"
        );

        expectedKeys.forEach((key) -> assertTrue(response.contains(key)));
    }

    @Test
    public void GivenEmptyFolderPath_ShouldReturnErrorMessage() throws IOException {
        String response = deployService.deploySnapshot("test-bucket", "p", "tomato", AWS_S3, null);

        assertEquals("Invalid folder path or folder is empty.", response);
    }
}
