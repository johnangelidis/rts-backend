package com.rts.application.controller;

import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    private final BuildProperties buildProperties;

    public SystemController(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @GetMapping("/status")
    public ResponseEntity<SystemStatusResponse> status() {
        return ResponseEntity.ok(new SystemStatusResponse(
                "UP", "rts-application", buildProperties.getVersion(), Instant.now()));
    }

    public record SystemStatusResponse(String status, String service, String version, Instant timestamp) {
    }
}
