package com.example.cli.s3.client;

import com.example.cli.s3.exception.SnowBrokerException;
import com.example.cli.s3.models.response.S3CredentialsResponse;
import com.example.cli.s3.stubs.ArtifactoryStubs;
import com.example.cli.s3.stubs.SnowBrokerStubs;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.shell.test.autoconfigure.ShellTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@ShellTest
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
@ComponentScan(basePackages = "com.example.cli.s3")
public class SnowBrokerClientTest {

    @Autowired
    private SnowBrokerClient snowBrokerClient;

    @BeforeEach
    public void setup() throws IOException {
        SnowBrokerStubs.validChangeRecordStub();
        ArtifactoryStubs.tarFile();
    }

    @ParameterizedTest
    @CsvSource({
            "INC23434114",
            "MCR18434340",
    })
    public void GivenValidChangeRecord_ShouldReturnCredentials(String changeRecord) {
        S3CredentialsResponse response = snowBrokerClient.validateChangeRecord(changeRecord);

        assertEquals("PV59ypmisU", response.getSecretKey());
        assertEquals("CnIjsDcI" , response.getAccessKeyId());
    }

    @ParameterizedTest
    @CsvSource({
            "INC000111",
            "MCR000111",
    })
    public void GivenInvalidChangeRecord_ShouldReturnErrorMessage(String changeRecord) {
        Exception exception = assertThrows(SnowBrokerException.class, () -> {
            snowBrokerClient.validateChangeRecord(changeRecord);
        });

        assertEquals("Change Record not valid for deployment", exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "INC717000",
            "MCR717000",
    })
    public void GivenSnowIsDown_ShouldReturnErrorMessage(String changeRecord) {
        Exception exception = assertThrows(SnowBrokerException.class, () -> {
            snowBrokerClient.validateChangeRecord(changeRecord);
        });

        assertEquals("Failed to reach ServiceNow, see logs for details", exception.getMessage());
    }
}
