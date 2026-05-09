package rw.blcp.backend.core.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.blcp.backend.auth.RoleName;
import rw.blcp.backend.auth.annotation.RequiredRoles;
import rw.blcp.backend.core.dto.ApiResponse;

@RestController
@RequestMapping("/api/v1")
public class HealthPrivateController {

    @GetMapping("/health")
    @RequiredRoles({RoleName.OFFICER, RoleName.SENIOR_OFFICER})
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        return ResponseEntity.ok(ApiResponse.of(Map.of("status", "UP")));
    }
}
