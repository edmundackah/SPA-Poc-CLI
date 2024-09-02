package com.example.cli.s3.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.shell.command.CommandHandlingResult;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;

import static com.example.cli.s3.exception.GlobalExceptionHandler.FAILURE_EXIT_CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void GivenIOException_WhenResolveCalled_ShouldReturnFailureWithMessage() {
        IOException exception = new IOException("IO error occurred");
        CommandHandlingResult result = globalExceptionHandler.resolve(exception);

        assertNotNull(result);
        assertEquals(FAILURE_EXIT_CODE, result.exitCode());
        assertEquals("IO error occurred", result.message());
    }

    @Test
    void GivenSdkClientException_WhenResolveCalled_ShouldReturnFailureWithS3ClientMessage() {
        SdkClientException exception = SdkClientException.builder().message("S3 client error").build();
        CommandHandlingResult result = globalExceptionHandler.resolve(exception);

        assertNotNull(result);
        assertEquals(FAILURE_EXIT_CODE, result.exitCode());
        assertEquals("An error occurred with S3 Client: S3 client error", result.message());
    }

    @Test
    void GivenS3Exception_WhenResolveCalled_ShouldReturnFailureWithS3AccessMessage() {
        S3Exception exception = (S3Exception) S3Exception.builder().message("S3 access error").build();
        CommandHandlingResult result = globalExceptionHandler.resolve(exception);

        assertNotNull(result);
        assertEquals(FAILURE_EXIT_CODE, result.exitCode());
        assertEquals("S3 Access Error: S3 access error", result.message());
    }

    @Test
    void GivenIllegalArgumentException_WhenResolveCalled_ShouldReturnFailureWithMessage() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");
        CommandHandlingResult result = globalExceptionHandler.resolve(exception);

        assertNotNull(result);
        assertEquals(FAILURE_EXIT_CODE, result.exitCode());
        assertEquals("Invalid argument", result.message());
    }

    @Test
    void GivenUnexpectedException_WhenResolveCalled_ShouldReturnFailureWithUnexpectedErrorMessage() {
        Exception exception = new Exception("Unexpected error");
        CommandHandlingResult result = globalExceptionHandler.resolve(exception);

        assertNotNull(result);
        assertEquals(FAILURE_EXIT_CODE, result.exitCode());
        assertEquals("An unexpected error occurred: Unexpected error", result.message());
    }

}
