package com.banking.accountservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "balance_operations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_transaction_operation",
                        columnNames = {"transaction_id", "operation_type"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(nullable = false)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    private BalanceOperationType operationType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}