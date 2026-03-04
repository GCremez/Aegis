package com.aegis.Aegis.egress;

import com.aegis.Aegis.model.DeltaEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class DeltaEventProducer {
    private final KafkaTemplate<String, DeltaEvent> kafkaTemplate;
    private final BlockingQueue<DeltaEvent> eventQueue = new LinkedBlockingQueue<>(10000);
    private final ExecutorService executorService;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong producedCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    
    public DeltaEventProducer(@Qualifier("deltaEventKafkaTemplate") KafkaTemplate<String, DeltaEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.executorService = Executors.newFixedThreadPool(2);
    }
    
    public boolean produceEvent(DeltaEvent event) {
        boolean queued = eventQueue.offer(event);
        if (!queued) {
            log.warn("Event queue full, dropping delta event for product: {}", event.getId());
        }
        return queued;
    }
    
    public void start() {
        if (running.compareAndSet(false, true)) {
            // Start producer thread
            executorService.submit(() -> {
                while (running.get()) {
                    try {
                        DeltaEvent event = eventQueue.take();
                        publishEvent(event);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
            
            // Start monitoring thread
            executorService.submit(() -> {
                while (running.get()) {
                    try {
                        Thread.sleep(1000);
                        log.debug("Event queue size: {}, produced: {}, errors: {}", 
                                 eventQueue.size(), producedCount.get(), errorCount.get());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
            
            log.info("Delta event producer started");
        }
    }
    
    public void stop() {
        running.set(false);
        executorService.shutdown();
        log.info("Delta event producer stopped");
    }
    
    private void publishEvent(DeltaEvent event) {
        try {
            kafkaTemplate.send("delta-events", event.getId(), event)
                .whenComplete((result, failure) -> {
                    if (failure != null) {
                        errorCount.incrementAndGet();
                        log.error("Failed to publish delta event for product: {}", event.getId(), failure);
                    } else {
                        producedCount.incrementAndGet();
                        log.debug("Published delta event for product: {}", event.getId());
                    }
                });
        } catch (Exception e) {
            errorCount.incrementAndGet();
            log.error("Error publishing delta event for product: {}", event.getId(), e);
        }
    }
    
    public int getQueueSize() {
        return eventQueue.size();
    }
    
    public long getProducedCount() {
        return producedCount.get();
    }
    
    public long getErrorCount() {
        return errorCount.get();
    }
    
    public boolean isRunning() {
        return running.get();
    }
}
