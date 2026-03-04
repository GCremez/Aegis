package com.aegis.Aegis.config;

import org.apache.kafka.clients.consumer.Consumer;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@EnableKafka
@ConfigurationProperties(prefix = "aegis.kafka")
public class KafkaConfig {
    private String bootstrapServers = "localhost:9092";
    private String topic = "delta-events";
    private int acks = 1;
    private int lingerMs = 0;
    private String compressionType = "lz4";
    
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put("bootstrap.servers", bootstrapServers);
        configProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        configProps.put("value.serializer", "org.springframework.kafka.support.serializer.JsonSerializer");
        configProps.put("acks", String.valueOf(acks));
        configProps.put("linger.ms", String.valueOf(lingerMs));
        configProps.put("compression.type", compressionType);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    @Bean
    public ProducerFactory<String, com.aegis.Aegis.model.DeltaEvent> deltaEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put("bootstrap.servers", bootstrapServers);
        configProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        configProps.put("value.serializer", "org.springframework.kafka.support.serializer.JsonSerializer");
        configProps.put("acks", String.valueOf(acks));
        configProps.put("linger.ms", String.valueOf(lingerMs));
        configProps.put("compression.type", compressionType);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
    
    @Bean
    public KafkaTemplate<String, com.aegis.Aegis.model.DeltaEvent> deltaEventKafkaTemplate() {
        return new KafkaTemplate<>(deltaEventProducerFactory());
    }

    @Bean
    public ConsumerFactory<String, Object>ConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put("bootstrap.servers", bootstrapServers);
        configProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        configProps.put("value.deserializer", "org.springframework.kafka.support.serializer.JsonDeserializer");
        configProps.put("group.id", "aegis-consumer-group");
        configProps.put("auto.offset.reset", "earliest");
        configProps.put("enable.auto.commit", "false");

        return new DefaultKafkaConsumerFactory<>(configProps);
    }
    
    // Getters and Setters
    public String getBootstrapServers() {
        return bootstrapServers;
    }
    
    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }
    
    public String getTopic() {
        return topic;
    }
    
    public void setTopic(String topic) {
        this.topic = topic;
    }
    
    public int getAcks() {
        return acks;
    }
    
    public void setAcks(int acks) {
        this.acks = acks;
    }
    
    public int getLingerMs() {
        return lingerMs;
    }
    
    public void setLingerMs(int lingerMs) {
        this.lingerMs = lingerMs;
    }
    
    public String getCompressionType() {
        return compressionType;
    }
    
    public void setCompressionType(String compressionType) {
        this.compressionType = compressionType;
    }
}
