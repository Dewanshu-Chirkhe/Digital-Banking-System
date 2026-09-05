package com.banking.frauddetectionservice.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FraudApprovedEvent {

    private String transactionId;
    private String accountNumber;
}