package com.team4.monew.config.converter;

import java.util.UUID;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToUUIDConverter implements Converter<String, UUID> {

  @Override
  public UUID convert(String source) {
    return UUID.fromString(source);
  }
}
