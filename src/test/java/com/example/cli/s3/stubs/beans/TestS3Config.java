package com.example.cli.s3.stubs.beans;

import com.example.cli.s3.client.SnowBrokerClient;
import com.example.cli.s3.enums.TargetServer;
import com.example.cli.s3.factory.S3ClientFactory;
import com.example.cli.s3.response.S3CredentialsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.command.annotation.CommandScan;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Slf4j
@TestConfiguration
public class TestS3Config {

    @Value("${ecs.s3.region}")
    private String region;

    @Value("${ecs.s3.endpoint.url}")
    private String endpointUrl;

    @Value("${ecs.s3.dev.access.key.id}")
    private String accessKey;

    @Value("${ecs.s3.dev.secret.access.key}")
    private String secretKey;

    @Autowired
    private SnowBrokerClient snowBrokerClient;

    @Bean
    public S3ClientFactory s3ClientFactory() {
        return new S3ClientFactory() {
            @Override
            public S3Client getS3Client(TargetServer server, String changeRecord) {
                log.info("Connecting to test server: {}", server);

                switch (server) {
                    case AWS_S3, ECS_S3:
                        log.info("Using ES endpoint: {}", endpointUrl);

                        return S3Client.builder()
                            .region(Region.of(region))
                            .credentialsProvider(StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(accessKey, secretKey)))
                            .endpointOverride(URI.create(endpointUrl))
                            .build();

                    case AWS_S3_PROD, ECS_S3_PROD:
                        S3CredentialsResponse res = snowBrokerClient.validateChangeRecord(changeRecord);

                        log.debug("Test S3 credentials from snow broker stub {}", res);

                        return S3Client.builder()
                                .region(Region.of(region))
                                .credentialsProvider(StaticCredentialsProvider.create(
                                        AwsBasicCredentials.create(accessKey, secretKey)))
                                .endpointOverride(URI.create(endpointUrl))
                                .build();
                    default:
                        throw new IllegalArgumentException(StringUtils.join(server, " is not a valid target server"));
                }
            }
        };
    }
}