package com.banking.transactionservice.client;

import com.banking.transactionservice.dto.BalanceOperationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class AccountServiceClient {

    private final RestClient restClient;

    public void deductBalance(
            String accountNumber,
            String transactionId,
            BigDecimal amount){

        BalanceOperationRequest request = new BalanceOperationRequest(transactionId, amount);

        restClient.put()
                .uri("/api/v1/accounts/{accountNumber}/deduct", accountNumber)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    public void creditBalance(
            String accountNumber,
            String transactionId,
            BigDecimal amount) {

        BalanceOperationRequest request =
                new BalanceOperationRequest(transactionId, amount);

        restClient.put()
                .uri("/api/v1/accounts/{accountNumber}/credit", accountNumber)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
