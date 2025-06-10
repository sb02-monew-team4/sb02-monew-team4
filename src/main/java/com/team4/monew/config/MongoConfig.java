package com.team4.monew.config;

import com.team4.monew.config.converter.StringToUUIDConverter;
import com.team4.monew.config.converter.UUIDToStringConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions.MongoConverterConfigurationAdapter;

@Configuration
@EnableMongoAuditing
public class MongoConfig extends AbstractMongoClientConfiguration {

  @Override
  protected String getDatabaseName() {
    return "your_database_name";
  }

  @Override
  protected void configureConverters(MongoConverterConfigurationAdapter adapter) {
    adapter.registerConverter(new UUIDToStringConverter());
    adapter.registerConverter(new StringToUUIDConverter());
  }
}

