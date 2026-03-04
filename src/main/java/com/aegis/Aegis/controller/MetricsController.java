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
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class MetricsController {
    
    private final ProductUpdateConsumer productUpdateConsumer;
    private final DeltaEventProducer deltaEventProducer;
    private final StateStore stateStore;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMetrics() {
        return ResponseEntity.ok(Map.of(
            "ingress", Map.of(
                "bufferSize", productUpdateConsumer.getBufferSize(),
                "processedCount", productUpdateConsumer.getProcessedCount(),
                "errorCount", productUpdateConsumer.getErrorCount(),
                "isRunning", productUpdateConsumer.isRunning()
            ),
            "egress", Map.of(
                "queueSize", deltaEventProducer.getQueueSize(),
                "producedCount", deltaEventProducer.getProducedCount(),
                "errorCount", deltaEventProducer.getErrorCount(),
                "isRunning", deltaEventProducer.isRunning()
            ),
            "state", Map.of(
                "storeSize", stateStore.size(),
                "totalUpdates", stateStore.getTotalUpdates(),
                "evictions", stateStore.getEvictions()
            )
        ));
    }
    
    @GetMapping("/throughput")
    public ResponseEntity<Map<String, Object>> getThroughput() {
        long processed = productUpdateConsumer.getProcessedCount();
        long produced = deltaEventProducer.getProducedCount();
        
        return ResponseEntity.ok(Map.of(
            "eventsProcessed", processed,
            "eventsProduced", produced,
            "efficiency", processed > 0 ? (double) produced / processed : 0.0,
            "timestamp", System.currentTimeMillis()
        ));
    }
}
