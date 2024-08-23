package com.example.cli.s3.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SnowBrokerValidationResponse {
    private String secretKey;
    private String accessKeyId;
    private Boolean isValid;
}
