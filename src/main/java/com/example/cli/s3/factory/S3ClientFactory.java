package com.example.cli.s3.factory;

import com.example.cli.s3.client.SnowBrokerClient;
import com.example.cli.s3.constants.HelpMessages;
import com.example.cli.s3.enums.TargetServer;
import com.example.cli.s3.response.SnowBrokerValidationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3ClientFactory {

    @Value("${ecs.s3.region}")
    private String awsRegion;

    @Value("${ecs.s3.endpoint.url}")
    private String endpointUrl;

    @Value("${ecs.s3.dev.access.key.id}")
    private String accessKeyId;

    @Value("${ecs.s3.dev.secret.access.key}")
    private String secretAccessKey;

    @Autowired
    private SnowBrokerClient snowBrokerClient;

    public S3Client getS3Client(TargetServer server, String changeRecord) {
        log.info("Connecting to server: {}", server);

        switch (server) {
            case AWS_S3:
                return S3Client.builder().build();
            case AWS_S3_PROD:
                SnowBrokerValidationResponse response = snowBrokerClient.validateChangeRecord(changeRecord);
                if (response.getIsValid()) {
                    return S3Client.builder().build();
                }
                throw new RuntimeException(HelpMessages.PROD_BUCKET_INVALID_CHANGE_RECORD);
            case ECS_S3:
                StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider
                        .create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));

                return S3Client.builder()
                        .endpointOverride(URI.create(endpointUrl))
                        .region(Region.of(awsRegion))
                        .credentialsProvider(credentialsProvider)
                        .build();
            case ECS_S3_PROD:
                SnowBrokerValidationResponse res = snowBrokerClient.validateChangeRecord(changeRecord);
                StaticCredentialsProvider prodCredentials = StaticCredentialsProvider
                        .create(AwsBasicCredentials.create(res.getAccessKeyId(), res.getSecretKey()));

                return S3Client.builder()
                        .endpointOverride(URI.create(endpointUrl))
                        .region(Region.of(awsRegion))
                        .credentialsProvider(prodCredentials)
                        .build();
            default:
                throw new IllegalArgumentException(StringUtils.join(server, " is not a valid target server"));
        }
    }
}
