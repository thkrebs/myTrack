package com.tmv.core.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MultiFormatDateParserTest {

    @Test
    void testParseDate_validFormat1() {
        String dateString = "2023-10-30T23:00:00.000+00:00";
        LocalDateTime result = MultiFormatDateParser.parseDate(dateString);
        assertEquals(LocalDateTime.of(2023, 10, 30, 23, 0), result);
    }

    @Test
    void testParseDate_validFormat2() {
        String dateString = "2023-10-30 23:00:00";
        LocalDateTime result = MultiFormatDateParser.parseDate(dateString);
        assertEquals(LocalDateTime.of(2023, 10, 30, 23, 0), result);
    }

    @Test
    void testParseDate_validFormat3() {
        String dateString = "30-10-2023 23:00";
        LocalDateTime result = MultiFormatDateParser.parseDate(dateString);
        assertEquals(LocalDateTime.of(2023, 10, 30, 23, 0), result);
    }

    @Test
    void testParseDate_validFormat4() {
        String dateString = "30-10-2023";
        LocalDateTime result = MultiFormatDateParser.parseDate(dateString);
        assertEquals(LocalDate.of(2023, 10, 30), result.toLocalDate());
    }

    @Test
    void testParseDate_validFormat5() {
        String dateString = "30.10.2023";
        LocalDateTime result = MultiFormatDateParser.parseDate(dateString);
        assertEquals(LocalDate.of(2023, 10, 30), result.toLocalDate());
    }

    @Test
    void testParseDate_invalidFormat() {
        String invalidDateString = "30/10/2023"; // Nicht unterstütztes Format
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            MultiFormatDateParser.parseDate(invalidDateString);
        });
        assertEquals("Unsupported date format: " + invalidDateString, exception.getMessage());
    }


}