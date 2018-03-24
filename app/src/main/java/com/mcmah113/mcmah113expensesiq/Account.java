package com.mcmah113.mcmah113expensesiq;

import android.graphics.Bitmap;

class Account {
    private int id;
    private String name;
    private String type;
    private String locale;
    private String symbol;
    private double initialBalance;
    private double currentBalance;
    private String description;
    private boolean hiddenFlag;

    private Bitmap imageIcon;

    Account(int id, String name, String type, String locale,
            String symbol, double initialBalance, double currentBalance,
            String description, boolean hiddenFlag) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.locale = locale;
        this.symbol = symbol;
        this.initialBalance = initialBalance;
        this.currentBalance = currentBalance;
        this.description = description;
        this.imageIcon = setImageIcon();
        this.hiddenFlag = hiddenFlag;
    }

    void setId(int id) {
        this.id = id;
    }

    void setName(String name) {
        this.name = name;
    }

    void setType(String type) {
        this.type = type;
    }

    void setLocale(String locale) {
        this.locale = locale;
    }

    void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    void setInitialBalance(double initialBalance) {
        this.initialBalance = initialBalance;
    }

    void setCurrentBalance(double currentBalance) {
        this.currentBalance = currentBalance;
    }

    void setDescription(String description) {
        this.description = description;
    }

    void setHiddenFlag(boolean hiddenFlag) {
        this.hiddenFlag = hiddenFlag;
    }

    int getId() {
        return id;
    }

    String getName() {
        return name;
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

    double getInitialBalance() {
        return initialBalance;
    }

    double getCurrentBalance() {
        return currentBalance;
    }

    String getDescription() {
        return description;
    }

    boolean getHiddenFlag() {
        return hiddenFlag;
    }

    Bitmap getIcon() {
        return imageIcon;
    }

    private Bitmap setImageIcon() {
        switch (type) {
            case "Cash":
                return null;
            case "Bank":
                return null;
            default:
                return null;
        }
    }
}