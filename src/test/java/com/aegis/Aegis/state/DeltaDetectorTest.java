package com.aegis.Aegis.state;

import com.aegis.Aegis.model.DeltaEvent;
import com.aegis.Aegis.model.ProductState;
import com.aegis.Aegis.egress.DeltaEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DeltaDetectorTest {
    
    @Mock
    private StateStore stateStore;
    
    @Mock
    private DeltaEventProducer eventProducer;
    
    private DeltaDetector deltaDetector;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        deltaDetector = new DeltaDetector(stateStore, eventProducer);
    }
    
    @Test
    public void testNewProductDetection() {
        ProductState newProduct = createTestProduct("P001", 10000, 50, "Warehouse-A");
        
        when(stateStore.getSnapshot("P001")).thenReturn(null);
        when(stateStore.putState(any())).thenReturn(null);
        
        deltaDetector.processProductUpdate(newProduct);
        
        ArgumentCaptor<DeltaEvent> eventCaptor = ArgumentCaptor.forClass(DeltaEvent.class);
        verify(eventProducer, times(1)).produceEvent(eventCaptor.capture());
        
        DeltaEvent event = eventCaptor.getValue();
        assertEquals("P001", event.getId());
        assertEquals(com.aegis.Aegis.model.EventType.PRODUCT_CREATED, event.getType());
    }
    
    @Test
    public void testPriceChangeDetection() {
        ProductState oldProduct = createTestProduct("P002", 10000, 50, "Warehouse-A");
        ProductState newProduct = createTestProduct("P002", 12000, 50, "Warehouse-A");
        
        ProductSnapshot snapshot = new ProductSnapshot(oldProduct);
        when(stateStore.getSnapshot("P002")).thenReturn(snapshot);
        when(stateStore.putState(any())).thenReturn(null);
        
        deltaDetector.processProductUpdate(newProduct);
        
        ArgumentCaptor<DeltaEvent> eventCaptor = ArgumentCaptor.forClass(DeltaEvent.class);
        verify(eventProducer, times(1)).produceEvent(eventCaptor.capture());
        
        DeltaEvent event = eventCaptor.getValue();
        assertEquals("P002", event.getId());
        assertEquals(com.aegis.Aegis.model.EventType.PRICE_INCREASE, event.getType());
        assertEquals(2000, event.getDelta());
    }
    
    @Test
    public void testNoChangeDetection() {
        ProductState product = createTestProduct("P003", 10000, 50, "Warehouse-A");
        
        ProductSnapshot snapshot = new ProductSnapshot(product);
        when(stateStore.getSnapshot("P003")).thenReturn(snapshot);
        
        deltaDetector.processProductUpdate(product);
        
        verify(eventProducer, never()).produceEvent(any());
        verify(stateStore, never()).putState(any());
    }
    
    @Test
    public void testMultipleFieldChanges() {
        ProductState oldProduct = createTestProduct("P004", 10000, 50, "Warehouse-A");
        ProductState newProduct = createTestProduct("P004", 12000, 30, "Warehouse-B");
        
        ProductSnapshot snapshot = new ProductSnapshot(oldProduct);
        when(stateStore.getSnapshot("P004")).thenReturn(snapshot);
        when(stateStore.putState(any())).thenReturn(null);
        
        deltaDetector.processProductUpdate(newProduct);
        
        ArgumentCaptor<DeltaEvent> eventCaptor = ArgumentCaptor.forClass(DeltaEvent.class);
        verify(eventProducer, times(3)).produceEvent(eventCaptor.capture());
        
        List<DeltaEvent> events = eventCaptor.getAllValues();
        assertTrue(events.stream().anyMatch(e -> e.getType() == com.aegis.Aegis.model.EventType.PRICE_INCREASE));
        assertTrue(events.stream().anyMatch(e -> e.getType() == com.aegis.Aegis.model.EventType.STOCK_DECREASE));
        assertTrue(events.stream().anyMatch(e -> e.getType() == com.aegis.Aegis.model.EventType.WAREHOUSE_CHANGE));
    }
    
    private ProductState createTestProduct(String id, int price, int stock, String warehouse) {
        ProductState product = new ProductState();
        product.setId(id);
        product.setName("Test Product " + id);
        product.setPrice(price);
        product.setStock(stock);
        product.setWarehouse(warehouse);
        product.setLastUpdated(System.currentTimeMillis());
        return product;
    }
}
