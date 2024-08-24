package com.example.cli.s3.utils;

import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


public class Util {

    @Value("${default.object.content.type}")
    private static String defaultContentType;

    public static String getUKTimestamp(Instant instant) {
        // Get the ZoneId for the UK (Europe/London)
        ZoneId ukZoneId = ZoneId.of("Europe/London");

        // Convert the Instant to a ZonedDateTime in the UK time zone
        ZonedDateTime ukZonedDateTime = ZonedDateTime.ofInstant(instant, ukZoneId);

        // Format the UK Date Time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return ukZonedDateTime.format(formatter);
    }

    public static String getContentType(Path filePath) {
        try {
            String contentType = Files.probeContentType(filePath);
            return contentType != null ? contentType : defaultContentType;
        } catch (IOException e) {
            return defaultContentType;
        }
    }
}
