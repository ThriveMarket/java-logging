package com.thrivemarket.logging.config;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class LoggingUtilsTest {

  private LoggingUtils loggingUtils;

  @Mock private Logger mockLogger;

  @BeforeEach
  void setUp() {
    loggingUtils = new LoggingUtils();
    ReflectionTestUtils.setField(loggingUtils, "serviceName", "test-service");
    ReflectionTestUtils.setField(loggingUtils, "serviceVersion", "1.0.0");
  }

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  @Test
  void testLogEvent() {
    // Given
    Map<String, Object> context = new HashMap<>();
    context.put("user_id", "12345");
    context.put("action", "login");

    // When
    loggingUtils.logEvent(
        mockLogger, LoggingUtils.LogLevel.INFO, "user_event", "User logged in", context);

    // Then
    verify(mockLogger, times(1)).info("User logged in");
  }

  @Test
  void testLogError() {
    // Given
    Map<String, Object> context = new HashMap<>();
    context.put("operation", "data_sync");

    Exception testException = new RuntimeException("Test exception");

    // When
    loggingUtils.logError(
        mockLogger, "system_error", "Operation failed", testException, context);

    // Then
    verify(mockLogger, times(1)).error("Operation failed");
  }
}