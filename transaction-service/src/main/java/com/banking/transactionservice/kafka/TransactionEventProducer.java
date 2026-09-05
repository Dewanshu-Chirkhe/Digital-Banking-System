package com.banking.transactionservice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionEventProducer {

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    private static final String TOPIC = "transaction.initiated";

    public void publishTransaction(TransactionEvent event) {
        kafkaTemplate.send(TOPIC, event.getTransactionId(), event);
    }

}
