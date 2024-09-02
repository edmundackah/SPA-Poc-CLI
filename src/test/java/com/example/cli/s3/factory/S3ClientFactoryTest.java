package com.example.cli.s3.factory;

import com.example.cli.s3.client.SnowBrokerClient;
import com.example.cli.s3.enums.TargetServer;
import com.example.cli.s3.exception.SnowBrokerException;
import com.example.cli.s3.models.response.S3CredentialsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ClientFactoryTest {

    @Mock
    private SnowBrokerClient snowBrokerClient;

    @InjectMocks
    private S3ClientFactory s3ClientFactory;

    @BeforeEach
    void setUp() {
        // Set the private fields using ReflectionTestUtils
        ReflectionTestUtils.setField(s3ClientFactory, "ecsRegion", "us-west-2");
        ReflectionTestUtils.setField(s3ClientFactory, "endpointUrl", "https://ecs.example.com");
        ReflectionTestUtils.setField(s3ClientFactory, "accessKeyId", "access-key-id");
        ReflectionTestUtils.setField(s3ClientFactory, "secretAccessKey", "secret-access-key");
    }

    @Test
    void GivenAwsS3Server_WhenGetS3ClientCalled_ShouldReturnNonNullClient() {
        S3Client client = s3ClientFactory.getS3Client(TargetServer.AWS_S3, null);
        assertNotNull(client, "S3Client should not be null for AWS_S3");
    }

    @Test
    void GivenAwsS3ProdServer_WhenGetS3ClientCalled_ShouldReturnNonNullClientAndValidateChangeRecord() {
        String changeRecord = "some-change-record";
        S3Client client = s3ClientFactory.getS3Client(TargetServer.AWS_S3_PROD, changeRecord);
        assertNotNull(client, "S3Client should not be null for AWS_S3_PROD");
        verify(snowBrokerClient, times(1)).validateChangeRecord(changeRecord);
    }

    @Test
    void GivenEcsS3Server_WhenGetS3ClientCalled_ShouldReturnClientWithCorrectEndpointRegionAndCredentials() {
        S3Client client = s3ClientFactory.getS3Client(TargetServer.ECS_S3, null);
        assertNotNull(client, "S3Client should not be null for ECS_S3");
    }

    @Test
    void GivenEcsS3ProdServer_WhenGetS3ClientCalled_ShouldReturnClientWithProdCredentials() {
        String changeRecord = "some-change-record";
        S3CredentialsResponse response = new S3CredentialsResponse("prod-access-key-id", "prod-secret-key");
        when(snowBrokerClient.validateChangeRecord(changeRecord)).thenReturn(response);

        S3Client client = s3ClientFactory.getS3Client(TargetServer.ECS_S3_PROD, changeRecord);
        assertNotNull(client, "S3Client should not be null for ECS_S3_PROD");

        verify(snowBrokerClient, times(1)).validateChangeRecord(changeRecord);
        assertNotNull(client);
    }

    @Test
    void GivenInvalidChangeRecord_WhenGetS3ClientCalled_ShouldThrowSnowBrokerException() {
        String changeRecord = "invalid-change-record";
        when(snowBrokerClient.validateChangeRecord(changeRecord)).thenThrow(new SnowBrokerException("Change Record not valid for deployment"));

        SnowBrokerException exception = assertThrows(SnowBrokerException.class, () -> {
            s3ClientFactory.getS3Client(TargetServer.ECS_S3_PROD, changeRecord);
        });

        assertEquals("Change Record not valid for deployment", exception.getMessage());
        verify(snowBrokerClient, times(1)).validateChangeRecord(changeRecord);
    }

    @Test
    void GivenValidChangeRecord_WhenSnowBrokerResponseInvalid_ShouldThrowSnowBrokerException() {
        String changeRecord = "some-change-record";

        when(snowBrokerClient.validateChangeRecord(changeRecord))
                .thenThrow(new SnowBrokerException("Change Record not valid for deployment"));

        SnowBrokerException exception = assertThrows(SnowBrokerException.class, () -> {
            s3ClientFactory.getS3Client(TargetServer.ECS_S3_PROD, changeRecord);
        });

        assertEquals("Change Record not valid for deployment", exception.getMessage());
        verify(snowBrokerClient, times(1)).validateChangeRecord(changeRecord);
    }
}
