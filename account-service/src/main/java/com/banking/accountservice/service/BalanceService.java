package com.banking.accountservice.service;

import com.banking.accountservice.entity.*;
import com.banking.accountservice.repository.AccountRepository;
import com.banking.accountservice.repository.BalanceOperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceService {

    private final AccountRepository accountRepository;
    private final BalanceOperationRepository balanceOperationRepository;

    @Transactional
    public BalanceOperationResult deductBalance(String accountNumber, String transactionId, BigDecimal amount){
        log.info("Deducting {} from account {}", amount, accountNumber);
        Account account = findByAccountNumber(accountNumber);

        if (balanceOperationRepository
                .existsByTransactionIdAndOperationType(
                        transactionId,
                        BalanceOperationType.DEBIT)) {

            log.info("Debit already processed for transaction {}", transactionId);
            return BalanceOperationResult.ALREADY_PROCESSED;
        }

        if(account.getStatus() != AccountStatus.ACTIVE){
            throw new RuntimeException("Account is not active : "+accountNumber);
        }

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        if(account.getBalance().compareTo(amount) < 0){
            throw new RuntimeException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(amount));

        BalanceOperation operation = BalanceOperation.builder()
                .transactionId(transactionId)
                .accountNumber(accountNumber)
                .operationType(BalanceOperationType.DEBIT)
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .build();

        balanceOperationRepository.save(operation);
        return BalanceOperationResult.SUCCESS;
    }

    @Transactional
    public BalanceOperationResult creditBalance(String accountNumber, String transactionId, BigDecimal amount){
        log.info("Crediting {} to account {}", amount, accountNumber);
        Account account = findByAccountNumber(accountNumber);

        if (balanceOperationRepository
                .existsByTransactionIdAndOperationType(
                        transactionId,
                        BalanceOperationType.CREDIT)) {

            log.info("Credit already processed for transaction {}", transactionId);
            return BalanceOperationResult.ALREADY_PROCESSED;
        }

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        account.setBalance(account.getBalance().add(amount));

        BalanceOperation operation = BalanceOperation.builder()
                .transactionId(transactionId)
                .accountNumber(accountNumber)
                .operationType(BalanceOperationType.CREDIT)
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .build();

        balanceOperationRepository.save(operation);
        return BalanceOperationResult.SUCCESS;
    }

    private Account findByAccountNumber(String accountNumber){
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException(
                        "Account not found : "+accountNumber
                ));
    }
}
