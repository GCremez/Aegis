package com.aegis.Aegis.egress;

import com.aegis.Aegis.model.DeltaEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeltaEventConsumer {
    
    private long consumedCount = 0;
    private long errorCount = 0;
    
    @KafkaListener(
        topics = "${aegis.kafka.topic:delta-events}",
        groupId = "aegis-consumer-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeDeltaEvent(
            @Payload DeltaEvent deltaEvent,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("Consumed delta event: productId={}, type={}, delta={}, topic={}, partition={}, offset={}", 
                     deltaEvent.getId(), deltaEvent.getType(), deltaEvent.getDelta(), topic, partition, offset);
            
            // Process the delta event (this would be sent to downstream services)
            processDeltaEvent(deltaEvent);
            
            // Acknowledge the message
            acknowledgment.acknowledge();
            
            consumedCount++;
            
            if (consumedCount % 100 == 0) {
                log.info("Consumed {} delta events", consumedCount);
            }
            
        } catch (Exception e) {
            errorCount++;
            log.error("Error processing delta event: productId={}, type={}, error={}", 
                     deltaEvent.getId(), deltaEvent.getType(), e.getMessage(), e);
        }
    }
    
    private void processDeltaEvent(DeltaEvent deltaEvent) {
        // Simulate processing time
        try {
            Thread.sleep(1); // 1ms processing time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Here you would:
        // 1. Route to specific downstream service based on event type
        // 2. Apply business logic transformations
        // 3. Update downstream systems
        // 4. Send acknowledgments back if needed
        
        log.debug("Processed delta event: productId={}, type={}", 
                  deltaEvent.getId(), deltaEvent.getType());
    }
    
    public long getConsumedCount() {
        return consumedCount;
    }
    
    public long getErrorCount() {
        return errorCount;
    }
}
