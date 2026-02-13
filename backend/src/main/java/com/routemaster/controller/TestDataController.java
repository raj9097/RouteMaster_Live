package com.routemaster.controller;

import com.routemaster.service.TestDataGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/test-data")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TestDataController {

    private final TestDataGeneratorService testDataGenerator;

    @PostMapping("/generate-route-logs")
    public ResponseEntity<?> generateRouteLogs(@RequestParam(defaultValue = "100000") int count) {
        testDataGenerator.generateTestRouteLogs(count);
        return ResponseEntity.ok(new Response("Generated " + count + " route logs successfully"));
    }

    @DeleteMapping("/clear-route-logs")
    public ResponseEntity<?> clearRouteLogs() {
        testDataGenerator.clearRouteLogs();
        return ResponseEntity.ok(new Response("Cleared all route logs"));
    }

    public record Response(String message) {
    }
}
