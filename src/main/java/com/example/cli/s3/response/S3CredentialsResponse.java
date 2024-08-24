package com.example.cli.s3.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class S3CredentialsResponse {
    private String secretKey;
    private String accessKeyId;
}
