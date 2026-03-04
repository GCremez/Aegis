package com.aegis.Aegis.state;

import com.aegis.Aegis.model.ProductState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StateStoreTest {
    
    private StateStore stateStore;
    
    @BeforeEach
    public void setUp() {
        stateStore = new StateStore();
    }
    
    @Test
    public void testPutAndGetState() {
        ProductState product = createTestProduct("P001", 10000, 50, "Warehouse-A");
        
        ProductSnapshot snapshot = stateStore.putState(product);
        
        assertNotNull(snapshot);
        assertEquals(product, snapshot.getState());
        assertEquals(1, snapshot.getVersion());
        assertEquals(1, snapshot.getUpdateCount());
        
        ProductState retrieved = stateStore.getState("P001");
        assertNotNull(retrieved);
        assertEquals(product.getId(), retrieved.getId());
        assertEquals(product.getPrice(), retrieved.getPrice());
    }
    
    @Test
    public void testUpdateExistingState() {
        ProductState product1 = createTestProduct("P002", 10000, 50, "Warehouse-A");
        ProductState product2 = createTestProduct("P002", 12000, 50, "Warehouse-A");
        
        stateStore.putState(product1);
        ProductSnapshot updatedSnapshot = stateStore.putState(product2);
        
        assertNotNull(updatedSnapshot);
        assertEquals(2, updatedSnapshot.getVersion());
        assertEquals(2, updatedSnapshot.getUpdateCount());
        
        ProductState retrieved = stateStore.getState("P002");
        assertEquals(12000, retrieved.getPrice());
    }
    
    @Test
    public void testRemoveState() {
        ProductState product = createTestProduct("P003", 10000, 50, "Warehouse-A");
        
        stateStore.putState(product);
        assertEquals(1, stateStore.size());
        
        ProductSnapshot removed = stateStore.removeState("P003");
        assertNotNull(removed);
        assertEquals(0, stateStore.size());
        
        ProductState retrieved = stateStore.getState("P003");
        assertNull(retrieved);
    }
    
    @Test
    public void testGetSnapshot() {
        ProductState product = createTestProduct("P004", 10000, 50, "Warehouse-A");
        
        stateStore.putState(product);
        ProductSnapshot snapshot = stateStore.getSnapshot("P004");
        
        assertNotNull(snapshot);
        assertEquals(product, snapshot.getState());
        assertTrue(snapshot.getLastUpdated() > 0);
    }
    
    @Test
    public void testTotalUpdates() {
        ProductState product1 = createTestProduct("P005", 10000, 50, "Warehouse-A");
        ProductState product2 = createTestProduct("P005", 12000, 50, "Warehouse-A");
        
        assertEquals(0, stateStore.getTotalUpdates());
        
        stateStore.putState(product1);
        assertEquals(1, stateStore.getTotalUpdates());
        
        stateStore.putState(product2);
        assertEquals(2, stateStore.getTotalUpdates());
    }
    
    @Test
    public void testExpiredSnapshot() throws InterruptedException {
        ProductState product = createTestProduct("P006", 10000, 50, "Warehouse-A");
        
        ProductSnapshot snapshot = new ProductSnapshot(product);
        
        // Should not be expired immediately
        assertFalse(snapshot.isExpired(1000));
        
        // Should be expired after waiting
        Thread.sleep(10);
        assertTrue(snapshot.isExpired(5)); // 5ms TTL
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
