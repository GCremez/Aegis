package com.aegis.Aegis.state;

import com.aegis.Aegis.model.ProductState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class StateStore {
    private final ConcurrentHashMap<String, ProductSnapshot> store = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    private final AtomicLong totalUpdates = new AtomicLong(0);
    private final AtomicLong evictions = new AtomicLong(0);
    
    public StateStore() {
        // Schedule cleanup every 5 minutes
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredSnapshots, 5, 5, TimeUnit.MINUTES);
    }
    
    public ProductSnapshot getSnapshot(String productId) {
        return store.get(productId);
    }
    
    public ProductState getState(String productId) {
        ProductSnapshot snapshot = store.get(productId);
        return snapshot != null ? snapshot.getState() : null;
    }
    
    public ProductSnapshot putState(ProductState state) {
        totalUpdates.incrementAndGet();
        return store.compute(state.getId(), (productId, existingSnapshot) -> {
            if (existingSnapshot == null) {
                return new ProductSnapshot(state);
            } else {
                return existingSnapshot.update(state);
            }
        });
    }
    
    public ProductSnapshot removeState(String productId) {
        return store.remove(productId);
    }
    
    public int size() {
        return store.size();
    }
    
    public long getTotalUpdates() {
        return totalUpdates.get();
    }
    
    public long getEvictions() {
        return evictions.get();
    }
    
    private void cleanupExpiredSnapshots() {
        long ttlMs = TimeUnit.HOURS.toMillis(1); // 1 hour TTL
        long currentTime = System.currentTimeMillis();
        
        store.entrySet().removeIf(entry -> {
            ProductSnapshot snapshot = entry.getValue();
            boolean expired = currentTime - snapshot.getLastUpdated() > ttlMs;
            if (expired) {
                evictions.incrementAndGet();
                log.debug("Evicted expired snapshot for product: {}", entry.getKey());
            }
            return expired;
        });
    }
    
    public void shutdown() {
        cleanupExecutor.shutdown();
    }
}
