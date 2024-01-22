package com.abdul.XYZbank.service.impl;

import com.abdul.XYZbank.dto.TransactionRequest;
import com.abdul.XYZbank.entity.Transaction;
import com.abdul.XYZbank.repository.TransactionRepository;
import com.abdul.XYZbank.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    public void saveTransaction(TransactionRequest transactionRequest) {
        Transaction transaction = Transaction.builder()
                .transactionType(transactionRequest.getTransactionType())
                .accountNumber(transactionRequest.getAccountNumber())
                .amount(transactionRequest.getAmount())
                .accountName(transactionRequest.getAccountName())
                .date(transactionRequest.getDate())
                .time(transactionRequest.getTime())
                .status("SUCCESS")
                .build();

        transactionRepository.save(transaction);

        System.out.println("Transaction Saved");
    }
}
