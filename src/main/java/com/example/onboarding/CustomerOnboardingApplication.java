package com.example.onboarding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CustomerOnboardingApplication {

  public static void main(String[] args) {
    SpringApplication.run(CustomerOnboardingApplication.class, args);
  }
}
