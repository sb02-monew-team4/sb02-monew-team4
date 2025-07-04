package com.team4.monew.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import java.time.Instant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfig {

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    // private 필드에 접근 가능하도록 설정
    mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    // JavaTime 모듈 등록
    mapper.registerModule(new JavaTimeModule());
    // 빈 객체 직렬화 허용
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    // java.time 객체(Instant, LocalDateTime 등)를 Unix timestamp 형식 대신 ISO 8601 문자열 형식으로 직렬화하도록 설정
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return mapper;
  }

  @Bean
  public Jackson2ObjectMapperBuilder jacksonBuilder() {
    Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
    builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    builder.serializerByType(Instant.class, InstantSerializer.INSTANCE);
    builder.modules(new JavaTimeModule());
    return builder;
  }

}
