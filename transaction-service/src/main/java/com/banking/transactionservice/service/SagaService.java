package com.banking.transactionservice.service;

import com.banking.transactionservice.client.AccountServiceClient;
import com.banking.transactionservice.entity.Transaction;
import com.banking.transactionservice.entity.TransactionStatus;
import com.banking.transactionservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SagaService {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;

    @Transactional
    public void handleFraudResult(
            String transactionId,
            String accountNumber,
            boolean approved) {

        Transaction transaction =
                transactionRepository.findByTransactionId(transactionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Transaction not found: " + transactionId));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            return;
        }

        if (approved) {
            // Fraud approved → credit receiver
            accountServiceClient.creditBalance(
                    transaction.getReceiverAccountNumber(),
                    transactionId,
                    transaction.getAmount()
            );

            transaction.setStatus(TransactionStatus.COMPLETED);

        } else {
            // Fraud detected → compensate sender
            accountServiceClient.creditBalance(
                    transaction.getSenderAccountNumber(),
                    transactionId,
                    transaction.getAmount()
            );

            transaction.setStatus(TransactionStatus.FAILED);
        }
    }
}