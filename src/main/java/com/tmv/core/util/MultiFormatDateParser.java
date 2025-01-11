package com.tmv.core.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

public class MultiFormatDateParser {

    private static final List<DateTimeFormatter> formatters = new ArrayList<>();

   static {
       formatters.add(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")); // ISO mit Offset
       formatters.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));         // Standard Datum-Zeit
       formatters.add(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));           // Tag-Monat-Jahr mit Zeit
       formatters.add(
               new DateTimeFormatterBuilder().appendPattern("dd-MM-yyyy")
                       .optionalStart()
                       .appendPattern(" HH:mm")
                       .optionalEnd()
                       .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                       .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                       .toFormatter());
       formatters.add(
               new DateTimeFormatterBuilder().appendPattern("dd.MM.yyyy")
                       .optionalStart()
                       .appendPattern(" HH:mm")
                       .optionalEnd()
                       .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                       .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                       .toFormatter());
    }

    public static LocalDateTime parseDate(String dateString) {
        for (DateTimeFormatter formatter : formatters) {
            try {
                // try formatter
                return LocalDateTime.parse(dateString, formatter);
            } catch (DateTimeParseException e) {
                // parse failed try next one
            }
        }
        throw new IllegalArgumentException("Unsupported date format: " + dateString);
    }
}