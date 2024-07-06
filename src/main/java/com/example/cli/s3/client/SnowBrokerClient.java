package com.example.cli.s3.client;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SnowBrokerClient {

    public void validateChangeRecord(String changeRecord) throws RuntimeException {
        // Simulated validation logic
        if (!changeRecord.matches("CR[0-9]+")) {  // Assuming change records follow a pattern like "CR12345"
            throw new RuntimeException("Invalid change record: " + changeRecord);
        }
    }

    public void validateChangeRecord(String bucketName, String changeRecord) {
        if ("prd".equalsIgnoreCase(bucketName) && !StringUtils.hasText(changeRecord)) {
            throw new IllegalArgumentException("Change record is required for production bucket.");
        }
    }
}
