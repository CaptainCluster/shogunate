package com.tvtracker.common;

import com.tvtracker.common.exception.NotFoundException;
import com.tvtracker.common.openapi.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "System")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Health check")
    @SecurityRequirements
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Service is running")})
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @GetMapping("/demo/error")
    @Operation(summary = "Demo structured error response")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "No error triggered"),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Structured error payload when trigger=true",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> demoError(
            @Parameter(description = "When true, throws a sample NotFoundException")
                    @RequestParam(defaultValue = "false")
                    boolean trigger) {
        if (trigger) {
            throw new NotFoundException("Demo error for verification");
        }
        return ResponseEntity.ok().build();
    }
}
