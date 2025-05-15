package com.thrivemarket.logging.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Utility class for structured logging according to company standards. */
@Component
public class LoggingUtils {

  @Value("${spring.application.name:unknown}")
  private String serviceName;

  @Value("${spring.application.version:0.0.1}")
  private String serviceVersion;

  private static final String LOG_VERSION = "1";
  private static final String UNKNOWN = "unknown";
  private static String hostname;

  static {
    try {
      hostname = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      hostname = UNKNOWN;
    }
  }

  /**
   * Log an event with structured context data.
   *
   * @param logger The SLF4J logger
   * @param level Log level (DEBUG, INFO, WARN, ERROR)
   * @param event Event type
   * @param message Log message
   * @param context Additional context data
   */
  public void logEvent(
      Logger logger, LogLevel level, String event, String message, Map<String, Object> context) {

    // Add standard metadata
    MDC.put("version", LOG_VERSION);
    MDC.put("event", event);

    // Add service information
    MDC.put("service.name", serviceName);
    MDC.put("service.version", serviceVersion);
    MDC.put("service.hostName", hostname);

    try {
      // Include context data if provided
      if (context != null) {
        for (Map.Entry<String, Object> entry : context.entrySet()) {
          if (entry.getValue() != null) {
            MDC.put("context." + entry.getKey(), entry.getValue().toString());
          }
        }
      }

      // Log with appropriate level
      switch (level) {
        case DEBUG:
          logger.debug(message);
          break;
        case INFO:
          logger.info(message);
          break;
        case WARN:
          logger.warn(message);
          break;
        case ERROR:
          logger.error(message);
          break;
      }
    } finally {
      // Clear MDC for context entries only, leaving trace IDs intact
      if (context != null) {
        for (String key : context.keySet()) {
          MDC.remove("context." + key);
        }
      }
      MDC.remove("version");
      MDC.remove("event");
      MDC.remove("service.name");
      MDC.remove("service.version");
      MDC.remove("service.hostName");
    }
  }

  /**
   * Log an error event with exception details.
   *
   * @param logger The SLF4J logger
   * @param event Event type
   * @param message Log message
   * @param exception The exception
   * @param context Additional context data
   */
  public void logError(
      Logger logger,
      String event,
      String message,
      Throwable exception,
      Map<String, Object> context) {

    Map<String, Object> errorData = new HashMap<>();
    if (context != null) {
      errorData.putAll(context);
    }

    // Add exception details
    if (exception != null) {
      errorData.put("error.message", exception.getMessage());
      errorData.put("error.type", exception.getClass().getName());

      // Add stack trace as a single string
      StringBuilder stackTrace = new StringBuilder();
      for (StackTraceElement element : exception.getStackTrace()) {
        stackTrace.append(element.toString()).append("\n");
      }
      errorData.put("error.stack_trace", stackTrace.toString());
    }

    // Log using the standard method
    logEvent(logger, LogLevel.ERROR, event, message, errorData);
  }

  /** Helper enum for log levels. */
  public enum LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR
  }
}