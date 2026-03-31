package com.aegis.Aegis.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.*;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Test configuration for Kafka.
 * This configuration provides test-specific Kafka beans that can be overridden
 * by integration tests using Testcontainers.
 */
@TestConfiguration
public class TestKafkaConfig {
    
    @Bean
    @Primary
    public ProducerFactory<String, Object> testProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put("bootstrap.servers", "localhost:9092");
        configProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        configProps.put("value.serializer", "org.springframework.kafka.support.serializer.JsonSerializer");
        configProps.put("acks", "1");
        configProps.put("linger.ms", "0");
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    @Bean
    @Primary
    public KafkaTemplate<String, Object> testKafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
    
    @Bean
    @Primary
    public ConsumerFactory<String, Object> testConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put("bootstrap.servers", "localhost:9092");
        configProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        configProps.put("value.deserializer", "org.springframework.kafka.support.serializer.JsonDeserializer");
        configProps.put("group.id", "test-group");
        configProps.put("auto.offset.reset", "earliest");
        configProps.put("enable.auto.commit", "false");
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }
    
    @Bean
    @Primary
    public ConcurrentKafkaListenerContainerFactory<String, Object> testKafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {
        
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(1);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.getContainerProperties().setPollTimeout(1000);
        // Don't auto-start containers in tests
        factory.setAutoStartup(false);
        
        return factory;
    }
}
