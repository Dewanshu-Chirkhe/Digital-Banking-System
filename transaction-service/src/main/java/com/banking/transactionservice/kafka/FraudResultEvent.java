package com.banking.transactionservice.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FraudResultEvent {

    private String transactionId;
    private String accountNumber;
    private boolean approved;
}