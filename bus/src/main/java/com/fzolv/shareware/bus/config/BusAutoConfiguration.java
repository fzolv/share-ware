package com.fzolv.shareware.bus.config;

import com.fzolv.shareware.bus.api.MessageBus;
import com.fzolv.shareware.bus.kafka.KafkaMessageBus;
import com.fzolv.shareware.bus.rabbit.RabbitMessageBus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BusAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MessageBus.class)
    public MessageBus messageBus(BusProperties props, KafkaMessageBus kafka, RabbitMessageBus rabbit) {
        String provider = props.getProvider() == null ? "kafka" : props.getProvider().toLowerCase();
        switch (provider) {
            case "rabbit":
            case "rabbitmq":
                return rabbit;
            case "kafka":
            default:
                return kafka;
        }
    }
}


