package com.fzolv.shareware.bus.kafka;

import com.fzolv.shareware.bus.api.MessageBus;
import com.fzolv.shareware.bus.config.BusProperties;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Component
public class KafkaMessageBus implements MessageBus {

    private final BusProperties props;
    private volatile KafkaProducer<String, String> producer;
    private final Map<String, KafkaConsumer<String, String>> consumers = new ConcurrentHashMap<>();

    public KafkaMessageBus(BusProperties props) {
        this.props = props;
    }

    private Properties producerProps() {
        Properties p = new Properties();
        p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, props.getKafkaBootstrapServers());
        p.put(ProducerConfig.CLIENT_ID_CONFIG, props.getKafkaClientId());
        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return p;
    }

    private Properties consumerProps(String groupId) {
        Properties p = new Properties();
        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, props.getKafkaBootstrapServers());
        p.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return p;
    }

    @Override
    public void createTopic(String topic, int partitions, short replicationFactor) {
        try (AdminClient admin = AdminClient.create(Map.of("bootstrap.servers", props.getKafkaBootstrapServers()))) {
            admin.createTopics(Collections.singletonList(new NewTopic(topic, partitions, replicationFactor)));
        }
    }

    @Override
    public void subscribe(String topic, String subscriptionId, Consumer<String> handler) {
        String key = topic + "|" + subscriptionId;
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps(subscriptionId));
        consumer.subscribe(Collections.singletonList(topic));
        consumers.put(key, consumer);
        // naive polling loop should be run by caller via poll(); this method only registers subscription
    }

    @Override
    public List<String> poll(String topic, int maxMessages, Duration timeout) {
        String key = topic + "|" + props.getKafkaClientId();
        KafkaConsumer<String, String> consumer = consumers.computeIfAbsent(key, k -> {
            KafkaConsumer<String, String> c = new KafkaConsumer<>(consumerProps(props.getKafkaClientId()));
            c.subscribe(Collections.singletonList(topic));
            return c;
        });
        List<String> records = new ArrayList<>();
        for (ConsumerRecord<String, String> rec : consumer.poll(timeout)) {
            records.add(rec.value());
            if (records.size() >= maxMessages) break;
        }
        return records;
    }

    @Override
    public void publish(String topic, String message) {
        if (producer == null) {
            synchronized (this) {
                if (producer == null) {
                    producer = new KafkaProducer<>(producerProps());
                }
            }
        }
        producer.send(new ProducerRecord<>(topic, message));
    }
}


