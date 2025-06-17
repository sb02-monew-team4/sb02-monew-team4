package com.team4.monew.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

public class DateTimeUtils {

  public static LocalDateTime parseToLocalDateTime(String input) {
    if (input == null || input.isBlank()) {
      return null;
    }

    try {
      if (input.endsWith("Z")) {
        Instant instant = Instant.parse(input);
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
      } else {
        return LocalDateTime.parse(input);
      }
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid datetime format: " + input, e);
    }
  }

}
