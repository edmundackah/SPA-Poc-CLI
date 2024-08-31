package com.example.cli.s3.client;

import com.example.cli.s3.exception.SnowBrokerException;
import com.example.cli.s3.response.S3CredentialsResponse;
import com.example.cli.s3.response.SnowBrokerValidationResponse;
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
        HttpResponse<String> res;

        try {
            res = Unirest.get(url).asString();
            log.debug("Response: {}", res.getBody());

            if (res.getStatus() == 200) {
                try {
                    SnowBrokerValidationResponse body = objectMapper.readValue(res.getBody(), SnowBrokerValidationResponse.class);
                    if (body.getIsValid()) {
                        return body.getKey();
                    }
                    throw new SnowBrokerException("Change Record not valid for deployment");
                } catch (Exception e) {
                    throw new SnowBrokerException("Error parsing res: " + e.getMessage());
                }
            } else if (res.getStatus() == 404) {
                throw new SnowBrokerException("Change Record not valid for deployment");
            } else {
                throw new SnowBrokerException("Failed to reach ServiceNow, see logs for details");
            }
        } catch (Exception e) {
            throw new SnowBrokerException("Failed to reach ServiceNow, see logs for details");
        }
    }
}
