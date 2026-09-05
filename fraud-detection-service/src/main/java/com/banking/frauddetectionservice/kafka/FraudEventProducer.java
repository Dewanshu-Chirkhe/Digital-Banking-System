package com.banking.frauddetectionservice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FraudEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishFraudApproved(FraudApprovedEvent event) {

        kafkaTemplate.send(
                "fraud.approved",
                event.getTransactionId(),
                event
        );
    }

    public void publishVerificationRequired(
            VerificationRequiredEvent event) {

        kafkaTemplate.send(
                "verification.required",
                event.getTransactionId(),
                event
        );
    }
}