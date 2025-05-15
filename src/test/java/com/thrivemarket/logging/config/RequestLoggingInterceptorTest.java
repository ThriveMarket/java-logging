package com.thrivemarket.logging.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class RequestLoggingInterceptorTest {

  private RequestLoggingInterceptor interceptor;

  @Mock private HttpServletRequest mockRequest;
  @Mock private HttpServletResponse mockResponse;
  
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    interceptor = new RequestLoggingInterceptor();
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    
    // Setup default values
    request.setMethod("GET");
    request.setRequestURI("/api/test");
    response.setStatus(200);
  }

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  @Test
  void testPreHandleShouldSetMDCValues() {
    // When
    interceptor.preHandle(request, response, null);

    // Then
    assertNotNull(MDC.get("clientIp"));
    assertNotNull(MDC.get("requestId"));
    assertEquals("GET", MDC.get("method"));
    assertEquals("/api/test", MDC.get("path"));
    assertEquals("none", MDC.get("sessionId"));
  }

  @Test
  void testPreHandleShouldUseRequestIdFromHeader() {
    // Given
    request.addHeader("X-Request-ID", "test-request-id");

    // When
    interceptor.preHandle(request, response, null);

    // Then
    assertEquals("test-request-id", MDC.get("requestId"));
  }

  @Test
  void testAfterCompletionShouldLogRequestDetails() {
    // Given
    request.setAttribute("startTime", System.currentTimeMillis() - 100);
    interceptor.preHandle(request, response, null);

    // When
    interceptor.afterCompletion(request, response, null, null);

    // Then
    // MDC should be cleared after completion
    assertTrue(MDC.getCopyOfContextMap() == null || MDC.getCopyOfContextMap().isEmpty());
  }

  @Test
  void testGetClientIpShouldCheckMultipleHeaders() {
    // Given
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader("X-Forwarded-For", "1.2.3.4");
    req.setRemoteAddr("5.6.7.8");

    // Then
    assertEquals("1.2.3.4", interceptor.getClientIp(req));
  }
}