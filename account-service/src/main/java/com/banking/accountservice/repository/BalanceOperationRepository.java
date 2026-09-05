package com.banking.accountservice.repository;

import com.banking.accountservice.entity.BalanceOperation;
import com.banking.accountservice.entity.BalanceOperationType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BalanceOperationRepository
        extends JpaRepository<BalanceOperation, String> {

    boolean existsByTransactionIdAndOperationType(
            String transactionId,
            BalanceOperationType operationType
    );
}