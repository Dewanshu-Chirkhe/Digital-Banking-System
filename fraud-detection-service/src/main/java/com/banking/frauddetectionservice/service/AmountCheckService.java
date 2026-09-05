package com.banking.frauddetectionservice.service;

import com.banking.frauddetectionservice.repository.FraudCheckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AmountCheckService {

    private final FraudCheckRepository fraudCheckRepository;

    @Value("${fraud.amount.average-multiplier}")
    private BigDecimal averageMultiplier;

    public boolean isSuspicious(
            String accountNumber,
            BigDecimal currentAmount) {

        BigDecimal average =
                fraudCheckRepository.findAverageTransactionAmount(accountNumber);

        // No transaction history
        if (average == null || average.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }

        BigDecimal threshold = average.multiply(averageMultiplier);

        return currentAmount.compareTo(threshold) > 0;
    }

    public BigDecimal getAverageTransactionAmount(String accountNumber) {
        return fraudCheckRepository.findAverageTransactionAmount(accountNumber);
    }
}