package com.aegis.Aegis.ingress;

import com.aegis.Aegis.model.ProductState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductUpdateConsumer {
    private final RingBufferWrapper ringBuffer;
    private final DisruptorEventHandler eventHandler;
    private volatile boolean running = false;
    
    public ProductUpdateConsumer(RingBufferWrapper ringBuffer, DisruptorEventHandler eventHandler) {
        this.ringBuffer = ringBuffer;
        this.eventHandler = eventHandler;
    }
    
    public boolean ingest(ProductState productState) {
        if (!running) {
            log.warn("Consumer not started, dropping event for product: {}", productState.getId());
            return false;
        }
        
        boolean published = ringBuffer.publish(productState);
        if (!published) {
            log.warn("Ring buffer full, dropping event for product: {}", productState.getId());
        }
        
        return published;
    }
    
    public void start() {
        if (running) {
            return;
        }
        
        running = true;
        eventHandler.start(ringBuffer);
        log.info("Product update consumer started");
    }
    
    public void stop() {
        running = false;
        eventHandler.stop();
        log.info("Product update consumer stopped");
    }
    
    public int getBufferSize() {
        return ringBuffer.size();
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public long getProcessedCount() {
        return eventHandler.getProcessedCount();
    }
    
    public long getErrorCount() {
        return eventHandler.getErrorCount();
    }
}
