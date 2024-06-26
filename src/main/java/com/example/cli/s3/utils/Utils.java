package com.example.cli.s3.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {

    public static String getUKTimestamp(Instant instant) {
        // Get the ZoneId for the UK (Europe/London)
        ZoneId ukZoneId = ZoneId.of("Europe/London");

        // Convert the Instant to a ZonedDateTime in the UK time zone
        ZonedDateTime ukZonedDateTime = ZonedDateTime.ofInstant(instant, ukZoneId);

        // Format the UK Date Time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return ukZonedDateTime.format(formatter);
    }
}
