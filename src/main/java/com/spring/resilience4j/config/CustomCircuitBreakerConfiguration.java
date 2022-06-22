package com.spring.resilience4j.config;

import com.spring.resilience4j.exception.DownstreamNotAvailableException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreaker.StateTransition;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.timelimiter.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class CustomCircuitBreakerConfiguration {

    @Bean
    public RegistryEventConsumer<CircuitBreaker> circuitBreakerEventConsumer(final KafkaManager kafkaManager) {
        return new RegistryEventConsumer<CircuitBreaker>() {

            @Override
            public void onEntryAddedEvent(EntryAddedEvent<CircuitBreaker> entryAddedEvent) {
                entryAddedEvent.getAddedEntry().getEventPublisher()
                    .onFailureRateExceeded(event -> {
                        log.error("circuit breaker {} failure rate {} on {}",
                            event.getCircuitBreakerName(), event.getFailureRate(), event.getCreationTime());
                        }
                    )
                    .onSlowCallRateExceeded(event -> {
                        log.error("circuit breaker {} slow call rate {} on {}",
                            event.getCircuitBreakerName(), event.getSlowCallRate(), event.getCreationTime());
                        }
                    )
                    .onCallNotPermitted(event -> {
                            log.error("circuit breaker {} call not permitted {}",
                                event.getCircuitBreakerName(), event.getCreationTime());
                            throw new DownstreamNotAvailableException("Downstream not available");
                        }
                    )
                    .onError(event -> {
                        log.error("circuit breaker {} error with duration {}s",
                            event.getCircuitBreakerName(), event.getElapsedDuration().getSeconds());
                        throw new DownstreamNotAvailableException("Downstream not available");
                        }
                    )
                    .onStateTransition(event -> {
                        String listenerName = event.getCircuitBreakerName().replace("-cb", "");
                        switch (event.getStateTransition()) {
                            case CLOSED_TO_OPEN:
                            case CLOSED_TO_FORCED_OPEN:
                            case HALF_OPEN_TO_OPEN:
                                kafkaManager.pause(listenerName);
                                break;
                            case OPEN_TO_HALF_OPEN:
                            case HALF_OPEN_TO_CLOSED:
                            case FORCED_OPEN_TO_CLOSED:
                            case FORCED_OPEN_TO_HALF_OPEN:
                                kafkaManager.resume(listenerName);
                                break;
                            default:
                                throw new IllegalStateException("Unknown transition state: " + event.getStateTransition());
                        }
                        log.warn("circuit breaker {} state transition from {} to {} on {}",
                            event.getCircuitBreakerName(), event.getStateTransition().getFromState(),
                            event.getStateTransition().getToState(), event.getCreationTime());
                        }
                    ).onSuccess(event -> {
                        log.warn("circuit breaker {} state transition from {} to {} on {}",
                            event.getCircuitBreakerName(), event.getElapsedDuration().getSeconds());
                        }
                    );
            }

            @Override
            public void onEntryRemovedEvent(EntryRemovedEvent<CircuitBreaker> entryRemoveEvent) { }

            @Override
            public void onEntryReplacedEvent(EntryReplacedEvent<CircuitBreaker> entryReplacedEvent) { }
        };
    }

    @Bean
    public RegistryEventConsumer<TimeLimiter> timeLimiterEventConsumer() {
        return new RegistryEventConsumer<TimeLimiter>() {
            @Override
            public void onEntryAddedEvent(EntryAddedEvent<TimeLimiter> entryAddedEvent) {
                entryAddedEvent.getAddedEntry().getEventPublisher()
                    .onTimeout(event -> {
                        log.error("time limiter {} timeout {} on {}",
                            event.getTimeLimiterName(), event.getEventType(), event.getCreationTime());
                        }
                    );
            }

            @Override
            public void onEntryRemovedEvent(EntryRemovedEvent<TimeLimiter> entryRemoveEvent) { }

            @Override
            public void onEntryReplacedEvent(EntryReplacedEvent<TimeLimiter> entryReplacedEvent) { }
        };
    }

    /*@Bean
    public Customizer<Resilience4JCircuitBreakerFactory> globalCustomConfiguration() {
        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(4))
            .build();
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(5)
            .waitDurationInOpenState(Duration.ofMillis(1000))
            .slidingWindowSize(2)
            .build();

        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
            .timeLimiterConfig(timeLimiterConfig)
            .circuitBreakerConfig(circuitBreakerConfig)
            .build());
    }*/

    /*public CustomCircuitBreakerConfiguration(CircuitBreakerRegistry circuitBreakerRegistry, KafkaManager kafkaManager) {
        circuitBreakerRegistry.circuitBreaker("StudentConsumer").getEventPublisher().onStateTransition(event -> {

            switch (event.getStateTransition()) {
                case CLOSED_TO_OPEN:
                case CLOSED_TO_FORCED_OPEN:
                case HALF_OPEN_TO_OPEN:
                    kafkaManager.pause(event.getCircuitBreakerName());
                    break;
                case OPEN_TO_HALF_OPEN:
                case HALF_OPEN_TO_CLOSED:
                case FORCED_OPEN_TO_CLOSED:
                case FORCED_OPEN_TO_HALF_OPEN:
                    kafkaManager.resume(event.getCircuitBreakerName());
                    break;
                default:
                    throw new IllegalStateException("Unknown transition state: " + event.getStateTransition());
            }
        });
    }

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> globalCustomConfiguration() {
        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(4))
            .build();
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofMillis(1000))
            .slidingWindowSize(2)
            .build();

        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
            .timeLimiterConfig(timeLimiterConfig)
            .circuitBreakerConfig(circuitBreakerConfig)
            .build());
    }

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> specificCustomConfiguration1() {

        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(4))
            .build();
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofMillis(1000))
            .slidingWindowSize(2)
            .build();

        return factory -> factory.configure(builder -> builder.circuitBreakerConfig(circuitBreakerConfig)
            .timeLimiterConfig(timeLimiterConfig).build(), "circuitBreaker");
    }

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> specificCustomConfiguration2() {

        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(4))
            .build();
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofMillis(1000))
            .slidingWindowSize(2)
            .build();

        return factory -> factory.configure(builder -> builder.circuitBreakerConfig(circuitBreakerConfig)
                .timeLimiterConfig(timeLimiterConfig).build(),
            "circuitBreaker1", "circuitBreaker2", "circuitBreaker3");
    }*/
}
