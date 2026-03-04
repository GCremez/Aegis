package com.aegis.Aegis.controller;

import com.aegis.Aegis.model.ProductState;
import com.aegis.Aegis.ingress.ProductUpdateConsumer;
import com.aegis.Aegis.state.StateStore;
import com.aegis.Aegis.state.ProductSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final ProductUpdateConsumer productUpdateConsumer;
    private final StateStore stateStore;

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        return ResponseEntity.ok(Map.of(
                "service", "Aegis - High-Performance Edge Delta Engine",
                "version", "1.0.0",
                "status", "running",
                "endpoints", Map.of(
                        "admin", "/api/v1/admin",
                        "health", "/actuator/health",
                        "metrics", "/actuator/metrics"
                ),
                "stats", Map.of(
                        "productsInStore", stateStore.size(),
                        "bufferSize", productUpdateConsumer.getBufferSize()
                )
        ));
    }
    
    @PostMapping("/products")
    public ResponseEntity<Map<String, Object>> ingestProduct(@RequestBody ProductState productState) {
        boolean ingested = productUpdateConsumer.ingest(productState);
        
        return ResponseEntity.ok(Map.of(
            "success", ingested,
            "productId", productState.getId(),
            "bufferSize", productUpdateConsumer.getBufferSize(),
            "stateStoreSize", stateStore.size()
        ));
    }
    
    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductState> getProduct(@PathVariable String productId) {
        ProductState state = stateStore.getState(productId);
        return state != null ? ResponseEntity.ok(state) : ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable String productId) {
        ProductSnapshot removed = stateStore.removeState(productId);
        boolean deleted = removed != null;
        
        return ResponseEntity.ok(Map.of(
            "deleted", deleted,
            "productId", productId,
            "remainingProducts", stateStore.size()
        ));
    }
    
    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> listProducts() {
        return ResponseEntity.ok(Map.of(
            "totalProducts", stateStore.size(),
            "totalUpdates", stateStore.getTotalUpdates(),
            "evictions", stateStore.getEvictions()
        ));
    }
    
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetSystem() {
        // Note: This would need to be implemented carefully in production
        return ResponseEntity.ok(Map.of(
            "message", "System reset not implemented in demo mode",
            "timestamp", System.currentTimeMillis()
        ));
    }
}
