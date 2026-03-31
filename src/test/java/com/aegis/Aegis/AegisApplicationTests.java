package com.aegis.Aegis;

import com.aegis.Aegis.config.TestKafkaConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * Basic Spring Boot application context test.
 * This test verifies that the application context can load successfully
 * without requiring a real Kafka broker.
 */
@SpringBootTest
@Import(TestKafkaConfig.class)
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=localhost:9092",
    "aegis.kafka.bootstrap-servers=localhost:9092"
})
class AegisApplicationTests {

	@Test
	void contextLoads() {
		// This test verifies that the Spring context loads successfully
		// with the test Kafka configuration
	}

}
