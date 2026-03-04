package com.aegis.Aegis.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeltaEvent {
    private String id;
    private EventType type;
    private int delta;
    private int current;
    private long timestamp;
    private String oldValue;
    private String newValue;
    
    public static DeltaEvent priceChange(String id, int oldPrice, int newPrice) {
        EventType type = newPrice > oldPrice ? EventType.PRICE_INCREASE : EventType.PRICE_DECREASE;
        return new DeltaEvent(id, type, newPrice - oldPrice, newPrice, System.currentTimeMillis(), 
                           String.valueOf(oldPrice), String.valueOf(newPrice));
    }
    
    public static DeltaEvent stockChange(String id, int oldStock, int newStock) {
        EventType type = newStock > oldStock ? EventType.STOCK_INCREASE : EventType.STOCK_DECREASE;
        return new DeltaEvent(id, type, newStock - oldStock, newStock, System.currentTimeMillis(),
                           String.valueOf(oldStock), String.valueOf(newStock));
    }
    
    public static DeltaEvent warehouseChange(String id, String oldWarehouse, String newWarehouse) {
        return new DeltaEvent(id, EventType.WAREHOUSE_CHANGE, 0, 0, System.currentTimeMillis(),
                           oldWarehouse, newWarehouse);
    }
    
    public static DeltaEvent nameChange(String id, String oldName, String newName) {
        return new DeltaEvent(id, EventType.NAME_CHANGE, 0, 0, System.currentTimeMillis(),
                           oldName, newName);
    }
    
    public static DeltaEvent productCreated(String id) {
        return new DeltaEvent(id, EventType.PRODUCT_CREATED, 0, 0, System.currentTimeMillis(),
                           null, null);
    }
}
