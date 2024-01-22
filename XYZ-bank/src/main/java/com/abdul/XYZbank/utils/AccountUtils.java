package com.abdul.XYZbank.utils;

import com.abdul.XYZbank.dto.CreditDebitRequest;

import java.time.Year;

public class AccountUtils {


    public static final String ACCOUNT_EXITS_CODE = "001";
    public static final String ACCOUNT_EXITS_MESSAGE = "This user already has an account created";

    public static final String ACCOUNT_CREATION_CODE = "002";
    public static final String ACCOUNT_CREATION_MESSAGE = "Account has been successfully created";
    public static final String ACCOUNT_NOT_EXIST_CODE = "003";
    public static final String ACCOUNT_NOT_EXIST_MESSAGE = "User with this account number does not exist, Please check the account number entered";
    public static final String ACCOUNT_EXIST_CODE = "004";
    public static final String ACCOUNT_EXIST_MESSAGE = "User with this account number exists";
    public static final String ACCOUNT_CREDIT_CODE = "005";
    public static final String ACCOUNT_CREDIT_MESSAGE = "Your account has been credited";
    public static final String INSUFFICIENT_BALANCE_CODE = "006";
    public static final String INSUFFICIENT_BALANCE_MESSAGE = "OOPS!!! Insufficient Balance";
    public static final String ACCOUNT_DEBIT_CODE = "007";
    public static final String ACCOUNT_DEBIT_MESSAGE = "Your account has been debited";
    public static final String ACCOUNT_TRANSFER_DEBIT_CODE = "008";
    public static final String ACCOUNT_TRANSFER_DEBIT_MESSAGE = "Transfer successfully (Debit)";
    public static final String ACCOUNT_TRANSFER_CREDIT_CODE = "009";
    public static final String ACCOUNT_TRANSFER_CREDIT_MESSAGE = "Transfer successfully (Credit)";


    //    generate account number
    public static String generateAccountNumber() {
        Year currentYear = Year.now();
        int min = 100000;
        int max = 999999;

//    generate random number between min and max
        int randomNum = (int) Math.floor(Math.random() * (max - min + 1) + min);

//    convert year and randomNum to String and concatenate them

        String year = String.valueOf(currentYear);
        String randomNumber = String.valueOf(randomNum);

        StringBuilder accountNumber = new StringBuilder();
        return accountNumber.append(year).append(randomNumber).toString();
    }
}
