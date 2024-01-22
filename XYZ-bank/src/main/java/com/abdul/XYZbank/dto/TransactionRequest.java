package com.abdul.XYZbank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionRequest {

    private String transactionType;
    private BigDecimal amount;
    private String accountNumber;
    private String accountName;
    private LocalDate date;
    private LocalTime time;
    private String status;
}
