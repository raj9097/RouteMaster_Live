package com.routemaster.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/batch")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class BatchController {
    
    private final JobLauncher jobLauncher;
    private final Job routeLogProcessingJob;
    
    @PostMapping("/process-route-logs")
    public ResponseEntity<?> processRouteLogs() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            
            var execution = jobLauncher.run(routeLogProcessingJob, jobParameters);
            
            log.info("Batch job started: {}", execution.getJobId());
            
            return ResponseEntity.ok(new BatchResponse(
                    execution.getJobId(),
                    execution.getStatus().name(),
                    "Batch job started successfully"
            ));
        } catch (Exception e) {
            log.error("Failed to start batch job", e);
            return ResponseEntity.internalServerError()
                    .body(new BatchResponse(null, "FAILED", e.getMessage()));
        }
    }
    
    public record BatchResponse(Long jobId, String status, String message) {}
}
