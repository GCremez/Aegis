package com.aegis.Aegis.controller;

import com.aegis.Aegis.egress.DeltaEventProducer;
import com.aegis.Aegis.ingress.ProductUpdateConsumer;
import com.aegis.Aegis.state.StateStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthController {
    
    private final ProductUpdateConsumer productUpdateConsumer;
    private final DeltaEventProducer deltaEventProducer;
    private final StateStore stateStore;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        boolean healthy = productUpdateConsumer.isRunning() && deltaEventProducer.isRunning();
        
        return ResponseEntity.ok(Map.of(
            "status", healthy ? "UP" : "DOWN",
            "consumer", productUpdateConsumer.isRunning() ? "UP" : "DOWN",
            "producer", deltaEventProducer.isRunning() ? "UP" : "DOWN",
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        boolean ready = productUpdateConsumer.isRunning() && deltaEventProducer.isRunning();
        
        return ResponseEntity.ok(Map.of(
            "status", ready ? "READY" : "NOT_READY",
            "components", Map.of(
                "consumer", productUpdateConsumer.isRunning(),
                "producer", deltaEventProducer.isRunning()
            )
        ));
    }
    
    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> liveness() {
        return ResponseEntity.ok(Map.of(
            "status", "ALIVE",
            "timestamp", System.currentTimeMillis()
        ));
    }
}
