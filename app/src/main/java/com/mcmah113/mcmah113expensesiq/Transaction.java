package com.mcmah113.mcmah113expensesiq;

public class Transaction {
    private int transactionId;
    private int userId;
    private int accountFromId;
    private int accountToId;
    private String type;
    private double amount;
    private String symbol;
    private String date;

    Transaction(int transactionId, int userId, int accountFromId,
                int accountToId, String type, double amount,
                String symbol, String date) {

        this.transactionId = transactionId;
        this.userId = userId;
        this.accountFromId = accountFromId;
        this.accountToId = accountToId;
        this.type = type;
        this.amount = amount;
        this.symbol = symbol;
        this.date = date;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public int getUserId() {
        return userId;
    }

    public int getAccountFromId() {
        return accountFromId;
    }

    public int getAccountToId() {
        return accountToId;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDate() {
        return date;
    }

    //no setters as the transactions should be read only
    //no editing allowed

    //final Date currentTime = Calendar.getInstance().getTime();
    //refreshDate = "Refreshed on " + new SimpleDateFormat("yyyy-MM-dd").format(currentTime);
}
