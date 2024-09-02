package com.example.cli.s3.client;

import com.example.cli.s3.exception.SnowBrokerException;
import com.example.cli.s3.models.response.S3CredentialsResponse;
import com.example.cli.s3.models.response.SnowValidationResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnowBrokerClient {

    @Autowired
    private final ObjectMapper objectMapper;

    @Value("${snow.broker.incident.endpoint}")
    private String incidentEndpoint;

    @Value("${snow.broker.change.endpoint}")
    private String changeEndpoint;

    //TODO: Review logic for change record validation
    public S3CredentialsResponse validateChangeRecord(String changeRecord) throws SnowBrokerException {
        String url = StringUtils.join((changeRecord.startsWith("INC") ? incidentEndpoint : changeEndpoint), changeRecord);

        log.info("Calling url: {}", url);

        try {
            HttpResponse<String> response = Unirest.get(url).asString();
            log.debug("Response: {}", response.getBody());
            switch (response.getStatus()) {
                case 200:
                    SnowValidationResponse body = objectMapper.readValue(response.getBody(), SnowValidationResponse.class);
                    if (body.getIsValid()) {
                        return body.getKey();
                    } else {
                        throw new SnowBrokerException("Change Record not valid for deployment");
                    }
                case 404:
                    throw new SnowBrokerException("Change Record not valid for deployment");
                default:
                    throw new SnowBrokerException("Failed to reach ServiceNow, see logs for details");
            }
        } catch (JsonProcessingException e) {
            log.error("Something went wrong: ", e);
            throw new SnowBrokerException("Failed to reach ServiceNow, see logs for details");
        }
    }
}
