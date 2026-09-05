package com.banking.frauddetectionservice.kafka;

import com.banking.frauddetectionservice.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FraudEventConsumer {

    private final FraudDetectionService fraudDetectionService;

    @KafkaListener(
            topics = "transaction.initiated",
            groupId = "fraud-detection-service"
    )
    public void consume(TransactionEvent event) {

        System.out.println(
                "Received transaction for fraud check: "
                        + event.getTransactionId()
        );

        fraudDetectionService.checkTransaction(event);
    }
}