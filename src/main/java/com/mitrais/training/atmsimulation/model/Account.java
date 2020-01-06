package com.mitrais.training.atmsimulation.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class Account {

    @Id
    @Column(name = "account_number", nullable = false, unique = true, length = 6)
    private String     accountNumber;

    @Column(name = "name", nullable = false, length = 50)
    private String     name;

    @Column(name = "pin", nullable = false, length = 6)
    private String     pin;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}