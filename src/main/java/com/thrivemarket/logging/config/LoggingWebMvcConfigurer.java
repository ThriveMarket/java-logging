package com.thrivemarket.logging.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Web MVC configuration to register the request logging interceptor.
 * This configuration is conditionally enabled based on application properties.
 */
@Configuration
@ConditionalOnProperty(name = "thrivemarket.logging.request.enabled", havingValue = "true", matchIfMissing = true)
public class LoggingWebMvcConfigurer implements WebMvcConfigurer {

  private final RequestLoggingInterceptor requestLoggingInterceptor;

  public LoggingWebMvcConfigurer(RequestLoggingInterceptor requestLoggingInterceptor) {
    this.requestLoggingInterceptor = requestLoggingInterceptor;
  }

  @Override
  public void addInterceptors(@NonNull InterceptorRegistry registry) {
    registry.addInterceptor(requestLoggingInterceptor);
  }
}