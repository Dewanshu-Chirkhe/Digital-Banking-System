package com.banking.frauddetectionservice.service;

import com.banking.frauddetectionservice.entity.FraudCheck;
import com.banking.frauddetectionservice.entity.FraudCheckStatus;
import com.banking.frauddetectionservice.kafka.TransactionEvent;
import com.banking.frauddetectionservice.repository.FraudCheckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final VelocityCheckService velocityCheckService;
    private final AmountCheckService amountCheckService;
    private final BalanceCheckService balanceCheckService;
    private final FraudCheckRepository fraudCheckRepository;

    public FraudCheck checkTransaction(TransactionEvent event) {

        String accountNumber = event.getSenderAccountNumber();
        BigDecimal amount = event.getAmount();

        // Run all three fraud checks
        boolean velocityTriggered =
                velocityCheckService.isSuspicious(accountNumber);

        BigDecimal averageAmount =
                amountCheckService.getAverageTransactionAmount(accountNumber);

        boolean amountTriggered =
                amountCheckService.isSuspicious(accountNumber, amount);

        BigDecimal currentBalance =
                balanceCheckService.getAccountBalance(accountNumber);

        boolean balanceTriggered =
                balanceCheckService.isSuspicious(currentBalance, amount);

        // Any suspicious check requires verification
        FraudCheckStatus status =
                velocityTriggered || amountTriggered || balanceTriggered
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

        return fraudCheckRepository.save(fraudCheck);
    }
}