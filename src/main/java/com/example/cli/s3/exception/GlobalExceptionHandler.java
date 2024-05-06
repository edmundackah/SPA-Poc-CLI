package com.example.cli.s3.exception;

import com.example.cli.s3.constants.ExitCodes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.shell.command.CommandHandlingResult;
import org.springframework.shell.command.annotation.ExceptionResolver;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkClientException;

import java.net.ConnectException;

@Slf4j
@Component
public class GlobalExceptionHandler {

    @ExceptionResolver(SdkClientException.class)
    public CommandHandlingResult handleSdkClientException(SdkClientException ex) {
        log.error("Exception caught in GlobalExceptionHandler!", ex); // Extensive logging
        return CommandHandlingResult.of( "An error occurred with S3 Client. See stacktrace for details.", ExitCodes.FAILURE);
    }

    @ExceptionResolver(ConnectException.class)
    public CommandHandlingResult handleConnectException(ConnectException ex) {
        String message = StringUtils.join(ex.getLocalizedMessage(), "See stacktrace for details."); // Extensive logging
        return CommandHandlingResult.of(message, ExitCodes.FAILURE);
    }
}

