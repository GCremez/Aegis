package com.aegis.Aegis.state;

import com.aegis.Aegis.model.ProductState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSnapshot {
    private ProductState state;
    private long lastUpdated;
    private long version;
    private int updateCount;
    
    public ProductSnapshot(ProductState state) {
        this.state = state;
        this.lastUpdated = System.currentTimeMillis();
        this.version = 1;
        this.updateCount = 1;
    }
    
    public ProductSnapshot update(ProductState newState) {
        this.state = newState;
        this.lastUpdated = System.currentTimeMillis();
        this.version++;
        this.updateCount++;
        return this;
    }
    
    public boolean isExpired(long ttlMs) {
        return System.currentTimeMillis() - lastUpdated > ttlMs;
    }
}
