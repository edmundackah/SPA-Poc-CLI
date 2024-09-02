package com.example.cli.s3.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.command.CommandExceptionResolver;
import org.springframework.shell.command.CommandHandlingResult;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;


@Slf4j
@Component
public class GlobalExceptionHandler implements CommandExceptionResolver {

    public static final Integer FAILURE_EXIT_CODE = 1;

    @Override
    public CommandHandlingResult resolve(Exception ex) {

        if (ex instanceof IOException) {
            return CommandHandlingResult.of(ex.getLocalizedMessage(), FAILURE_EXIT_CODE);
        } else if (ex instanceof SdkClientException) {
            return CommandHandlingResult.of("An error occurred with S3 Client: " + ex.getLocalizedMessage(), FAILURE_EXIT_CODE);
        } else if (ex instanceof S3Exception) {
            return CommandHandlingResult.of("S3 Access Error: " + ex.getLocalizedMessage(), FAILURE_EXIT_CODE);
        } else if (ex instanceof IllegalArgumentException) {
            return CommandHandlingResult.of(ex.getLocalizedMessage(), FAILURE_EXIT_CODE);
        }

        // ... more else-if blocks for other custom exceptions ...

        else { // Default case for unexpected exceptions
            log.error("Unexpected error", ex);
            return CommandHandlingResult.of("An unexpected error occurred: " + ex.getLocalizedMessage(), FAILURE_EXIT_CODE);
        }
    }
}


