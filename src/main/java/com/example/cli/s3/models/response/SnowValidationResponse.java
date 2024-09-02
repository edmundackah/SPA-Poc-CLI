package com.example.cli.s3.models.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class SnowValidationResponse {
    private S3CredentialsResponse key;
    private Boolean isValid;
}
