package com.mitrais.training.atmsimulation.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Integer transactionId;

    @Column(name = "transaction_date", nullable = false, unique = true)
    private Date       transactionDate;

    @Column(name = "account_number", nullable = false, length = 6)
    private String     accountNumber;

    @Column(name = "destination_account", nullable = true, length = 6)
    private String     destinationAccount;

    @Column(name = "type", nullable = false, length = 3)
    private String     type;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getDestinationAccount() {
        return destinationAccount;
    }

    public void setDestinationAccount(String destinationAccount) {
        this.destinationAccount = destinationAccount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Transaction inverseTransaction() {
        Transaction transaction = new Transaction();
        transaction.setTransactionDate(getTransactionDate());
        transaction.setAccountNumber(getDestinationAccount());
        transaction.setDestinationAccount(getAccountNumber());
        transaction.setType(getType());
        transaction.setAmount(getAmount().negate());

        return transaction;
    }
}