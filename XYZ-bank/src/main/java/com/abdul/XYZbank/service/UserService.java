package com.abdul.XYZbank.service;

import com.abdul.XYZbank.dto.*;

public interface UserService {

   BankResponse createAccount(UserRequest userRequest);

   BankResponse balanceEnquiry(EnquiryRequest enquiryRequest);

   String nameEnquiry(EnquiryRequest enquiryRequest);

   BankResponse creditAccount(CreditDebitRequest creditDebitRequest);

   BankResponse debitAccount(CreditDebitRequest creditDebitRequest);

   BankResponse transfer(TransferRequest transferRequest);
}
