package com.mitrais.training.atmsimulation.util;

public enum TransactionType {
    TRANSFER("TRF"), WITHDRAW("WDR");

    private String code = "";

    private TransactionType(String code) {
        this.code = code;
    }

    public static TransactionType fromCode(String code) {
        switch(code) {
        case "TRF":
            return TRANSFER;
        case "WDR":
            return WITHDRAW;
        default:
            return null;
        }
    }

    public String toString() {
        return code;
    }
}
