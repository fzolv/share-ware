package com.fzolv.shareware.bus.api;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

public interface MessageBus {

    void createTopic(String topic, int partitions, short replicationFactor);

    void subscribe(String topic, String subscriptionId, Consumer<String> handler);

    List<String> poll(String topic, int maxMessages, Duration timeout);

    void publish(String topic, String message);
}


