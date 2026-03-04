package com.aegis.Aegis.config;

import com.aegis.Aegis.egress.DeltaEventProducer;
import com.aegis.Aegis.ingress.ProductUpdateConsumer;
import com.aegis.Aegis.state.StateStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PreDestroy;

@Slf4j
@Configuration
public class AegisConfig implements ApplicationListener<ApplicationReadyEvent> {
    
    private final ProductUpdateConsumer productUpdateConsumer;
    private final DeltaEventProducer deltaEventProducer;
    private final StateStore stateStore;
    
    public AegisConfig(ProductUpdateConsumer productUpdateConsumer, 
                      DeltaEventProducer deltaEventProducer,
                      StateStore stateStore) {
        this.productUpdateConsumer = productUpdateConsumer;
        this.deltaEventProducer = deltaEventProducer;
        this.stateStore = stateStore;
    }
    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Starting Aegis Delta Engine...");
        
        // Start the pipeline components
        deltaEventProducer.start();
        productUpdateConsumer.start();
        
        log.info("Aegis Delta Engine started successfully!");
    }
    
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down Aegis Delta Engine...");
        
        productUpdateConsumer.stop();
        deltaEventProducer.stop();
        stateStore.shutdown();
        
        log.info("Aegis Delta Engine shutdown complete");
    }
}
