package com.abdul.XYZbank.service.impl;

import com.abdul.XYZbank.dto.*;
import com.abdul.XYZbank.entity.User;
import com.abdul.XYZbank.repository.UserRepository;
import com.abdul.XYZbank.service.EmailService;
import com.abdul.XYZbank.service.TransactionService;
import com.abdul.XYZbank.service.UserService;
import com.abdul.XYZbank.twilio.SmsRequest;
import com.abdul.XYZbank.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final TransactionService transactionService;
    private final com.abdul.XYZbank.twilio.Service twilioService;

    @Override
    public BankResponse createAccount(UserRequest userRequest) {
//        check if user already has an account
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            BankResponse response = BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_EXITS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_EXITS_MESSAGE)
                    .accountInfo(null)
                    .build();

            return response;

        }

        User newUser = User.builder()
                // by using @builder you can save a user by calling the .builder, the variable in tha entity class and using the request dto class to get the variable value from the user
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .otherName(userRequest.getOtherName())
                .gender(userRequest.getGender())
                .address(userRequest.getAddress())
                .stateOfOrigin(userRequest.getStateOfOrigin())
                .accountNumber(AccountUtils.generateAccountNumber())
                .accountBalance(BigDecimal.ZERO)
                .email(userRequest.getEmail())
                .phoneNumber(userRequest.getPhoneNumber())
                .alternativePhoneNumber(userRequest.getAlternativePhoneNumber())
                .status("ACTIVE")
                .build();

        User saveUser = userRepository.save(newUser);

//        email alert
        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(saveUser.getEmail()).subject("ACCOUNT CREATION")
                .messageBody("Congratulations! Your account has been successfully created. \nYour account details are . . . " +
                        "\n" + "Account Name: " + saveUser.getFirstName() + " " + saveUser.getLastName() + " " + saveUser.getOtherName() + " " +
                        "\n" + "Account Number: " + saveUser.getAccountNumber())
                .build();
        emailService.sendEmailAlert(emailDetails);

//        phone number SMS
//        SmsRequest smsRequest = SmsRequest.builder()
//                .phoneNumber(saveUser.getPhoneNumber())
//                .message(("Dear " + saveUser.getFirstName() + "\n Congratulations! Your account has been successfully created. \nYour account details are . . . \n" +
//                        "Account Name: " + saveUser.getFirstName() + " " + saveUser.getLastName() + " " + saveUser.getOtherName() + " \n" +
//                        "Account Number: " + saveUser.getAccountNumber()))
//                .build();
//        twilioService.sendSms(smsRequest);

        BankResponse response = BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_CREATION_CODE)
                .responseMessage(AccountUtils.ACCOUNT_CREATION_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountName(saveUser.getFirstName() + " " + saveUser.getLastName() + " " + saveUser.getOtherName())
                        .accountNumber(saveUser.getAccountNumber())
                        .accountBalance(saveUser.getAccountBalance())
                        .build())
                .build();

        return response;
    }


//    balance and name enquiry, credit, debit, transfer

    @Override
    public BankResponse balanceEnquiry(EnquiryRequest enquiryRequest) {
//        check if the provided account number exists
        boolean isAccountExist = userRepository.existsByAccountNumber(enquiryRequest.getAccountNumber());
        if (!isAccountExist) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXIST_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE)
                    .accountInfo(null)
                    .build();
        }

        User foundUser = userRepository.findByAccountNumber(enquiryRequest.getAccountNumber());
        BankResponse response = BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_EXIST_CODE)
                .responseMessage(AccountUtils.ACCOUNT_EXIST_MESSAGE).
                accountInfo(AccountInfo.builder()
                        .accountName(foundUser.getFirstName() + " " + foundUser.getLastName() + " " + foundUser.getOtherName())
                        .accountNumber(enquiryRequest.getAccountNumber())
                        .accountBalance(foundUser.getAccountBalance()).build())
                .build();

        return response;
    }

    @Override
    public String nameEnquiry(EnquiryRequest enquiryRequest) {
        //        check if the provided account number exists
        boolean isAccountExist = userRepository.existsByAccountNumber(enquiryRequest.getAccountNumber());
        if (!isAccountExist) {
            return "Not Exist";
        }

        User foundUser = userRepository.findByAccountNumber(enquiryRequest.getAccountNumber());
        return foundUser.getFirstName() + " " + foundUser.getLastName() + " " + foundUser.getOtherName();
    }

    //    credit an account
    @Override
    public BankResponse creditAccount(CreditDebitRequest creditDebitRequest) {
        //        check if the provided account number exists
        boolean isAccountExist = userRepository.existsByAccountNumber(creditDebitRequest.getAccountNumber());
        if (!isAccountExist) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXIST_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE)
                    .accountInfo(null)
                    .build();
        }

        User userToCredit = userRepository.findByAccountNumber(creditDebitRequest.getAccountNumber());
        userToCredit.setAccountBalance(userToCredit.getAccountBalance().add(creditDebitRequest.getAmount()));
        userRepository.save(userToCredit);

//        Email alert
        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(userToCredit.getEmail())
                .subject("CREDIT ALERT")
                .messageBody("Credit! \nAcc:" + creditDebitRequest.getAccountNumber() + "\nAmt:₦" + creditDebitRequest.getAmount() +
                        "\nTID: 0003872539 \nDate:" + LocalDateTime.now() + "\nBal:₦" + userToCredit.getAccountBalance())
                .build();

        emailService.sendEmailAlert(emailDetails);

//        save transaction
        TransactionRequest transactionRequest = TransactionRequest.builder()
                .accountNumber(userToCredit.getAccountNumber())
                .transactionType("CREDIT")
                .amount(creditDebitRequest.getAmount())
                .accountName(userToCredit.getFirstName() + " " + userToCredit.getLastName() + " " + userToCredit.getOtherName())
                .date(LocalDate.now())
                .time(LocalTime.now())
                .build();
        transactionService.saveTransaction(transactionRequest);

        BankResponse response = BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_CREDIT_CODE)
                .responseMessage(AccountUtils.ACCOUNT_CREDIT_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountName(userToCredit.getFirstName() + " " + userToCredit.getLastName() + " " + userToCredit.getOtherName())
                        .accountBalance(userToCredit.getAccountBalance())
                        .accountNumber(creditDebitRequest.getAccountNumber())
                        .build())
                .build();

        return response;
    }

    //    debit an account
    @Override
    public BankResponse debitAccount(CreditDebitRequest creditDebitRequest) {
        //        check if the provided account number exists
        boolean isAccountExist = userRepository.existsByAccountNumber(creditDebitRequest.getAccountNumber());
        if (!isAccountExist) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXIST_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE)
                    .accountInfo(null)
                    .build();
        }

        User userToDebit = userRepository.findByAccountNumber(creditDebitRequest.getAccountNumber());

//        check if amount to withdraw is less than the total account
        BigDecimal availableBalance = new BigDecimal(userToDebit.getAccountBalance().toString());
        BigDecimal debitAmount = new BigDecimal(creditDebitRequest.getAmount().toString());
        if (availableBalance.compareTo(debitAmount) < 0) { //debit amount - total balance < 0, error
            return BankResponse.builder()
                    .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                    .responseMessage(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE)
                    .accountInfo(null)
                    .build();
        } else {
            userToDebit.setAccountBalance(userToDebit.getAccountBalance().subtract(creditDebitRequest.getAmount()));
            userRepository.save(userToDebit);

            //        Email alert
            EmailDetails emailDetails = EmailDetails.builder()
                    .recipient(userToDebit.getEmail())
                    .subject("DEBIT ALERT")
                    .messageBody("Debit! \nAcc:" + creditDebitRequest.getAccountNumber() + "\nAmt:₦" + creditDebitRequest.getAmount() +
                            "\nTID: 0003872539 \nDate:" + LocalDateTime.now() + "\nBal:₦" + userToDebit.getAccountBalance())
                    .build();

            emailService.sendEmailAlert(emailDetails);

//                    save transaction
            TransactionRequest transactionRequest = TransactionRequest.builder()
                    .accountNumber(userToDebit.getAccountNumber())
                    .transactionType("DEBIT")
                    .amount(creditDebitRequest.getAmount())
                    .accountName(userToDebit.getFirstName() + " " + userToDebit.getLastName() + " " + userToDebit.getOtherName())
                    .date(LocalDate.now())
                    .time(LocalTime.now())
                    .build();
            transactionService.saveTransaction(transactionRequest);

            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_DEBIT_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_DEBIT_MESSAGE)
                    .accountInfo(AccountInfo.builder()
                            .accountName(userToDebit.getFirstName() + " " + userToDebit.getLastName() + " " + userToDebit.getOtherName())
                            .accountNumber(userToDebit.getAccountNumber())
                            .accountBalance(userToDebit.getAccountBalance())
                            .build())
                    .build();
        }
    }

    //    Transfer
    @Override
    public BankResponse transfer(TransferRequest transferRequest) {

        BigDecimal dAmount = transferRequest.getAmount();
        if (dAmount.equals(-1)) {
            return BankResponse.builder()
                    .responseCode("010")
                    .responseMessage("Amount can not be a negative value")
                    .accountInfo(null)
                    .build();
        }

//        check if account to debit exists
        boolean isAccountToDebitExist = userRepository.existsByAccountNumber(transferRequest.getAccountFrom());
        boolean isAccountToCreditExist = userRepository.existsByAccountNumber(transferRequest.getAccountTo());
        if (!isAccountToDebitExist || !isAccountToCreditExist) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXIST_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE)
                    .accountInfo(null)
                    .build();
        }

        User transferDebit = userRepository.findByAccountNumber(transferRequest.getAccountFrom());

//        check if transfer amount is less than total amount
        BigDecimal availableBalance = new BigDecimal(transferDebit.getAccountBalance().toString());
        BigDecimal transferAmount = new BigDecimal(transferRequest.getAmount().toString());

        if (availableBalance.compareTo(transferAmount) < 0) { //transfer amount - total balance < 0, error
            return BankResponse.builder()
                    .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                    .responseMessage(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE)
                    .accountInfo(null)
                    .build();
        } else {
            transferDebit.setAccountBalance(transferDebit.getAccountBalance().subtract(transferRequest.getAmount()));
            userRepository.save(transferDebit);

            EmailDetails emailDetails = EmailDetails.builder()
                    .recipient(transferDebit.getEmail())
                    .subject("DEBIT ALERT")
                    .messageBody("Debit! \nAcc:" + transferRequest.getAccountFrom() + "\nAmt:₦" + transferRequest.getAmount() +
                            "\nTID: 0003872539 \nTRF TO " + transferRequest.getAccountTo() + "\nDate:" + LocalDateTime.now() + "\nBal:₦" + transferDebit.getAccountBalance())
                    .build();

            emailService.sendEmailAlert(emailDetails);

//                   save transaction
            TransactionRequest transactionRequestDebit = TransactionRequest.builder()
                    .accountNumber(transferDebit.getAccountNumber())
                    .transactionType("DEBIT TRANSFER")
                    .amount(transferRequest.getAmount())
                    .accountName(transferDebit.getFirstName() + " " + transferDebit.getLastName() + " " + transferDebit.getOtherName())
                    .date(LocalDate.now())
                    .time(LocalTime.now())
                    .build();
            transactionService.saveTransaction(transactionRequestDebit);


            //       check if account to credit exists
            if (!isAccountToCreditExist || !isAccountToDebitExist) {
                return BankResponse.builder()
                        .responseCode(AccountUtils.ACCOUNT_NOT_EXIST_CODE)
                        .responseMessage(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE)
                        .accountInfo(null)
                        .build();
            }

            User transferCredit = userRepository.findByAccountNumber(transferRequest.getAccountTo());
            transferCredit.setAccountBalance(transferCredit.getAccountBalance().add(transferRequest.getAmount()));
            userRepository.save(transferCredit);

            EmailDetails emailDetail = EmailDetails.builder()
                    .recipient(transferCredit.getEmail())
                    .subject("CREDIT ALERT")
                    .messageBody("Credit! \nAcc:" + transferRequest.getAccountFrom() + "\nAmt:₦" + transferRequest.getAmount() +
                            "\nTID: 0003872539 \nTRF FRM " + transferRequest.getAccountFrom() + "\nDate:" + LocalDateTime.now() + "\nBal:₦" + transferCredit.getAccountBalance())
                    .build();

            emailService.sendEmailAlert(emailDetail);

//                   save transaction
            TransactionRequest transactionRequestCredit = TransactionRequest.builder()
                    .accountNumber(transferCredit.getAccountNumber())
                    .transactionType("CREDIT TRANSFER")
                    .amount(transferRequest.getAmount())
                    .accountName(transferCredit.getFirstName() + " " + transferCredit.getLastName() + " " + transferCredit.getOtherName())
                    .date(LocalDate.now())
                    .time(LocalTime.now())
                    .build();
            transactionService.saveTransaction(transactionRequestCredit);

            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_TRANSFER_CREDIT_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_TRANSFER_CREDIT_MESSAGE)
                    .accountInfo(AccountInfo.builder()
                            .accountName(transferCredit.getFirstName() + " " + transferCredit.getLastName() + " " + transferCredit.getOtherName())
                            .accountNumber(transferRequest.getAccountTo())
                            .accountBalance(transferCredit.getAccountBalance())
                            .build())
                    .build();
        }


    }

}
