package com.spring.resilience4j.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.resilience4j.constants.ApplicationConstant;
import com.netsurfingzone.dto.Student;
import com.spring.resilience4j.service.HelloWorldGateway;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaConsumer {

	private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

	private final HelloWorldGateway helloWorldGateway;

	@KafkaListener(groupId = ApplicationConstant.GROUP_ID_JSON, topics = ApplicationConstant.TOPIC_NAME,
		containerFactory = ApplicationConstant.KAFKA_LISTENER_CONTAINER_FACTORY, id = "netsurfing-topic")
	public void receivedMessage(Student message, Acknowledgment acknowledgment) throws JsonProcessingException {
		try {
			helloWorldGateway.processMessage(message);
			acknowledgment.acknowledge();
		} catch (Exception ex) {
			acknowledgment.nack(1000);
		}
	}
}
