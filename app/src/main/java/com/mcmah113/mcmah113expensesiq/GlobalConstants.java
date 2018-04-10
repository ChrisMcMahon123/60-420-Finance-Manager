package com.mcmah113.mcmah113expensesiq;

import java.util.Currency;
import java.util.Locale;

class GlobalConstants {
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