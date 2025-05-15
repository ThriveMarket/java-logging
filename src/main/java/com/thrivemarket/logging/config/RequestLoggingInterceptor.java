package com.thrivemarket.logging.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interceptor for logging HTTP requests with structured data.
 * Logs method, path, status code, and timing information.
 */
public class RequestLoggingInterceptor implements HandlerInterceptor {

  private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    request.setAttribute("startTime", System.currentTimeMillis());

    // Add client IP to MDC
    String clientIp = getClientIp(request);
    MDC.put("clientIp", clientIp);

    // Add request ID to MDC
    String requestId = getRequestId(request);
    MDC.put("requestId", requestId);

    // Add path and method to MDC for structured logging
    MDC.put("path", request.getRequestURI());
    MDC.put("method", request.getMethod());

    // Add session ID if available
    String sessionId = "none";
    if (request.getSession(false) != null) {
      sessionId = request.getSession().getId();
    }
    MDC.put("sessionId", sessionId);

    return true;
  }

  @Override
  public void postHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      ModelAndView modelAndView) {
    // Nothing to do here
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    try {
      long startTime = (long) request.getAttribute("startTime");
      long duration = System.currentTimeMillis() - startTime;

      MDC.put("durationMs", String.valueOf(duration));
      MDC.put("status", String.valueOf(response.getStatus()));

      // Log the request details
      log.info(
          "HTTP {} {} - {} ({}ms)",
          request.getMethod(),
          request.getRequestURI(),
          response.getStatus(),
          duration);
    } finally {
      // Clear MDC context after logging
      MDC.clear();
    }
  }

  protected String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("X-Real-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    return ip;
  }

  protected String getRequestId(HttpServletRequest request) {
    // Check common request ID headers
    String requestId = request.getHeader("X-Request-ID");
    if (requestId == null || requestId.isEmpty()) {
      requestId = request.getHeader("X-Correlation-ID");
    }
    if (requestId == null || requestId.isEmpty()) {
      requestId = request.getHeader("Request-ID");
    }
    return requestId != null ? requestId : "unknown";
  }
}