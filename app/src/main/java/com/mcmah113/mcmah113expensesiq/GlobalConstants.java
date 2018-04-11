package com.mcmah113.mcmah113expensesiq;

import android.graphics.Color;

import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;

class GlobalConstants {
    private static final String[] monthsArray = new String[]{
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December"
    };

    static String[] getMonthsArray() {
        return monthsArray;
    }

    private static final int colorPalette[] = {
        Color.rgb(193, 37, 82),   Color.rgb(255, 102, 0),   Color.rgb(245, 199, 0),
        Color.rgb(106, 150, 31),  Color.rgb(179, 100, 53),
        Color.rgb(64, 89, 128),   Color.rgb(149, 165, 124), Color.rgb(217, 184, 162),
        Color.rgb(191, 134, 134), Color.rgb(179, 48, 80),
        Color.rgb(192, 255, 140), Color.rgb(255, 247, 140), Color.rgb(255, 208, 140),
        Color.rgb(140, 234, 255), Color.rgb(255, 140, 157),
        Color.rgb(207, 248, 246), Color.rgb(148, 212, 212), Color.rgb(136, 180, 187),
        Color.rgb(118, 174, 175), Color.rgb(42, 109, 130),
        Color.rgb(217, 80, 138),  Color.rgb(254, 149, 7),   Color.rgb(254, 247, 120),
        Color.rgb(106, 167, 134), Color.rgb(53, 194, 209)
    };

    static int[] getColorPalette() {
        return colorPalette;
    }

    private static final String transactionTypeList[] = {
        "Accommodation",
        "Automobile",
        "Child Support",
        "Credit Cards",
        "Donation",
        "Entertainment",
        "Food",
        "Given Gifts",
        "Received Gifts",
        "Groceries",
        "Household",
        "Investment",
        "Medicare",
        "Personal Care",
        "Pets",
        "Self Improvement",
        "Shopping",
        "Sports and Recreation",
        "Tax",
        "Transportation",
        "Utilities",
        "Vacation",
        "Other"
    };

    private static final String localeArray[] = {
        "" + Currency.getInstance(Locale.US),
        "" + Currency.getInstance(Locale.CANADA),
        "" + Currency.getInstance(Locale.JAPAN),
        "" + Currency.getInstance(Locale.UK),
        "" + Currency.getInstance(Locale.FRANCE),
        "" + Currency.getInstance(Locale.CHINA)
    };

    static String[] getLocaleArray() {
        return localeArray;
    }

    private static HashMap<String, String> hashMapExchangeRates;
    private static boolean currencyExchangeFallBack = false;

    static void setHashMapExchangeRates(HashMap<String, String> hashMap) {
        hashMapExchangeRates = hashMap;
    }

    static HashMap<String, String> getHashMapExchangeRates() {
        return hashMapExchangeRates;
    }

    static boolean getCurrencyExchangeFallBack() {
        return currencyExchangeFallBack;
    }

    static void setCurrencyExchangeFallBack(boolean flag) {
        currencyExchangeFallBack = flag;
    }

    private static final String currencyArray[] = {
        Locale.US.getDisplayCountry() + " (" + Currency.getInstance(Locale.US) + ")",
        Locale.CANADA.getDisplayCountry() + " (" + Currency.getInstance(Locale.CANADA) + ")",
        Locale.JAPAN.getDisplayCountry() + " (" + Currency.getInstance(Locale.JAPAN) + ")",
        Locale.UK.getDisplayCountry() + " (" + Currency.getInstance(Locale.UK) + ")",
        Locale.FRANCE.getDisplayCountry() + " (" + Currency.getInstance(Locale.FRANCE) + ")",
        Locale.CHINA.getDisplayCountry() + " (" + Currency.getInstance(Locale.CHINA) + ")"
    };

    private static final String languageArray[] = {
        Locale.ENGLISH.getDisplayLanguage() + " (" + Locale.ENGLISH.getLanguage() + ")",
        Locale.JAPANESE.getDisplayLanguage() + " (" + Locale.JAPANESE.getLanguage() + ")",
        Locale.FRENCH.getDisplayLanguage() + " (" + Locale.FRENCH.getLanguage() + ")",
        Locale.CHINESE.getDisplayLanguage() + " (" + Locale.CHINESE.getLanguage() + ")"
    };

    private static final String typesArray[] = {"Bank", "Cash", "Credit"};

    private static final String expenseReports[][] = {
        {"Expense by Category", "View all expenses sorted by category type"},
        {"Daily Expense", "View the last seven days breakdown of your expenses by specific accounts"},
        {"Monthly Expense", "View a month by month breakdown of your expenses by specific accounts"}
    };

    private static final String incomeReports[][] = {
        {"Income by Category", "View all incomes sorted by category type"},
        {"Daily Income", "View the last seven days breakdown of your income by specific accounts"},
        {"Monthly Expense", "View a month by month breakdown of your income by specific accounts"}
    };

    private static final String cashFlowReports[][] = {
        {"Income Vs Expense", "Compare your total expense and income"}
    };

    private static final String balanceReports[][] = {
        {"Daily Balance", "Compare your income and expenses on a daily basis"}
    };

    private static final String transactionPeriods[] = {
        "All Time",
        "This Month",
        "This Week",
        "Today"
    };

    static String[] getTransactionTypeList() {
        return transactionTypeList;
    }

    static String[] getTypesArray() {
        return typesArray;
    }

    static String[] getLanguageArray() {
        return languageArray;
    }

    static String[] getCurrencyArray() {
        return currencyArray;
    }

    static String[][] getExpenseReports() {
        return expenseReports;
    }

    static String[][] getIncomeReports() {
        return incomeReports;
    }

    static String[][] getCashFlowReports() {
        return cashFlowReports;
    }

    static String[][] getBalanceReports() {
        return balanceReports;
    }

    static String[] getTransactionPeriods() {
        return transactionPeriods;
    }
}