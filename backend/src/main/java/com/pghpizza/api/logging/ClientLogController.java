package com.pghpizza.api.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/client-logs")
public class ClientLogController {
    private static final Logger log = LoggerFactory.getLogger(ClientLogController.class);

    @PostMapping
    public ResponseEntity<Void> logClientError(@Valid @RequestBody ClientLogRequest request) {
        log.warn("Client {}: message='{}', pageUrl='{}', occurredAt='{}', context={}, details={}",
                request.level(),
                request.message(),
                request.pageUrl(),
                request.occurredAt(),
                request.context(),
                request.details());
        return ResponseEntity.accepted().build();
    }
}
