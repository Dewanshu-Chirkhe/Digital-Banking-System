package com.banking.accountservice.controller;

import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.dto.BalanceOperationRequest;
import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.entity.BalanceOperationResult;
import com.banking.accountservice.service.AccountService;
import com.banking.accountservice.service.BalanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final BalanceService balanceService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccount(request));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(
            @PathVariable String accountNumber){
        return ResponseEntity.ok(accountService.getAccount(accountNumber));
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<BigDecimal> getBalance(
            @PathVariable String accountNumber){
        return ResponseEntity.ok(accountService.getBalance(accountNumber));
    }

    @PutMapping("/{accountNumber}/block")
    public ResponseEntity<String> blockAccount(
            @PathVariable String accountNumber){
        accountService.blockAccount(accountNumber);
        return ResponseEntity.ok("Account blocked successfully");
    }

    @PutMapping("/{accountNumber}/unblock")
    public ResponseEntity<String> unblockAccount(
            @PathVariable String accountNumber) {

        accountService.unblockAccount(accountNumber);

        return ResponseEntity.ok("Account unblocked successfully");
    }

    /*
        SAGA STEP 1 - Deduct Balance
        Called by Transaction Service when transfer is initiated
     */
    @PutMapping("/{accountNumber}/deduct")
    public ResponseEntity<String> deductBalance(
            @PathVariable String accountNumber,
            @Valid @RequestBody BalanceOperationRequest request) {

        BalanceOperationResult result = balanceService.deductBalance(
                accountNumber,
                request.getTransactionId(),
                request.getAmount()
        );

        if (result == BalanceOperationResult.ALREADY_PROCESSED) {
            return ResponseEntity.ok("Debit already processed for this transaction");
        }

        return ResponseEntity.ok("Amount deducted successfully");
    }

    @PutMapping("/{accountNumber}/credit")
    public ResponseEntity<String> creditBalance(
            @PathVariable String accountNumber,
            @Valid @RequestBody BalanceOperationRequest request) {

        BalanceOperationResult result = balanceService.creditBalance(
                accountNumber,
                request.getTransactionId(),
                request.getAmount()
        );

        if (result == BalanceOperationResult.ALREADY_PROCESSED) {
            return ResponseEntity.ok("Credit already processed for this transaction");
        }

        return ResponseEntity.ok("Amount credited successfully");
    }
}
