package com.banking.transactionservice.controller;

import com.banking.transactionservice.dto.CreateTransactionRequest;
import com.banking.transactionservice.dto.TransactionResponse;
import com.banking.transactionservice.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> createTransfer(
            @Valid @RequestBody CreateTransactionRequest request){

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionService.createTransfer(request));
    }
}
