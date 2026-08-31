package com.example.onboarding.config;

import com.example.onboarding.domain.account.Iban;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;

@Configuration
public class JdbcConfiguration {

  @Bean
  JdbcCustomConversions jdbcCustomConversions() {
    return new JdbcCustomConversions(
        List.of(IbanToStringConverter.INSTANCE, StringToIbanConverter.INSTANCE));
  }

  @WritingConverter
  private enum IbanToStringConverter implements Converter<Iban, String> {
    INSTANCE;

    @Override
    public String convert(Iban source) {
      return source.value();
    }
  }

  @ReadingConverter
  private enum StringToIbanConverter implements Converter<String, Iban> {
    INSTANCE;

    @Override
    public Iban convert(String source) {
      return new Iban(source);
    }
  }
}
