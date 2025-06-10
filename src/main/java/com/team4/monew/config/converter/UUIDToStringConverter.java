package com.team4.monew.config.converter;

import java.util.UUID;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UUIDToStringConverter implements Converter<UUID, String> {

  @Override
  public String convert(UUID source) {
    return source.toString();
  }
}