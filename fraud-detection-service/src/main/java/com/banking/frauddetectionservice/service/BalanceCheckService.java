package com.banking.frauddetectionservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BalanceCheckService {

    private final RestClient restClient;

    public BigDecimal getAccountBalance(String accountNumber) {

        return restClient.get()
                .uri(
                        "http://localhost:8080/api/v1/accounts/{accountNumber}/balance",
                        accountNumber
                )
                .retrieve()
                .body(BigDecimal.class);
    }

    public boolean isSuspicious(
            BigDecimal currentBalance,
            BigDecimal amount) {

        BigDecimal balanceBefore = currentBalance.add(amount);

        BigDecimal ninetyPercent =
                balanceBefore.multiply(new BigDecimal("0.90"));

        return amount.compareTo(ninetyPercent) > 0;
    }
}