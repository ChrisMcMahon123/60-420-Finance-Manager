package com.mcmah113.mcmah113expensesiq;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;

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
        this.hiddenFlag = hiddenFlag;
        this.imageIcon = null;
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

    //will never be used as the database never stores the currency symbol, only the locale
    //account is only ever updated before a database call, where symbol is never used
    //then on returning, a new database call is issued to get the updated info
    //where the database will set the symbol based on the locale
    /*void setSymbol(String symbol) {
        this.symbol = symbol;
    }*/

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

    //must be called from outside, in order to get the context
    void setImageIcon(Context context) {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream;

        try {
            switch (type) {
                case "Cash":
                    inputStream = assetManager.open("type_cash.png");
                    break;
                case "Bank":
                    inputStream = assetManager.open("type_bank.png");
                    break;
                case "Credit":
                    inputStream = assetManager.open("type_credit.png");
                    break;
                case "Add":
                    inputStream = assetManager.open("new_account.png");
                    break;
                default:
                    inputStream = assetManager.open("default_image.png");
            }

            imageIcon = BitmapFactory.decodeStream(inputStream);
        }
        catch(IOException e) {
            e.printStackTrace();
            imageIcon = BitmapFactory.decodeResource(context.getResources(),R.color.colorGreen);
        }
    }
}