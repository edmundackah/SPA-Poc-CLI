package com.example.cli.s3.exception;

public class SnowBrokerException extends RuntimeException {
    public SnowBrokerException(String message) {
        super(message);
    }
}