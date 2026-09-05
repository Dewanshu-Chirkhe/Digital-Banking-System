package com.banking.frauddetectionservice.service;

import com.banking.frauddetectionservice.entity.FraudCheck;
import com.banking.frauddetectionservice.entity.FraudCheckStatus;
import com.banking.frauddetectionservice.kafka.FraudApprovedEvent;
import com.banking.frauddetectionservice.kafka.FraudEventProducer;
import com.banking.frauddetectionservice.kafka.TransactionEvent;
import com.banking.frauddetectionservice.kafka.VerificationRequiredEvent;
import com.banking.frauddetectionservice.repository.FraudCheckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final VelocityCheckService velocityCheckService;
    private final AmountCheckService amountCheckService;
    private final BalanceCheckService balanceCheckService;
    private final FraudCheckRepository fraudCheckRepository;
    private final FraudEventProducer fraudEventProducer;

    public FraudCheck checkTransaction(TransactionEvent event) {

        String accountNumber = event.getSenderAccountNumber();
        BigDecimal amount = event.getAmount();

        // Run each check exactly once
        boolean velocityTriggered =
                velocityCheckService.isSuspicious(accountNumber);

        BigDecimal averageAmount =
                amountCheckService.getAverageTransactionAmount(accountNumber);

        boolean amountTriggered =
                amountCheckService.isSuspicious(accountNumber, amount);

        BigDecimal currentBalance =
                balanceCheckService.getAccountBalance(accountNumber);

        boolean balanceTriggered =
                balanceCheckService.isSuspicious(
                        currentBalance,
                        amount
                );

        boolean suspicious =
                velocityTriggered ||
                        amountTriggered ||
                        balanceTriggered;

        FraudCheckStatus status = suspicious
                ? FraudCheckStatus.VERIFICATION_REQUIRED
                : FraudCheckStatus.APPROVED;

        FraudCheck fraudCheck = FraudCheck.builder()
                .transactionId(event.getTransactionId())
                .accountNumber(accountNumber)
                .amount(amount)
                .velocityTriggered(velocityTriggered)
                .amountTriggered(amountTriggered)
                .balanceTriggered(balanceTriggered)
                .averageTransactionAmount(averageAmount)
                .accountBalance(currentBalance)
                .status(status)
                .build();

        FraudCheck saved = fraudCheckRepository.save(fraudCheck);

        if (suspicious) {
            publishVerificationRequired(event, velocityTriggered,
                    amountTriggered, balanceTriggered);
        } else {
            publishFraudApproved(event);
        }

        return saved;
    }

    private void publishFraudApproved(TransactionEvent event) {

        fraudEventProducer.publishFraudApproved(
                new FraudApprovedEvent(
                        event.getTransactionId(),
                        event.getSenderAccountNumber()
                )
        );
    }

    private void publishVerificationRequired(
            TransactionEvent event,
            boolean velocityTriggered,
            boolean amountTriggered,
            boolean balanceTriggered) {

        List<String> reasons = new ArrayList<>();

        if (velocityTriggered) {
            reasons.add("VELOCITY");
        }

        if (amountTriggered) {
            reasons.add("AMOUNT");
        }

        if (balanceTriggered) {
            reasons.add("BALANCE");
        }

        fraudEventProducer.publishVerificationRequired(
                new VerificationRequiredEvent(
                        event.getTransactionId(),
                        event.getSenderAccountNumber(),
                        event.getAmount(),
                        reasons
                )
        );
    }
}