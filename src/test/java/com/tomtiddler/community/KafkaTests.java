package com.tomtiddler.community;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class KafkaTests {
    @Autowired
    private KafkaProducer producer;

    @Test
    public void testKafka() {
        producer.sendMsg("test", "你好啊");
        producer.sendMsg("test", "spring send message");

        try {
            Thread.sleep(1000 * 10);
        } catch (Exception e) {
            //TODO: handle exception
            e.printStackTrace();
        }

    }
}

/**
 * InnerKafkaTests
 */
@Component
class KafkaProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void sendMsg(String topic, String content) {
        kafkaTemplate.send(topic, content);
    }

}

/**
 * InnerKafkaTests
 */
@Component
class KafkaConsumer {
    @KafkaListener(topics = { "test" })
    public void handleMsg(ConsumerRecord record) {
        System.out.println(record.value());
    }
}