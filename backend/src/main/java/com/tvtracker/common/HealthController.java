package com.tvtracker.common;

import com.tvtracker.common.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Health check")
    @ApiResponse(responseCode = "200", description = "Service is running")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @GetMapping("/demo/error")
    @Operation(summary = "Demo structured error response")
    @ApiResponse(responseCode = "404", description = "Structured error payload")
    public ResponseEntity<Void> demoError(@RequestParam(defaultValue = "false") boolean trigger) {
        if (trigger) {
            throw new NotFoundException("Demo error for verification");
        }
        return ResponseEntity.ok().build();
    }
}
