package com.mcmah113.mcmah113expensesiq;

class Transaction {
    private int accountFromId;
    private int accountToId;
    private String type;
    private double amount;
    private String locale;
    private String symbol;
    private String date;
    private String note;

    Transaction(int accountFromId, int accountToId, String type,String locale, String symbol,
                double amount, String date, String note) {

        this.accountFromId = accountFromId;
        this.accountToId = accountToId;
        this.type = type;
        this.locale = locale;
        this.symbol = symbol;
        this.amount = amount;
        this.date = date;
        this.note = note;
    }

    int getAccountFromId() {
        return accountFromId;
    }

    int getAccountToId() {
        return accountToId;
    }

    String getType() {
        return type;
    }

    String getLocale() {
        return locale;
    }

    String getSymbol() {
        return symbol;
    }

    double getAmount() {
        return amount;
    }

    String getDate() {
        return date;
    }

    String getNote() {
        return note;
    }

    //no editing of the transactions allowed, read only
}
