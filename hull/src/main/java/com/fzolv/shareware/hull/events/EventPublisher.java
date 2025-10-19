package com.fzolv.shareware.hull.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Method;

@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final Object messageBus; // resolved reflectively to avoid hard dependency on bus module in lint phase
    private final ObjectMapper objectMapper;

    public EventPublisher(ApplicationContext applicationContext) {
        this.objectMapper = new ObjectMapper();
        Object resolved = null;
        try {
            Class<?> busType = Class.forName("com.fzolv.shareware.bus.api.MessageBus");
            resolved = applicationContext.getBean(busType);
        } catch (Throwable t) {
            log.warn("MessageBus bean not found; events will be no-ops: {}", t.getMessage());
        }
        this.messageBus = resolved;
    }

    public void publish(String topic, String type, Map<String, Object> payload) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", type);
        event.put("payload", payload);
        try {
            String json = objectMapper.writeValueAsString(event);
            if (messageBus != null) {
                Method publish = messageBus.getClass().getMethod("publish", String.class, String.class);
                publish.invoke(messageBus, topic, json);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event: {}", e.getMessage(), e);
        } catch (Throwable t) {
            log.error("Failed to publish event: {}", t.getMessage(), t);
        }
    }
}


