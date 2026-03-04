package com.aegis.Aegis.config;

import com.aegis.Aegis.ingress.RingBufferWrapper;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "aegis.disruptor")
public class DisruptorConfig {
    private int bufferSize = 65536;
    private int consumerThreads = 1;
    private int producerThreads = 1;
    
    @Bean
    public RingBufferWrapper ringBuffer() {
        return new RingBufferWrapper(bufferSize);
    }
    
    // Getters and Setters
    public int getBufferSize() {
        return bufferSize;
    }
    
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
    
    public int getConsumerThreads() {
        return consumerThreads;
    }
    
    public void setConsumerThreads(int consumerThreads) {
        this.consumerThreads = consumerThreads;
    }
    
    public int getProducerThreads() {
        return producerThreads;
    }
    
    public void setProducerThreads(int producerThreads) {
        this.producerThreads = producerThreads;
    }
}
