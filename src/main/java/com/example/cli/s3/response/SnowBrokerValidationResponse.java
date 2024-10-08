package com.example.cli.s3.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class SnowBrokerValidationResponse {
    private S3CredentialsResponse key;
    private Boolean isValid;
}
