package com.pghpizza.api.logging;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClientLogRequest(
        @NotBlank @Size(max = 20) String level,
        @NotBlank @Size(max = 1000) String message,
        @Size(max = 10000) String stack,
        Map<String, Object> details,
        Map<String, Object> context,
        @Size(max = 80) String occurredAt,
        @Size(max = 1000) String pageUrl,
        @Size(max = 1000) String userAgent
) {
}
