package com.spring.resilience4j.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netsurfingzone.dto.Student;
import com.spring.resilience4j.exception.DownstreamNotAvailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class HelloWorldGateway {

    private final RestTemplate restTemplate;

    @CircuitBreaker(name = "netsurfing-topic-cb"/*, fallbackMethod = "fallbackForHelloWorld"*/)
    public void processMessage(Student message) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(message);
        log.info("Json message received using Kafka listener " + jsonString);
        throw new DownstreamNotAvailableException("Downstream not available exception");
    }

    public void fallbackForHelloWorld(Student message, Throwable ex) {
        log.error("Error in circuit breaker");
    }
}
