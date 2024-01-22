package com.abdul.XYZbank.service;

import com.abdul.XYZbank.dto.TransactionRequest;
import com.abdul.XYZbank.entity.Transaction;

public interface TransactionService {

    void saveTransaction(TransactionRequest transactionRequest);
}
