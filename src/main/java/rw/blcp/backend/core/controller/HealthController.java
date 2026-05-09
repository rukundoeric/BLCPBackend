package rw.blcp.backend.core.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.blcp.backend.web.dto.ApiResponse;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/public")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        return ResponseEntity.ok(ApiResponse.of(Map.of("status", "UP")));
    }
}
