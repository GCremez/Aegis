package com.aegis.Aegis.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductState {
    private String id;
    private String name;
    private int price;
    private int stock;
    private String warehouse;
    @JsonDeserialize(using = LenientLongDeserializer.class)
    private long lastUpdated;
    
    public int calculateDeltaMask(ProductState previousState) {
        int deltaMask = 0;
        
        if (previousState == null) {
            return FieldMask.ID_MASK | FieldMask.NAME_MASK | FieldMask.PRICE_MASK | 
                   FieldMask.STOCK_MASK | FieldMask.WAREHOUSE_MASK | FieldMask.LAST_UPDATED_MASK;
        }
        
        if (!this.id.equals(previousState.id)) {
            deltaMask |= FieldMask.ID_MASK;
        }
        
        if (!this.name.equals(previousState.name)) {
            deltaMask |= FieldMask.NAME_MASK;
        }
        
        if (this.price != previousState.price) {
            deltaMask |= FieldMask.PRICE_MASK;
        }
        
        if (this.stock != previousState.stock) {
            deltaMask |= FieldMask.STOCK_MASK;
        }
        
        if (!this.warehouse.equals(previousState.warehouse)) {
            deltaMask |= FieldMask.WAREHOUSE_MASK;
        }
        
        if (this.lastUpdated != previousState.lastUpdated) {
            deltaMask |= FieldMask.LAST_UPDATED_MASK;
        }
        
        return deltaMask;
    }
}
