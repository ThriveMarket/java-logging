package com.thrivemarket.logging.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Auto-configuration for standardized logging components.
 * This class provides beans for logging utilities and request interceptors that
 * can be conditionally enabled through application properties.
 */
@Configuration
public class LoggingAutoConfiguration {

  /**
   * Creates the logging utilities bean if it doesn't already exist.
   *
   * @return LoggingUtils instance
   */
  @Bean
  @ConditionalOnMissingBean
  public LoggingUtils loggingUtils() {
    return new LoggingUtils();
  }

  /**
   * Creates a request logging interceptor if enabled.
   *
   * @return RequestLoggingInterceptor instance
   */
  @Bean
  @ConditionalOnProperty(name = "thrivemarket.logging.request.enabled", havingValue = "true", matchIfMissing = true)
  public RequestLoggingInterceptor requestLoggingInterceptor() {
    return new RequestLoggingInterceptor();
  }

  /**
   * Creates a Commons request logging filter for detailed request/response content logging.
   * This is disabled by default but can be enabled via properties.
   *
   * @return CommonsRequestLoggingFilter instance
   */
  @Bean
  @ConditionalOnProperty(name = "thrivemarket.logging.request.detailed", havingValue = "true", matchIfMissing = false)
  public CommonsRequestLoggingFilter requestLoggingFilter() {
    CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
    loggingFilter.setIncludeClientInfo(true);
    loggingFilter.setIncludeQueryString(true);
    loggingFilter.setIncludePayload(true);
    loggingFilter.setMaxPayloadLength(10000);
    loggingFilter.setIncludeHeaders(true);
    loggingFilter.setBeforeMessagePrefix("REQUEST DATA: ");
    loggingFilter.setAfterMessagePrefix("RESPONSE DATA: ");
    return loggingFilter;
  }
}