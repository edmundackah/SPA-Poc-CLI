package com.example.cli.s3.service;

import com.example.cli.s3.constants.Constants;
import com.example.cli.s3.enums.FlagState;
import com.example.cli.s3.enums.TargetServer;
import com.example.cli.s3.util.BaseS3IntegrationTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MaintenanceServiceTest extends BaseS3IntegrationTest {

    @Autowired
    private MaintenanceService maintenanceService;

    @ParameterizedTest
    @CsvSource(value = {
            "aws-bucket, null, AWS_S3",
            "ecs-bucket, null, ECS_S3",
            "ecs-prod-bucket, MCR18434340, ECS_S3_PROD",
            "inc-prod-bucket, INC23434114, AWS_S3_PROD",
    }, nullValues = {"null"})
    public void GivenNoMaintenanceFile_ShouldRenderEmptyTable(String bucketName, String changeRecord, TargetServer server) {
        //Create bucket
        createBucket(testS3Client, bucketName);

        //Get maintenance flag
        String response = maintenanceService.displayFlags(bucketName, changeRecord, server)
                .replaceAll("\\r\\n?", "\n"); // normalise EOL characters;

        String expected = """
                +------+-------+
                | Flag | State |
                +------+-------+
                +------+-------+""";

        assertEquals(expected, response);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "aws-bucket, null, AWS_S3",
            "ecs-bucket, null, ECS_S3",
            "ecs-prod-bucket, MCR18434340, ECS_S3_PROD",
            "inc-prod-bucket, INC23434114, AWS_S3_PROD",
    }, nullValues = {"null"})
    public void GivenThereIsAMaintenanceFile_ShouldRenderTable(String bucketName, String changeRecord, TargetServer server) {
        String filePath = "src/test/resources/maintenance/maintenance.json";

        //Create bucket & Upload maintenance file
        createBucket(testS3Client, bucketName);
        uploadFileToS3(testS3Client, bucketName, Constants.MAINTENANCE_FILE,filePath);

        //Get maintenance flag
        String response = maintenanceService.displayFlags(bucketName, changeRecord, server)
                .replaceAll("\\r\\n?", "\n"); // normalise EOL characters;

        String expected = """
                +------------+-------+
                | Flag       | State |
                +------------+-------+
                | dummy:flag |    on |
                +------------+-------+
                |      test1 |    on |
                +------------+-------+""";

        assertEquals(expected, response);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "aws-bucket, 'test1,flag2', OFF, null, AWS_S3",
            "ecs-bucket, 'test1,flag2', OFF, null, ECS_S3",
            "ecs-prod-bucket, 'test1,flag2', OFF, MCR18434340, ECS_S3_PROD",
            "inc-prod-bucket, 'test1,flag2', OFF, INC23434114, AWS_S3_PROD",
    }, nullValues = {"null"})
    public void GivenFlagUpdates_ShouldUpdateTheFlags(String bucketName, String flag, FlagState flagState,
                                                 String changeRecord, TargetServer server) {
        String filePath = "src/test/resources/maintenance/maintenance.json";

        //Create bucket & Upload maintenance file
        createBucket(testS3Client, bucketName);
        uploadFileToS3(testS3Client, bucketName, Constants.MAINTENANCE_FILE, filePath);

        //Deploy maintenance flags
        String response = maintenanceService.updateFlags(bucketName, flag, flagState, changeRecord, server);

        //Check maintenance file contents
        byte[] object = downloadObject(testS3Client, bucketName, Constants.MAINTENANCE_FILE);
        String json = new String(Objects.requireNonNull(object), StandardCharsets.UTF_8);
        assertEquals("{\"dummy:flag\":\"on\",\"test1\":\"off\",\"flag2\":\"off\"}", json);

        String expected = StringUtils.join("Successfully created the maintenance file in bucket: ", bucketName);
        assertEquals(expected, response);
    }



    @ParameterizedTest
    @CsvSource(value = {
            "aws-bucket, 'flag1,flag2,flag3', ON, null, AWS_S3",
            "ecs-bucket, 'flag1,flag2,flag3', ON, null, ECS_S3",
            "ecs-prod-bucket, 'flag1,flag2,flag3', ON, MCR18434340, ECS_S3_PROD",
            "inc-prod-bucket, 'flag1,flag2,flag3', ON, INC23434114, AWS_S3_PROD",
    }, nullValues = {"null"})
    public void GivenMaintenanceFileDoesNotExist_ShouldCreateOne(String bucketName, String flag, FlagState flagState,
                                                                 String changeRecord, TargetServer server) {
        //Create bucket
        createBucket(testS3Client, bucketName);

        //Check maintenance file is not present
        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        assertEquals(0, testS3Client.listObjectsV2(listObjectsRequest).contents().size());

        //Deploy maintenance flag
        String response = maintenanceService.updateFlags(bucketName, flag, flagState, changeRecord, server);

        //Check maintenance json file is created
        assertEquals(1, testS3Client.listObjectsV2(listObjectsRequest).contents().size());

        //Check maintenance file contents
        byte[] object = downloadObject(testS3Client, bucketName, Constants.MAINTENANCE_FILE);
        String json = new String(Objects.requireNonNull(object), StandardCharsets.UTF_8);
        assertEquals("{\"flag1\":\"on\",\"flag2\":\"on\",\"flag3\":\"on\"}", json);

        String expected = StringUtils.join("Successfully created the maintenance file in bucket: ", bucketName);
        assertEquals(expected, response);
    }
}
