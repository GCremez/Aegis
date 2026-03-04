package com.aegis.Aegis.ingress;

import com.aegis.Aegis.model.ProductState;
import com.aegis.Aegis.state.DeltaDetector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DisruptorEventHandler {
    private final DeltaDetector deltaDetector;
    private volatile boolean running = false;
    private Thread handlerThread;
    private long processedCount = 0;
    private long errorCount = 0;
    
    public DisruptorEventHandler(DeltaDetector deltaDetector) {
        this.deltaDetector = deltaDetector;
    }
    
    public void start(RingBufferWrapper ringBuffer) {
        if (running) {
            return;
        }
        
        running = true;
        handlerThread = new Thread(() -> {
            while (running) {
                try {
                    ProductState event = ringBuffer.consume();
                    if (event != null) {
                        deltaDetector.processProductUpdate(event);
                        processedCount++;
                        
                        if (processedCount % 10000 == 0) {
                            log.info("Processed {} events", processedCount);
                        }
                    } else {
                        Thread.yield();
                    }
                } catch (Exception e) {
                    errorCount++;
                    log.error("Error processing event", e);
                }
            }
        }, "disruptor-handler");
        
        handlerThread.start();
        log.info("Disruptor event handler started");
    }
    
    public void stop() {
        running = false;
        if (handlerThread != null) {
            try {
                handlerThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        log.info("Disruptor event handler stopped");
    }
    
    public long getProcessedCount() {
        return processedCount;
    }
    
    public long getErrorCount() {
        return errorCount;
    }
    
    public boolean isRunning() {
        return running;
    }
}
