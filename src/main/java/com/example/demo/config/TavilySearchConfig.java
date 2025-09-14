package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class TavilySearchConfig {

  @Value("${tavily.api.key}")
  private String apiKey;

  @Bean
  public RestClient tavilyRestClient() {
    return RestClient.builder()
        .baseUrl("https://api.tavily.com")
        .defaultHeader("Authorization", "Bearer " + apiKey)
        .defaultHeader("Content-Type", "application/json")
        .build();
  }
}
