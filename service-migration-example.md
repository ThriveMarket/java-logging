# Example: Migrating a Service to Use the Java Logging Library

This document outlines the steps to migrate an existing service from using embedded logging code to using the standardized `java-logging` library.

## Step 1: Update Dependencies

### Maven

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.thrivemarket</groupId>
    <artifactId>java-logging</artifactId>
    <version>1.0.0</version>
</dependency>
```

And add the GitHub Packages repository:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/thrivemarket/java-logging</url>
    </repository>
</repositories>
```

### Gradle

For Gradle projects, add to your `build.gradle` or `build.gradle.kts`:

```kotlin
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/thrivemarket/java-logging")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_USERNAME")
            password = project.findProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("com.thrivemarket:java-logging:1.0.0")
}
```

## Step 2: Remove Embedded Logging Code

Delete the following files from your service (if they exist):

- `src/main/java/com/thrivemarket/starter/config/LoggingUtils.java`
- `src/main/java/com/thrivemarket/starter/config/RequestLoggingInterceptor.java`
- `src/main/java/com/thrivemarket/starter/config/WebConfig.java` (if only used for logging)

## Step 3: Update Imports

Search for imports of the embedded logging classes and update them to the library package:

```java
// Old
import com.thrivemarket.starter.config.LoggingUtils;
import com.thrivemarket.starter.config.RequestLoggingInterceptor;

// New
import com.thrivemarket.logging.config.LoggingUtils;
import com.thrivemarket.logging.config.RequestLoggingInterceptor;
```

## Step 4: Update Configuration

### Logback Configuration

1. Keep your existing `logback-spring.xml` or create one that includes the library's template:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="logback-json.xml"/>
    
    <!-- Your additional appenders or configuration here -->
</configuration>
```

### Application Properties

Update your `application.yml` or `application.properties`:

```yaml
# Old configuration
logging:
  level:
    com.thrivemarket.starter.config.RequestLoggingInterceptor: INFO

# New configuration
thrivemarket:
  logging:
    request:
      enabled: true
      detailed: false

logging:
  level:
    com.thrivemarket.logging.config.RequestLoggingInterceptor: INFO
```

## Step 5: Test the Integration

1. Build and run your service
2. Verify logs are being generated with the correct JSON structure
3. Check that request logging is working properly
4. Validate that any custom logging (using LoggingUtils) is functioning as expected

## Example: Updating a Service Class

### Before:

```java
package com.thrivemarket.starter.controller;

import com.thrivemarket.starter.api.HealthApi;
import com.thrivemarket.starter.config.LoggingUtils;
import com.thrivemarket.starter.model.HealthResponse;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController implements HealthApi {

  private static final Logger log = LoggerFactory.getLogger(HealthController.class);

  @Value("${spring.application.version:0.0.1}")
  private String version;

  @Autowired(required = false)
  private LoggingUtils loggingUtils;

  @Override
  public ResponseEntity<HealthResponse> getHealth() {
    // Create context information for structured logging
    Map<String, Object> context = new HashMap<>();
    context.put("version", version);

    // Log using structured logging if available, otherwise standard logging
    if (loggingUtils != null) {
      loggingUtils.logEvent(
          log, LoggingUtils.LogLevel.INFO, "health_check", "Health check executed", context);
    } else {
      log.info("Health check executed, version: {}", version);
    }

    HealthResponse response =
        new HealthResponse().status(HealthResponse.StatusEnum.UP).version(version);
    return ResponseEntity.ok(response);
  }
}
```

### After:

```java
package com.thrivemarket.starter.controller;

import com.thrivemarket.starter.api.HealthApi;
import com.thrivemarket.logging.config.LoggingUtils; // Updated import
import com.thrivemarket.starter.model.HealthResponse;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController implements HealthApi {

  private static final Logger log = LoggerFactory.getLogger(HealthController.class);

  @Value("${spring.application.version:0.0.1}")
  private String version;

  @Autowired(required = false)
  private LoggingUtils loggingUtils;

  @Override
  public ResponseEntity<HealthResponse> getHealth() {
    // Create context information for structured logging
    Map<String, Object> context = new HashMap<>();
    context.put("version", version);

    // Log using structured logging if available, otherwise standard logging
    if (loggingUtils != null) {
      loggingUtils.logEvent(
          log, LoggingUtils.LogLevel.INFO, "health_check", "Health check executed", context);
    } else {
      log.info("Health check executed, version: {}", version);
    }

    HealthResponse response =
        new HealthResponse().status(HealthResponse.StatusEnum.UP).version(version);
    return ResponseEntity.ok(response);
  }
}
```

The code remains almost identical, with only the import statement changing to use the library's package instead of the embedded code.