# Thrive Market Java Logging Library

A standardized logging configuration and utility library for Java applications that provides consistent, JSON-formatted logs suitable for modern observability platforms. This library wraps and configures SLF4J/Logback to provide structured logging capabilities.

## Important Clarification

**This library is not a replacement for SLF4J's Logger class.** Instead, it provides:

1. Pre-configured JSON logging via Logback
2. Spring Boot auto-configuration for logging
3. Request logging interceptors for web applications
4. Utility classes for enriched structured logging

You should continue to use `org.slf4j.Logger` and `LoggerFactory` as your logging interface, while this library provides the configuration layer and additional utilities.

## Features

- Structured JSON logging with standardized fields
- Request/response logging for web applications
- Customizable context data for richer log entries
- Exception logging with stack traces
- Service/version metadata automatically included
- Spring Boot auto-configuration for seamless integration
- Compatible with OpenTelemetry for distributed tracing

## Installation

### Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.thrivemarket</groupId>
    <artifactId>java-logging</artifactId>
    <version>1.0.0</version>
</dependency>
```

### AWS CodeArtifact Configuration

To use this library from AWS CodeArtifact, add the following to your `~/.m2/settings.xml`:

```xml
<settings>
  <servers>
    <server>
      <id>codeartifact</id>
      <username>aws</username>
      <password>${env.CODEARTIFACT_AUTH_TOKEN}</password>
    </server>
  </servers>
</settings>
```

And add the repository to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>codeartifact</id>
        <url>https://thrivemarket-904233098208.d.codeartifact.us-east-1.amazonaws.com/maven/libraries/</url>
    </repository>
</repositories>
```

Before building or deploying, export the CodeArtifact authorization token:

```bash
export CODEARTIFACT_AUTH_TOKEN=`aws codeartifact get-authorization-token --domain thrivemarket --domain-owner 904233098208 --region us-east-1 --query authorizationToken --output text`
```

Note: The token expires in 12 hours.

## Usage

### Basic Setup

The library uses Spring Boot auto-configuration to set up logging automatically. Simply add the dependency to your project and it will be enabled by default.

### Configuration Properties

Add the following to your `application.yml` to customize behavior:

```yaml
thrivemarket:
  logging:
    request:
      enabled: true      # Enable/disable request logging interceptor
      detailed: false    # Enable/disable detailed request content logging
```

### Logback Configuration

Create a `logback-spring.xml` file in your resources directory and include the pre-configured JSON logging template:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="logback-json.xml"/>
    
    <!-- Your additional appenders or configuration here -->
</configuration>
```

### Using LoggingUtils

The example below demonstrates how to use the SLF4J Logger interface alongside the LoggingUtils class from this library:

```java
import com.thrivemarket.logging.config.LoggingUtils;
import org.slf4j.Logger;                     // Standard SLF4J import - still required
import org.slf4j.LoggerFactory;              // Standard SLF4J import - still required 
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    // Standard SLF4J Logger usage - no changes needed here
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    // This library's utility class
    private final LoggingUtils loggingUtils;
    
    @Autowired
    public UserService(LoggingUtils loggingUtils) {
        this.loggingUtils = loggingUtils;
    }
    
    public void registerUser(User user) {
        Map<String, Object> context = new HashMap<>();
        context.put("user_id", user.getId());
        context.put("email", user.getEmail());
        
        try {
            // Business logic
            
            // Using this library's utility for structured logging
            loggingUtils.logEvent(
                log,                         // Pass your SLF4J logger here
                LoggingUtils.LogLevel.INFO, 
                "user_registered", 
                "User registration successful", 
                context);
                
            // You can also use standard SLF4J logging methods directly
            log.info("User registered: {}", user.getId());
        } catch (Exception e) {
            loggingUtils.logError(
                log,
                "registration_error",
                "User registration failed",
                e,
                context);
            throw e;
        }
    }
}
```

## Sample JSON Log Output

```json
{
  "@timestamp": "2025-04-26T05:50:44.405+0000",
  "@version": "1",
  "message": "HTTP GET /api/v1/health - 200 (79ms)",
  "logger_name": "com.thrivemarket.logging.config.RequestLoggingInterceptor",
  "thread_name": "http-nio-8080-exec-1",
  "level": "INFO",
  "level_value": 20000,
  "path": "/api/v1/health",
  "method": "GET",
  "requestId": "unknown",
  "clientIp": "0:0:0:0:0:0:0:1",
  "sessionId": "none",
  "durationMs": "79",
  "status": "200",
  "service": {
    "name": "my-service",
    "version": "0.0.1"
  }
}
```

## Migrating From Embedded Code

To migrate from the embedded code in your service to this library:

1. Remove the following classes from your codebase:
   - `LoggingUtils.java`
   - `RequestLoggingInterceptor.java` 
   - `WebConfig.java` (if only used for logging configuration)

2. Keep using SLF4J Logger interface:
   ```java
   // This is still correct - DO NOT change this!
   import org.slf4j.Logger;
   import org.slf4j.LoggerFactory;
   
   private static final Logger log = LoggerFactory.getLogger(YourClass.class);
   ```

3. Update utility class imports to use the library packages:
   ```java
   // Old
   import com.yourcompany.yourservice.config.LoggingUtils;
   
   // New
   import com.thrivemarket.logging.config.LoggingUtils;
   ```

   **Important:** Do NOT attempt to import `com.thrivemarket.logging.Logger` as this class does not exist.

4. Update your `application.yml` to use the new configuration properties:
   ```yaml
   thrivemarket:
     logging:
       request:
         enabled: true
   ```

5. Update your `logback-spring.xml` to include the template from the library.

## Contributing

We welcome contributions to improve this logging library. Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes with descriptive messages
4. Push to your branch (`git push origin feature/your-feature`)
5. Open a Pull Request

### Development

Build the project:
```bash
mvn clean install
```

Run tests:
```bash
mvn test
```

### Publishing to AWS CodeArtifact

To publish a new version to AWS CodeArtifact:

1. Export the CodeArtifact authorization token:
```bash
export CODEARTIFACT_AUTH_TOKEN=`aws codeartifact get-authorization-token --domain thrivemarket --domain-owner 904233098208 --region us-east-1 --query authorizationToken --output text`
```

2. Deploy to CodeArtifact:
```bash
mvn deploy
```

## License

Copyright © 2025 Thrive Market, Inc. All rights reserved.