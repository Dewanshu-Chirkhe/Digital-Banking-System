package com.banking.frauddetectionservice.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerificationRequiredEvent {

    private String transactionId;
    private String accountNumber;
    private BigDecimal amount;
    private List<String> reasons;
}