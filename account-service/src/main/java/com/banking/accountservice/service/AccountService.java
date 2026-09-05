package com.banking.accountservice.service;

import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.entity.*;
import com.banking.accountservice.repository.AccountRepository;
import com.banking.accountservice.repository.BalanceOperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final BalanceService balanceService;
    private static final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request){
        log.info("Creating account for : {}", request.getEmail());

        if(accountRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Account already exists for email : "+ request.getEmail());
        }

        Account account = Account.builder()
                .accountHolderName(request.getAccountHolderName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .accountType(request.getAccountType())
                .status(AccountStatus.ACTIVE)
                .balance(request.getInitialDeposit())
                .accountNumber(generateAccountNumber())
                .dailyTransactionLimit(
                        request.getAccountType() == AccountType.SAVINGS
                                ? new BigDecimal("100000")
                                : new BigDecimal("500000")
                )
                .build();

        Account saved = accountRepository.saveAndFlush(account);
        log.info("Account created of : {}", saved.getAccountNumber());
        return mapToResponse(saved);
    }

    public AccountResponse getAccount(String accountNumber){
        Account account = findByAccountNumber(accountNumber);
        return mapToResponse(account);
    }

    public BigDecimal getBalance(String accountNumber){
        return findByAccountNumber(accountNumber).getBalance();
    }

    @Transactional
    public void blockAccount(String accountNumber){
        log.info("Blocking account : {}", accountNumber);

        Account account = findByAccountNumber(accountNumber);
        account.setStatus(AccountStatus.BLOCKED);

        log.info("Account blocked : {}", accountNumber);
    }

    @Transactional
    public void unblockAccount(String accountNumber) {
        Account account = findByAccountNumber(accountNumber);

        account.setStatus(AccountStatus.ACTIVE);
    }

    @KafkaListener(topics = "transaction.completed")
    public void consumeTransactionCompleted(
            @Payload Map<String, Object> payload){
        try{
            String receiverAccount = (String)payload.get("receiverAccountNumber");
            BigDecimal amount = new BigDecimal(payload.get("amount").toString());
            String transactionId = (String) payload.get("transactionId");

            log.info("Crediting account : {} amount : {}", receiverAccount, amount);
            balanceService.creditBalance(
                    receiverAccount,
                    transactionId,
                    amount
            );
        }
        catch (Exception e){
            log.error("Error crediting account {}", e);
            throw e;
        }
    }

    @KafkaListener(topics = "fraud.detected")
    public void consumeFraudDetected(
            @Payload Map<String, Object> payload){
        try{
            String accountNumber = (String) payload.get("accountNumber");
            log.info("Fraud detected - blocking account : {}", accountNumber);
            blockAccount(accountNumber);
        }
        catch (Exception e){
            log.error("Error blocking account", e);
            throw e;
        }
    }

    private String generateAccountNumber(){
        String accountNumber;

        do{
            long number = secureRandom.nextLong(1_000_000_000_000L);
            accountNumber = String.format("%012d", number);
        }
        while(accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    private Account findByAccountNumber(String accountNumber){
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException(
                        "Account not found : "+accountNumber
                ));
    }

    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountHolderName(account.getAccountHolderName())
                .email(account.getEmail())
                .phone(account.getPhone())
                .accountType(account.getAccountType())
                .accountStatus(account.getStatus())
                .balance(account.getBalance())
                .dailyTransactionLimit(account.getDailyTransactionLimit())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
