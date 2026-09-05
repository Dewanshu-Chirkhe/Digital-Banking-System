package com.banking.transactionservice.kafka;

import com.banking.transactionservice.service.SagaService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionEventConsumer {

    private final SagaService sagaService;

    @KafkaListener(
            topics = "fraud.result",
            groupId = "transaction-service"
    )
    public void consume(FraudResultEvent event) {

        sagaService.handleFraudResult(
                event.getTransactionId(),
                event.getAccountNumber(),
                event.isApproved()
        );
    }
}