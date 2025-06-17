package com.team4.monew.config;

import com.team4.monew.interceptor.AuthInterceptor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final AuthInterceptor authInterceptor;
  private final Jackson2ObjectMapperBuilder jacksonBuilder;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(authInterceptor)
        .addPathPatterns("/api/**")
        .excludePathPatterns("/api/users", "/api/users/login");
  }

  //  @Override
//  public void addFormatters(FormatterRegistry registry) {
//    registry.addConverter(String.class, Instant.class, source -> {
//      try {
//        return Instant.ofEpochSecond(Long.parseLong(source));
//      } catch (NumberFormatException e) {
//        // ISO-8601 형식 처리 시도
//        return Instant.parse(source);
//      }
//    });
//  }
  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.stream()
        .filter(MappingJackson2HttpMessageConverter.class::isInstance)
        .map(MappingJackson2HttpMessageConverter.class::cast)
        .forEach(converter -> converter.setObjectMapper(jacksonBuilder.build()));
  }


}
