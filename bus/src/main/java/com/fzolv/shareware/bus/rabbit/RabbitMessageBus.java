package com.fzolv.shareware.bus.rabbit;

import com.fzolv.shareware.bus.api.MessageBus;
import com.fzolv.shareware.bus.config.BusProperties;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Component
public class RabbitMessageBus implements MessageBus {

    private final BusProperties props;
    private final CachingConnectionFactory connectionFactory;
    private final RabbitTemplate template;
    private final RabbitAdmin admin;

    private final Map<String, SimpleMessageListenerContainer> subscriptions = new ConcurrentHashMap<>();

    public RabbitMessageBus(BusProperties props) {
        this.props = props;
        this.connectionFactory = new CachingConnectionFactory(props.getRabbitHost(), props.getRabbitPort());
        this.connectionFactory.setUsername(props.getRabbitUsername());
        this.connectionFactory.setPassword(props.getRabbitPassword());
        this.template = new RabbitTemplate(connectionFactory);
        this.admin = new RabbitAdmin(connectionFactory);
    }

    @Override
    public void createTopic(String topic, int partitions, short replicationFactor) {
        admin.declareQueue(new Queue(topic, true));
    }

    @Override
    public void subscribe(String topic, String subscriptionId, Consumer<String> handler) {
        String key = topic + "|" + subscriptionId;
        subscriptions.computeIfAbsent(key, k -> {
            SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
            container.setQueueNames(topic);
            container.setAutoStartup(true);
            container.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.AUTO);
            container.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
                String payload = new String(message.getBody(), StandardCharsets.UTF_8);
                handler.accept(payload);
            });
            container.start();
            return container;
        });
    }

    @Override
    public List<String> poll(String topic, int maxMessages, Duration timeout) {
        long timeoutMs = timeout == null ? 0L : timeout.toMillis();
        long deadline = timeoutMs <= 0 ? 0 : System.currentTimeMillis() + timeoutMs;
        List<String> messages = new ArrayList<>();
        while (messages.size() < maxMessages) {
            Message m = timeoutMs > 0 ? template.receive(topic, Math.max(1L, deadline - System.currentTimeMillis())) : template.receive(topic);
            if (m == null) {
                if (timeoutMs > 0 && System.currentTimeMillis() < deadline) {
                    continue;
                }
                break;
            }
            messages.add(new String(m.getBody(), StandardCharsets.UTF_8));
        }
        return messages;
    }

    @Override
    public void publish(String topic, String message) {
        template.convertAndSend(topic, message);
    }
}


