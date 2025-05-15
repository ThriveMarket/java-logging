package com.thrivemarket.logging.example;

import com.thrivemarket.logging.config.LoggingUtils;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example class demonstrating how to use the logging utilities.
 * This is just for documentation purposes and not part of the main library.
 */
public class LoggingExample {

  private static final Logger logger = LoggerFactory.getLogger(LoggingExample.class);
  private final LoggingUtils loggingUtils;

  public LoggingExample(LoggingUtils loggingUtils) {
    this.loggingUtils = loggingUtils;
  }

  public void demonstrateLogging() {
    // Simple logging with context
    Map<String, Object> context = new HashMap<>();
    context.put("user_id", "12345");
    context.put("account_type", "premium");
    
    loggingUtils.logEvent(
        logger,
        LoggingUtils.LogLevel.INFO,
        "user_login",
        "User logged in successfully",
        context);
    
    // Error logging with exception
    try {
      throw new RuntimeException("Something went wrong");
    } catch (Exception e) {
      Map<String, Object> errorContext = new HashMap<>();
      errorContext.put("operation", "data_processing");
      
      loggingUtils.logError(
          logger,
          "process_failure",
          "Failed to process data",
          e,
          errorContext);
    }
  }
}