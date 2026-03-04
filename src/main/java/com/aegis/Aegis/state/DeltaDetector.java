package com.aegis.Aegis.state;

import com.aegis.Aegis.model.DeltaEvent;
import com.aegis.Aegis.model.EventType;
import com.aegis.Aegis.model.FieldMask;
import com.aegis.Aegis.model.ProductState;
import com.aegis.Aegis.egress.DeltaEventProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeltaDetector {
    private final StateStore stateStore;
    private final DeltaEventProducer eventProducer;
    
    public DeltaDetector(StateStore stateStore, DeltaEventProducer eventProducer) {
        this.stateStore = stateStore;
        this.eventProducer = eventProducer;
    }
    
    public void processProductUpdate(ProductState newProductState) {
        ProductSnapshot existingSnapshot = stateStore.getSnapshot(newProductState.getId());
        ProductState existingState = existingSnapshot != null ? existingSnapshot.getState() : null;
        
        int deltaMask = newProductState.calculateDeltaMask(existingState);
        
        if (deltaMask == 0) {
            log.debug("No changes detected for product: {}", newProductState.getId());
            return;
        }
        
        // Emit delta events for changed fields
        emitDeltaEvents(existingState, newProductState, deltaMask);
        
        // Update state store
        stateStore.putState(newProductState);
        
        log.debug("Processed delta for product: {}, changed fields: {}", 
                 newProductState.getId(), FieldMask.maskToString(deltaMask));
    }
    
    private void emitDeltaEvents(ProductState oldState, ProductState newState, int deltaMask) {
        if (oldState == null) {
            // New product creation
            eventProducer.produceEvent(DeltaEvent.productCreated(newState.getId()));
            return;
        }
        
        // Price changes
        if (FieldMask.hasFieldChanged(deltaMask, FieldMask.PRICE_MASK)) {
            DeltaEvent priceEvent = DeltaEvent.priceChange(newState.getId(), oldState.getPrice(), newState.getPrice());
            eventProducer.produceEvent(priceEvent);
        }
        
        // Stock changes
        if (FieldMask.hasFieldChanged(deltaMask, FieldMask.STOCK_MASK)) {
            DeltaEvent stockEvent = DeltaEvent.stockChange(newState.getId(), oldState.getStock(), newState.getStock());
            eventProducer.produceEvent(stockEvent);
        }
        
        // Warehouse changes
        if (FieldMask.hasFieldChanged(deltaMask, FieldMask.WAREHOUSE_MASK)) {
            DeltaEvent warehouseEvent = DeltaEvent.warehouseChange(newState.getId(), oldState.getWarehouse(), newState.getWarehouse());
            eventProducer.produceEvent(warehouseEvent);
        }
        
        // Name changes
        if (FieldMask.hasFieldChanged(deltaMask, FieldMask.NAME_MASK)) {
            DeltaEvent nameEvent = DeltaEvent.nameChange(newState.getId(), oldState.getName(), newState.getName());
            eventProducer.produceEvent(nameEvent);
        }
    }
    
    public long getProcessedCount() {
        return stateStore.getTotalUpdates();
    }
}
