package com.spring.resilience4j.config;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaManager {

    private final KafkaListenerEndpointRegistry registry;

    public void pause(final String listenerId) {
        registry.getListenerContainers()
            .stream()
            .filter(messageListenerContainer -> messageListenerContainer.getListenerId().equals(listenerId))
            .forEach(MessageListenerContainer::pause);
    }

    public void resume(final String listenerId) {
        registry.getListenerContainers()
            .stream()
            .filter(messageListenerContainer -> messageListenerContainer.getListenerId().equals(listenerId))
            .forEach(MessageListenerContainer::resume);
    }
}
