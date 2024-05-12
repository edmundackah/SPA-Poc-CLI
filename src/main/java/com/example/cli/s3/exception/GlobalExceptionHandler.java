package com.example.cli.s3.exception;

import com.example.cli.s3.constants.ExitCodes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.command.CommandExceptionResolver;
import org.springframework.shell.command.CommandHandlingResult;
import org.springframework.shell.command.annotation.ExitCode;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.net.ConnectException;


@Slf4j
@Component
public class GlobalExceptionHandler implements CommandExceptionResolver {

    private static final String ANSI_RED = "\\u001B[31m";
    private static final String ANSI_NO_COLOUR = "\\u001B[0m";

    @Override
    public CommandHandlingResult resolve(Exception ex) {


        if (ex instanceof SdkClientException) {
            return CommandHandlingResult.of("An error occurred with S3 Client: " + ex.getLocalizedMessage(), ExitCodes.FAILURE);
        } else if (ex instanceof S3Exception) {
            return CommandHandlingResult.of("S3 Access Error: " + ex.getLocalizedMessage(), ExitCodes.FAILURE);
        } else if (ex instanceof ConnectException) {
            return CommandHandlingResult.of("Network connection error occurred: " + ex.getLocalizedMessage(), ExitCodes.FAILURE);
        } else if (ex instanceof IllegalArgumentException) {
            return CommandHandlingResult.of(ex.getLocalizedMessage(), ExitCodes.FAILURE);
        }

        // ... more else-if blocks for other custom exceptions ...

        else { // Default case for unexpected exceptions
            log.error("Unexpected error", ex);
            return CommandHandlingResult.of("An unexpected error occurred: " + ex.getLocalizedMessage(), ExitCodes.FAILURE);
        }
    }
}


