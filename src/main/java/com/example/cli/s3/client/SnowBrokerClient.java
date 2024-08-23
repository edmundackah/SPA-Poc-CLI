package com.example.cli.s3.client;

import com.example.cli.s3.response.SnowBrokerValidationResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SnowBrokerClient {

    //TODO: Add logic for change record validation
    public SnowBrokerValidationResponse validateChangeRecord(String changeRecord) {
        if (changeRecord.endsWith("prd") && !StringUtils.hasText(changeRecord)) {
            throw new IllegalArgumentException("Change record is required for production bucket.");
        }

        return SnowBrokerValidationResponse.builder()
                .isValid(true)
                .accessKeyId("hardcodedkey")
                .secretKey("secretkeyhere")
                .build();
    }
}
