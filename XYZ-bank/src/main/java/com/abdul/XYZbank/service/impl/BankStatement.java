package com.abdul.XYZbank.service.impl;

import com.abdul.XYZbank.dto.EmailDetails;
import com.abdul.XYZbank.entity.Transaction;
import com.abdul.XYZbank.entity.User;
import com.abdul.XYZbank.repository.TransactionRepository;
import com.abdul.XYZbank.repository.UserRepository;
import com.abdul.XYZbank.service.EmailService;
import com.itextpdf.awt.geom.Rectangle;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankStatement {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    private static final String FILE = "C:\\Users\\PC\\Documents\\xyz-bank\\MyStatement.pdf";

    public List<Transaction> generateStatement(String accountNumber, String startDate, String endDate) throws FileNotFoundException, DocumentException {

        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
        LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);

        List<Transaction> transactionList = transactionRepository.findAll()
                .stream().filter(transaction -> transaction.getAccountNumber().equals(accountNumber))
                .filter(transaction -> transaction.getDate().isEqual(start))
                .filter(transaction -> transaction.getDate().isEqual(end))
                .toList();

        User user = userRepository.findByAccountNumber(accountNumber);
        String customerName = user.getFirstName() + " " + user.getLastName() + " " + user.getOtherName();
        String customerAddress = user.getAddress();

//        PDF CONTENT START

        com.itextpdf.text.Rectangle statementSize = new com.itextpdf.text.Rectangle(PageSize.A4);
        Document document = new Document(statementSize);
        log.info("Setting Size of document");

        OutputStream outputStream = new FileOutputStream(FILE);
        PdfWriter.getInstance(document, outputStream);

        document.open();

//        add content
//        table 1
        PdfPTable bankInfoTable = new PdfPTable(1);
        PdfPCell bankName = new PdfPCell(new Phrase ("XYZ BANK",new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE)));
        bankName.setBorder(0);
        bankName.setBackgroundColor(BaseColor.BLUE);
        bankName.setPadding(10f);

        PdfPCell bankAddress = new PdfPCell(new Phrase("No 5, GRA Road, Gombe state, Nigeria", new Font(Font.FontFamily.TIMES_ROMAN, 10)));
        bankAddress.setBorder(0);

//        table 2
        PdfPTable statementInfo = new PdfPTable(2);
        PdfPCell customerInfo = new PdfPCell(new Phrase("Start Date: " + startDate, new Font(Font.FontFamily.TIMES_ROMAN, 10)));
        customerInfo.setBorder(0);

        PdfPCell statement = new PdfPCell(new Phrase("STATEMENT OF ACCOUNT", new Font(Font.FontFamily.TIMES_ROMAN, 10)));
        statement.setBorder(0);

        PdfPCell stopDate = new PdfPCell(new Phrase("End Date: " + endDate, new Font(Font.FontFamily.TIMES_ROMAN, 10)));
        stopDate.setBorder(0);

        PdfPCell customerInfoName = new PdfPCell(new Phrase("Customer Name: " + customerName, new Font(Font.FontFamily.TIMES_ROMAN, 10)));
        customerInfoName.setBorder(0);

        PdfPCell space = new PdfPCell();
        space.setBorder(0);

        PdfPCell customerInfoAddress = new PdfPCell(new Phrase("Customer Address: " + customerAddress, new Font(Font.FontFamily.TIMES_ROMAN, 10)));
        customerInfoAddress.setBorder(0);

//        table 3
//        Transactions Table
        PdfPTable transactionTable = new PdfPTable(4);
        PdfPCell date = new PdfPCell(new Phrase("DATE", new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD, BaseColor.WHITE)));
        date.setBorder(0);
        date.setBackgroundColor(BaseColor.BLUE);

        PdfPCell transactionType = new PdfPCell(new Phrase("TRANSACTION TYPE", new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD, BaseColor.WHITE)));
        transactionType.setBorder(0);
        transactionType.setBackgroundColor(BaseColor.BLUE);

        PdfPCell amount = new PdfPCell(new Phrase("TRANSACTION AMOUNT", new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD, BaseColor.WHITE)));
        amount.setBorder(0);
        amount.setBackgroundColor(BaseColor.BLUE);

        PdfPCell status = new PdfPCell(new Phrase("TRANSACTION STATUS", new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD, BaseColor.WHITE)));
        status.setBorder(0);
        status.setBackgroundColor(BaseColor.BLUE);


//        table 1
        bankInfoTable.addCell(bankName);
        bankInfoTable.addCell(bankAddress);

//        table 2
        statementInfo.addCell(customerInfo);
        statementInfo.addCell(statement);
        statementInfo.addCell(stopDate);
        statementInfo.addCell(customerInfoName);
        statementInfo.addCell(space);
        statementInfo.addCell(customerInfoAddress);

//        table 3
        transactionTable.addCell(date);
        transactionTable.addCell(transactionType);
        transactionTable.addCell(amount);
        transactionTable.addCell(status);

//        fetch the record and complete the table
//        filter every single transaction for that account number and dates
        transactionList.forEach(transaction -> {
            transactionTable.addCell(new Phrase(startDate));
            transactionTable.addCell(new Phrase(transaction.getTransactionType()));
            transactionTable.addCell(new Phrase(transaction.getAmount().toString()));
            transactionTable.addCell(new Phrase(transaction.getStatus()));
        });


//        ADD THE TABLES TO DOCUMENT
        document.add(bankInfoTable);
        document.add(statementInfo);
        document.add(transactionTable);

        document.close();

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(user.getEmail())
                .subject("STATEMENT OF ACCOUNT")
                .messageBody("Kindly find your requested account statement attached")
                .attachment(FILE)
                .build();

        emailService.sendEmailWithAttachment(emailDetails);

//        PDF CONTENT END

        return transactionList;
    }

}
