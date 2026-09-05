package com.banking.frauddetectionservice.repository;

import com.banking.frauddetectionservice.entity.FraudCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface FraudCheckRepository extends JpaRepository<FraudCheck, String> {

    Optional<FraudCheck> findByTransactionId(String transactionId);

    @Query("""
        SELECT AVG(f.amount)
        FROM FraudCheck f
        WHERE f.accountNumber = :accountNumber
    """)
    BigDecimal findAverageTransactionAmount(String accountNumber);
}
