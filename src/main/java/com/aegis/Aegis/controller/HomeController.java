package com.aegis.Aegis.controller;

import com.aegis.Aegis.ingress.ProductUpdateConsumer;
import com.aegis.Aegis.state.StateStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HomeController {

    private final ProductUpdateConsumer productUpdateConsumer;
    private final StateStore stateStore;

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        return ResponseEntity.ok(Map.of(
                "service", "Aegis - High-Performance Edge Delta Engine",
                "description", "Sub-millisecond state differentiation and event squashing for high-scale retail ecosystems",
                "version", "1.0.0",
                "status", "operational",
                "timestamp", Instant.now().toString(),
                "endpoints", Map.of(
                        "root", "/",
                        "status", "/status",
                        "admin", "/api/v1/admin",
                        "products", "/api/v1/admin/products",
                        "health", "/actuator/health",
                        "metrics", "/actuator/metrics"
                ),
                "stats", Map.of(
                        "productsInStore", stateStore.size(),
                        "bufferSize", productUpdateConsumer.getBufferSize(),
                        "totalUpdates", stateStore.getTotalUpdates()
                )
        ));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        long maxMemory = runtime.maxMemory() / 1024 / 1024;

        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "timestamp", Instant.now().toString(),
                "uptime", java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime() / 1000 + "s",
                "memory", Map.of(
                        "used", usedMemory + "MB",
                        "max", maxMemory + "MB",
                        "available", (maxMemory - usedMemory) + "MB"
                ),
                "cache", Map.of(
                        "products", stateStore.size(),
                        "updates", stateStore.getTotalUpdates()
                )
        ));
    }
}