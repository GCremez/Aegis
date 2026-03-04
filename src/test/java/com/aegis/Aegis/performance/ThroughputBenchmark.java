package com.aegis.Aegis.performance;

import com.aegis.Aegis.model.ProductState;
import com.aegis.Aegis.ingress.ProductUpdateConsumer;
import com.aegis.Aegis.state.StateStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ThroughputBenchmark {
    
    @Autowired
    private ProductUpdateConsumer productUpdateConsumer;
    
    @Autowired
    private StateStore stateStore;
    
    @Test
    public void benchmarkThroughput() throws InterruptedException {
        int numThreads = Runtime.getRuntime().availableProcessors();
        int eventsPerThread = 10000;
        int totalEvents = numThreads * eventsPerThread;
        
        CountDownLatch latch = new CountDownLatch(numThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                Random random = new Random(threadId);
                List<ProductState> events = generateEvents(eventsPerThread, random);
                
                for (ProductState event : events) {
                    productUpdateConsumer.ingest(event);
                }
                
                latch.countDown();
            });
        }
        
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();
        
        executor.shutdown();
        
        // Wait for processing to complete
        Thread.sleep(2000);
        
        if (completed) {
            long duration = endTime - startTime;
            double throughput = (double) totalEvents / duration * 1000; // events per second
            
            System.out.println("=== Throughput Benchmark Results ===");
            System.out.println("Total Events: " + totalEvents);
            System.out.println("Duration: " + duration + " ms");
            System.out.println("Throughput: " + String.format("%.2f", throughput) + " events/sec");
            System.out.println("Processed: " + productUpdateConsumer.getProcessedCount());
            System.out.println("Errors: " + productUpdateConsumer.getErrorCount());
            System.out.println("State Store Size: " + stateStore.size());
            
            // Assert reasonable performance
            assertTrue(throughput > 10000, "Throughput should be at least 10,000 events/sec");
        } else {
            fail("Benchmark did not complete within timeout");
        }
    }
    
    @Test
    public void benchmarkMemoryUsage() {
        int numProducts = 100000;
        
        long beforeMemory = getUsedMemory();
        
        List<ProductState> products = new ArrayList<>();
        for (int i = 0; i < numProducts; i++) {
            ProductState product = new ProductState();
            product.setId("P" + i);
            product.setName("Product " + i);
            product.setPrice(10000 + (i % 1000));
            product.setStock(50 + (i % 100));
            product.setWarehouse("Warehouse-" + (i % 10));
            product.setLastUpdated(System.currentTimeMillis());
            
            products.add(product);
            productUpdateConsumer.ingest(product);
        }
        
        // Wait for processing
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long afterMemory = getUsedMemory();
        long memoryUsed = afterMemory - beforeMemory;
        double bytesPerProduct = (double) memoryUsed / numProducts;
        
        System.out.println("=== Memory Usage Benchmark Results ===");
        System.out.println("Products: " + numProducts);
        System.out.println("Memory Used: " + (memoryUsed / 1024 / 1024) + " MB");
        System.out.println("Bytes per Product: " + String.format("%.2f", bytesPerProduct));
        System.out.println("State Store Size: " + stateStore.size());
        
        // Assert memory efficiency (should be under 1KB per product)
        assertTrue(bytesPerProduct < 1024, "Memory usage should be under 1KB per product");
    }
    
    private List<ProductState> generateEvents(int count, Random random) {
        List<ProductState> events = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            ProductState product = new ProductState();
            product.setId("P" + random.nextInt(1000));
            product.setName("Product " + product.getId());
            product.setPrice(10000 + random.nextInt(10000));
            product.setStock(random.nextInt(100));
            product.setWarehouse("Warehouse-" + random.nextInt(10));
            product.setLastUpdated(System.currentTimeMillis());
            
            events.add(product);
        }
        
        return events;
    }
    
    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}
