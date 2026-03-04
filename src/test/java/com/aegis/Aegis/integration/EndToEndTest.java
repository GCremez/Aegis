package com.aegis.Aegis.integration;

import com.aegis.Aegis.model.ProductState;
import com.aegis.Aegis.egress.DeltaEventProducer;
import com.aegis.Aegis.ingress.ProductUpdateConsumer;
import com.aegis.Aegis.state.StateStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class EndToEndTest {
    
    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));
    
    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("aegis.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }
    
    @Autowired
    private ProductUpdateConsumer productUpdateConsumer;
    
    @Autowired
    private DeltaEventProducer deltaEventProducer;
    
    @Autowired
    private StateStore stateStore;
    
    @Test
    public void testEndToEndFlow() throws InterruptedException {
        // Create a product update
        ProductState product = new ProductState();
        product.setId("E2E-001");
        product.setName("E2E Test Product");
        product.setPrice(15000);
        product.setStock(75);
        product.setWarehouse("E2E-Warehouse");
        product.setLastUpdated(System.currentTimeMillis());
        
        // Ingest the product
        boolean ingested = productUpdateConsumer.ingest(product);
        assertTrue(ingested);
        
        // Wait for processing
        Thread.sleep(1000);
        
        // Verify state is stored
        ProductState storedState = stateStore.getState("E2E-001");
        assertNotNull(storedState);
        assertEquals("E2E-001", storedState.getId());
        assertEquals(15000, storedState.getPrice());
        assertEquals(75, storedState.getStock());
        
        // Update the product with price change
        product.setPrice(16000);
        product.setLastUpdated(System.currentTimeMillis());
        
        ingested = productUpdateConsumer.ingest(product);
        assertTrue(ingested);
        
        // Wait for processing
        Thread.sleep(1000);
        
        // Verify updated state
        ProductState updatedState = stateStore.getState("E2E-001");
        assertNotNull(updatedState);
        assertEquals(16000, updatedState.getPrice());
        
        // Verify events were produced
        long producedCount = deltaEventProducer.getProducedCount();
        assertTrue(producedCount >= 1); // At least one event should be produced
        
        // Verify processing metrics
        long processedCount = productUpdateConsumer.getProcessedCount();
        assertTrue(processedCount >= 2); // Two events should be processed
    }
    
    @Test
    public void testDuplicateUpdateFiltering() throws InterruptedException {
        ProductState product = new ProductState();
        product.setId("DUP-001");
        product.setName("Duplicate Test Product");
        product.setPrice(20000);
        product.setStock(100);
        product.setWarehouse("DUP-Warehouse");
        product.setLastUpdated(System.currentTimeMillis());
        
        // Ingest the same product twice
        productUpdateConsumer.ingest(product);
        Thread.sleep(100);
        
        long initialProcessedCount = productUpdateConsumer.getProcessedCount();
        long initialProducedCount = deltaEventProducer.getProducedCount();
        
        // Ingest identical product again
        productUpdateConsumer.ingest(product);
        Thread.sleep(1000);
        
        // Should not process duplicate
        long finalProcessedCount = productUpdateConsumer.getProcessedCount();
        long finalProducedCount = deltaEventProducer.getProducedCount();
        
        // The second identical event should be filtered out
        assertEquals(initialProcessedCount, finalProcessedCount);
        assertEquals(initialProducedCount, finalProducedCount);
    }
    
    @Test
    public void testHighVolumeUpdates() throws InterruptedException {
        int numUpdates = 1000;
        String productId = "HV-001";
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < numUpdates; i++) {
            ProductState product = new ProductState();
            product.setId(productId);
            product.setName("High Volume Product");
            product.setPrice(10000 + i);
            product.setStock(50 + (i % 20));
            product.setWarehouse("HV-Warehouse");
            product.setLastUpdated(System.currentTimeMillis());
            
            productUpdateConsumer.ingest(product);
        }
        
        // Wait for all processing to complete
        Thread.sleep(5000);
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Verify final state
        ProductState finalState = stateStore.getState(productId);
        assertNotNull(finalState);
        assertEquals(10000 + numUpdates - 1, finalState.getPrice());
        
        // Verify performance metrics
        long processedCount = productUpdateConsumer.getProcessedCount();
        long producedCount = deltaEventProducer.getProducedCount();
        
        System.out.println("High Volume Test Results:");
        System.out.println("Updates: " + numUpdates);
        System.out.println("Duration: " + duration + " ms");
        System.out.println("Processed: " + processedCount);
        System.out.println("Produced: " + producedCount);
        System.out.println("Throughput: " + (numUpdates * 1000.0 / duration) + " updates/sec");
        
        // Should have processed most updates (some may be filtered if identical)
        assertTrue(processedCount > numUpdates * 0.8);
        assertTrue(duration < 10000); // Should complete within 10 seconds
    }
}
