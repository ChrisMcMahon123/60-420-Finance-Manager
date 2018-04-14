package com.mcmah113.mcmah113expensesiq;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.HashMap;
import java.util.StringTokenizer;

public class DatabaseHelper extends SQLiteOpenHelper {
    //database information
    private static final String DATABASE_NAME = "ExpenseIq.db";
    private static final int DATABASE_VERSION = 10;

    //table names
    private static final String TABLE_USERS = "Users";
    private static final String TABLE_ACCOUNTS = "Accounts";
    private static final String TABLE_TRANSACTIONS = "Transactions";
    private static final String TABLE_EXCHANGE_RATES = "ExchangeRates";

    //tables columns
    private static final String COLUMNS_USERS[] = {
        "user_id",
        "username",
        "password",
        "language",
        "locale",
        "overview_customize_flag_accounts",
        "overview_customize_flag_transactions",
        "overview_customize_flag_report_1",
        "overview_customize_flag_report_2"
    };

    private static final String COLUMNS_ACCOUNTS[] = {
        "account_id",
        "user_id",
        "name",
        "type",
        "locale",
        "starting_balance",
        "current_balance",
        "description",
        "hidden_flag"
    };

    private static final String COLUMNS_TRANSACTIONS[] = {
        "transaction_id",
        "user_id",
        "account_from_id",
        "account_to_id",
        "transaction_type",
        "locale",
        "amount",
        "date",
        "note",
        "payee"
    };

    private static final String COLUMNS_EXCHANGE_RATES[] = {
        "exchange_id",
        "locale",
        "exchange_rate",
        "refresh_date",
        "default_flag"
    };

    //determines either to query for hidden accounts
    private static boolean displayHiddenFlag = false;

    DatabaseHelper(Context context) {
        //only care about the database name being accessed and the context
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @SuppressLint("SimpleDateFormat")
    public void onCreate(SQLiteDatabase database) {
        //building the sql queries that will create the tables
        String createUsersTable =
            "CREATE TABLE " + TABLE_USERS + " (\n" +
                COLUMNS_USERS[0] + " INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                COLUMNS_USERS[1] + " TEXT,\n" +
                COLUMNS_USERS[2] + " TEXT, \n" +
                COLUMNS_USERS[3] + " TEXT, \n" +
                COLUMNS_USERS[4] + " TEXT, \n" +
                COLUMNS_USERS[5] + " INTEGER, \n" +
                COLUMNS_USERS[6] + " INTEGER, \n" +
                COLUMNS_USERS[7] + " INTEGER, \n" +
                COLUMNS_USERS[8] + " INTEGER \n" +
            ");";

        String createAccountsTable =
            "CREATE TABLE " + TABLE_ACCOUNTS + " (\n" +
                COLUMNS_ACCOUNTS[0] + " INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                COLUMNS_ACCOUNTS[1] + " INTEGER,\n" +
                COLUMNS_ACCOUNTS[2] + " TEXT,\n" +
                COLUMNS_ACCOUNTS[3] + " TEXT,\n" +
                COLUMNS_ACCOUNTS[4] + " TEXT,\n" +
                COLUMNS_ACCOUNTS[5] + " REAL,\n" +
                COLUMNS_ACCOUNTS[6] + " REAL,\n" +
                COLUMNS_ACCOUNTS[7] + " TEXT, \n" +
                COLUMNS_ACCOUNTS[8] + " INTEGER, \n" +
                "FOREIGN KEY(" + COLUMNS_ACCOUNTS[1] + ") " +
                "REFERENCES " + TABLE_USERS + "(" + COLUMNS_USERS[0] + ")\n" +
            ");";

        String createTransactionsTable =
            "CREATE TABLE " + TABLE_TRANSACTIONS + " (\n" +
                COLUMNS_TRANSACTIONS[0] + " INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                COLUMNS_TRANSACTIONS[1] + " INTEGER,\n" +
                COLUMNS_TRANSACTIONS[2] + " INTEGER,\n" +
                COLUMNS_TRANSACTIONS[3] + " INTEGER,\n" +
                COLUMNS_TRANSACTIONS[4] + " TEXT,\n" +
                COLUMNS_TRANSACTIONS[5] + " TEXT,\n" +
                COLUMNS_TRANSACTIONS[6] + " REAL,\n" +
                COLUMNS_TRANSACTIONS[7] + " TEXT, \n" +
                COLUMNS_TRANSACTIONS[8] + " TEXT, \n" +
                COLUMNS_TRANSACTIONS[9] + " TEXT, \n" +
                "FOREIGN KEY(" + COLUMNS_TRANSACTIONS[1] + ") " +
                "REFERENCES " + TABLE_USERS + "(" + COLUMNS_USERS[0] + ")\n" +
            ");";

        String createExchangeRatesTable =
            "CREATE TABLE " + TABLE_EXCHANGE_RATES + " (\n" +
                COLUMNS_EXCHANGE_RATES[0] + " INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                COLUMNS_EXCHANGE_RATES[1] + " TEXT,\n" +
                COLUMNS_EXCHANGE_RATES[2] + " REAL,\n" +
                COLUMNS_EXCHANGE_RATES[3] + " STRING,\n" +
                COLUMNS_EXCHANGE_RATES[4] + " INTEGER\n" +
            ");";

        try {
            database.execSQL(createUsersTable);
            database.execSQL(createAccountsTable);
            database.execSQL(createTransactionsTable);
            database.execSQL(createExchangeRatesTable);

            Log.d("Users SQL Query", createUsersTable);
            Log.d("Accounts SQL Query", createAccountsTable);
            Log.d("Transactions SQL Query", createTransactionsTable);
            Log.d("ExchangeRates SQL Query", createExchangeRatesTable);
        }
        catch(Exception exception){
            //failed to create the tables
            Log.d("onCreate()", "Failed to create the database tables");
            Log.d("Users SQL Query", createUsersTable);
            Log.d("Accounts SQL Query", createAccountsTable);
            Log.d("Transactions SQL Query", createTransactionsTable);
            Log.d("ExchangeRates SQL Query", createExchangeRatesTable);
            exception.printStackTrace();
        }
    }

    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        //delete the tables when the database gets updated
        String dropTableUsers = "DROP TABLE IF EXISTS "+ TABLE_USERS;
        String dropTableAccounts = "DROP TABLE IF EXISTS " + TABLE_ACCOUNTS;
        String dropTableTransactions = "DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS;
        String dropTableExchangeRates = "DROP TABLE IF EXISTS " + TABLE_EXCHANGE_RATES;
        database.execSQL(dropTableUsers);
        database.execSQL(dropTableAccounts);
        database.execSQL(dropTableTransactions);
        database.execSQL(dropTableExchangeRates);

        this.onCreate(database);

        //populate the default database with a base exchange rate
        //will only be used if the app never gets internet connection to make a
        //successfull Fixer.io API call
        final HashMap<String, String> hashMapDefault = new HashMap<>();

        for(String locale : GlobalConstants.getLocaleArray()) {
            hashMapDefault.put(locale, "1.00");
        }

        setBackupExchangeRates(database, hashMapDefault, 1);
    }

    public void onDowngrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        onUpgrade(database, oldVersion, newVersion);
    }

    //need to mirror the structure of the HashMap that gets returned in Fixer.io API call
    HashMap<String, String> getBackupExchangeRates() {
        final HashMap<String, String> hashMapExchangeRates = new HashMap<>();

        final SQLiteDatabase database = this.getReadableDatabase();

        final String COLUMNS_SELECTED[] = {COLUMNS_EXCHANGE_RATES[1],COLUMNS_EXCHANGE_RATES[2],COLUMNS_EXCHANGE_RATES[3], COLUMNS_EXCHANGE_RATES[4]};

        final Cursor cursor = database.query(TABLE_EXCHANGE_RATES, COLUMNS_SELECTED, null, null, null, null, null, null);

        String locale;
        String amount;
        String date = "";
        int default_flag = 1;

        cursor.moveToFirst();

        for(int i = 0; i < cursor.getCount(); i ++) {
            locale = cursor.getString(cursor.getColumnIndex(COLUMNS_EXCHANGE_RATES[1]));
            amount = cursor.getString(cursor.getColumnIndex(COLUMNS_EXCHANGE_RATES[2]));
            date = cursor.getString(cursor.getColumnIndex(COLUMNS_EXCHANGE_RATES[3]));
            default_flag = cursor.getInt(cursor.getColumnIndex(COLUMNS_EXCHANGE_RATES[4]));
            Log.d("getDatabaseRates", locale + "|" + amount + "|" + date + "|" + default_flag);

            hashMapExchangeRates.put(locale, amount);

            cursor.moveToNext();
        }

        hashMapExchangeRates.put("Date", date);

        GlobalConstants.setCurrencyExchangeFallBack(default_flag == 1);

        cursor.close();
        database.close();

        return hashMapExchangeRates;
    }

    //this is called only when there's a database upgrade / downgrade
    //only used internally to set the default exchange rates to 1
    //don't close the database as that will lock out all requests in the future
    private void setBackupExchangeRates(SQLiteDatabase database, HashMap<String, String> hashMapExchangeRates, int default_flag) {
        //wipe the table and then refresh all values
        database.delete(TABLE_EXCHANGE_RATES,"", null);

        final Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") final String date = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());

        ContentValues VALUES;
        //the Fixer.io API call will be sending a "Date" key, don't insert it...
        //only inserting locale and their values, will calculate date separately
        for(String locale : GlobalConstants.getLocaleArray()) {
            if(hashMapExchangeRates.containsKey(locale)) {
                Log.d("Backup Exchange Rate", locale + " | " + hashMapExchangeRates.get(locale) + " | " + date + " | " + default_flag);
                VALUES = new ContentValues();
                VALUES.put(COLUMNS_EXCHANGE_RATES[1], locale);
                VALUES.put(COLUMNS_EXCHANGE_RATES[2], hashMapExchangeRates.get(locale));
                VALUES.put(COLUMNS_EXCHANGE_RATES[3], date);
                VALUES.put(COLUMNS_EXCHANGE_RATES[4], default_flag);

                database.insert(TABLE_EXCHANGE_RATES, null, VALUES);
            }
        }

        GlobalConstants.setCurrencyExchangeFallBack(default_flag == 1);
    }

    void setBackupExchangeRates(HashMap<String, String> hashMapExchangeRates, int default_flag) {
        final SQLiteDatabase database = this.getWritableDatabase();

        //wipe the table and then refresh all values
        database.delete(TABLE_EXCHANGE_RATES,"", null);

        final Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") final String date = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());

        ContentValues VALUES;
        //the Fixer.io API call will be sending a "Date" key, don't insert it...
        //only inserting locale and their values, will calculate date separately
        for(String locale : GlobalConstants.getLocaleArray()) {
            if(hashMapExchangeRates.containsKey(locale)) {
                Log.d("Backup Exchange Rate", locale + " | " + hashMapExchangeRates.get(locale) + " | " + date + " | " + default_flag);
                VALUES = new ContentValues();
                VALUES.put(COLUMNS_EXCHANGE_RATES[1], locale);
                VALUES.put(COLUMNS_EXCHANGE_RATES[2], hashMapExchangeRates.get(locale));
                VALUES.put(COLUMNS_EXCHANGE_RATES[3], date);
                VALUES.put(COLUMNS_EXCHANGE_RATES[4], default_flag);

                database.insert(TABLE_EXCHANGE_RATES, null, VALUES);
            }
        }

        GlobalConstants.setCurrencyExchangeFallBack(default_flag == 1);

        database.close();
    }

    int userLogin(String username, String password) {
        try {
            final SQLiteDatabase database = this.getReadableDatabase();

            final String ARGUMENTS[] = {username, password};
            final String COLUMNS_SELECTED[] = {COLUMNS_USERS[0]};

            final Cursor cursor = database.query(TABLE_USERS, COLUMNS_SELECTED, COLUMNS_USERS[1] + " = ? AND " + COLUMNS_USERS[2] + " = ?", ARGUMENTS, null, null, null, "1");

            int userId;

            if(cursor.getCount() != 0) {
                //an account exists with those credentials, log in successfull
                cursor.moveToFirst();
                userId = cursor.getInt(cursor.getColumnIndex(COLUMNS_SELECTED[0]));
            }
            else {
                //no account exists with those credentials, login failed
                userId = -1;
            }

            cursor.close();
            database.close();

            return userId;
        }
        catch (Exception exception) {
            Log.d("userLogin()","Attempt to log in failed");
            exception.printStackTrace();
            return -1;
        }
    }

    //the user gets two default accounts when signing up (Bank, Cash)
    boolean userSignUp(String username, String password,
                       String locale, String language,
                       double bankBalance, double cashBalance) {
        try {
            if(!uniqueUsername(username)) {
                //the username is already taken
                return false;
            }
            else {
                //valid new username, make user account
                SQLiteDatabase database = this.getWritableDatabase();

                ContentValues VALUES = new ContentValues();
                VALUES.put(COLUMNS_USERS[1], username);
                VALUES.put(COLUMNS_USERS[2], password);
                VALUES.put(COLUMNS_USERS[3], language);
                VALUES.put(COLUMNS_USERS[4], locale);
                //show all overview content by default
                VALUES.put(COLUMNS_USERS[5], 1);
                VALUES.put(COLUMNS_USERS[6], 1);
                VALUES.put(COLUMNS_USERS[7], 1);
                VALUES.put(COLUMNS_USERS[8], 1);

                database.insert(TABLE_USERS, null, VALUES);

                //get the new usersId
                int userId = userLogin(username, password);//will close the database connection

                //make the users accounts (2)
                boolean bankFlag = createAccount(userId, "Bank","Bank",locale,bankBalance,bankBalance," ", false);
                boolean cashFlag = createAccount(userId, "Cash","Cash",locale,cashBalance,cashBalance," ", false);

                return bankFlag && cashFlag;
            }
        }
        catch(Exception exception) {
            Log.d("userSignUp()","Attempt to sign up failed");
            exception.printStackTrace();
            return false;
        }
    }

    private boolean uniqueUsername(String username) {
        try {
            //check to see if an account already exists with that username
            SQLiteDatabase database = this.getReadableDatabase();

            final String ARGUMENTS[] = {username};
            final String COLUMNS_SELECTED[] = {COLUMNS_USERS[0]};

            final Cursor cursor = database.query(TABLE_USERS, COLUMNS_SELECTED, COLUMNS_USERS[1] + " = ?", ARGUMENTS, null, null, null, "1");

            if(cursor.getCount() > 0) {
                //the username already exists
                cursor.close();
                database.close();
                return false;
            }
            else {
                //the username is unique
                cursor.close();
                database.close();
                return true;
            }
        }
        catch(Exception exception) {
            Log.d("userLogin()","Attempt to check username failed");
            exception.printStackTrace();
            return false;
        }
    }

    boolean setUserSettings(int userId, String language, String locale, String flag1, String flag2, String flag3, String flag4) {
        final SQLiteDatabase database = this.getWritableDatabase();

        final String ARGUMENTS[] = {Integer.toString(userId)};

        final ContentValues VALUES = new ContentValues();
        VALUES.put(COLUMNS_USERS[3], language);
        VALUES.put(COLUMNS_USERS[4], locale);
        VALUES.put(COLUMNS_USERS[5], flag1);
        VALUES.put(COLUMNS_USERS[6], flag2);
        VALUES.put(COLUMNS_USERS[7], flag3);
        VALUES.put(COLUMNS_USERS[8], flag4);

        database.update(TABLE_USERS, VALUES, COLUMNS_USERS[0] + " = ?", ARGUMENTS);
        database.close();

        return true;
    }

    HashMap<String,String> getUserSettings(int userId) {
        final SQLiteDatabase database = this.getReadableDatabase();

        final String ARGUMENTS[] = {Integer.toString(userId)};

        final Cursor cursor = database.query(TABLE_USERS, COLUMNS_USERS, COLUMNS_USERS[0] + " = ?", ARGUMENTS, null, null, null, "1");
        cursor.moveToFirst();

        //only need language and locale for now, if time, allow username and password change
        HashMap<String, String> userData = new HashMap<>();

        //userId, username, password, language, locale, adding in symbol for ease of use
        //userData[1] = cursor.getString(cursor.getColumnIndex(COLUMNS_USERS[1]));
        //userData[2] = cursor.getString(cursor.getColumnIndex(COLUMNS_USERS[2]));
        userData.put("language",cursor.getString(cursor.getColumnIndex(COLUMNS_USERS[3])));
        userData.put("locale",cursor.getString(cursor.getColumnIndex(COLUMNS_USERS[4])));
        userData.put("symbol",getLocaleCurrencySymbol(userData.get("locale")));
        userData.put("flag1",cursor.getString(cursor.getColumnIndex(COLUMNS_USERS[5])));
        userData.put("flag2",cursor.getString(cursor.getColumnIndex(COLUMNS_USERS[6])));
        userData.put("flag3",cursor.getString(cursor.getColumnIndex(COLUMNS_USERS[7])));
        userData.put("flag4",cursor.getString(cursor.getColumnIndex(COLUMNS_USERS[8])));

        cursor.close();
        database.close();

        return userData;
    }

    //new accounts initial balance = current balance
    boolean createAccount(int userId, String name, String type, String locale, double initialBalance, double currentBalance, String description, boolean hidden) {
        try {
            SQLiteDatabase database = this.getWritableDatabase();
            ContentValues VALUES = new ContentValues();
            VALUES.put(COLUMNS_ACCOUNTS[1], userId);
            VALUES.put(COLUMNS_ACCOUNTS[2], name);
            VALUES.put(COLUMNS_ACCOUNTS[3], type);
            VALUES.put(COLUMNS_ACCOUNTS[4], locale);
            VALUES.put(COLUMNS_ACCOUNTS[5], initialBalance);
            VALUES.put(COLUMNS_ACCOUNTS[6], currentBalance);
            VALUES.put(COLUMNS_ACCOUNTS[7], description);

            int hiddenValue;

            if(hidden) {
                hiddenValue = 1;
            }
            else {
                hiddenValue = 0;
            }

            VALUES.put(COLUMNS_ACCOUNTS[8], hiddenValue);

            database.insert(TABLE_ACCOUNTS, null, VALUES);

            database.close();

            return true;
        }
        catch(Exception exception) {
            Log.d("userSignUp()","Attempt to sign up failed");
            exception.printStackTrace();
            return false;
        }
    }

    @SuppressLint("DefaultLocale")
    Account[] getAccountList(int userId) {
        try {
            final ArrayList<Account> accountsList = new ArrayList<>();

            //check to see if an account already exists with that username
            final SQLiteDatabase database = this.getReadableDatabase();

            final String ARGUMENTS[] = {Integer.toString(userId)};
            final String COLUMNS_SELECTED[] = COLUMNS_ACCOUNTS;

            Cursor cursor;

            if(displayHiddenFlag) {
                //show hidden accounts
                cursor = database.query(TABLE_ACCOUNTS, COLUMNS_SELECTED, COLUMNS_ACCOUNTS[1] + " = ?", ARGUMENTS, null, null, null, null);
            }
            else {
                //don't show hidden accounts
                cursor = database.query(TABLE_ACCOUNTS, COLUMNS_SELECTED, COLUMNS_ACCOUNTS[1] + " = ? AND " + COLUMNS_ACCOUNTS[8] + " = 0", ARGUMENTS, null, null, null, null);
            }

            cursor.moveToFirst();

            int id;
            String name;
            String type;
            String locale;
            String symbol;
            double initialBalance;
            double currentBalance;
            String description;
            boolean hiddenFlag;

            for (int i = 0; i < cursor.getCount(); i++) {
                //get the account details from the database
                id = cursor.getInt(cursor.getColumnIndex(COLUMNS_SELECTED[0]));

                name = cursor.getString(cursor.getColumnIndex(COLUMNS_SELECTED[2]));

                type = cursor.getString(cursor.getColumnIndex(COLUMNS_SELECTED[3]));

                //only want the value inside the brackets (locale) not the country name
                locale = cursor.getString(cursor.getColumnIndex(COLUMNS_SELECTED[4]));

                symbol = getLocaleCurrencySymbol(locale);

                //get the amount as a string, only keep 2 decimal places
                initialBalance = cursor.getDouble((cursor.getColumnIndex(COLUMNS_SELECTED[5])));
                initialBalance = Double.parseDouble(String.format("%.2f", initialBalance));

                currentBalance = cursor.getDouble((cursor.getColumnIndex(COLUMNS_SELECTED[6])));
                currentBalance = Double.parseDouble(String.format("%.2f", currentBalance));

                description = cursor.getString(cursor.getColumnIndex(COLUMNS_SELECTED[7]));

                hiddenFlag = cursor.getInt(cursor.getColumnIndex(COLUMNS_ACCOUNTS[8])) != 0;

                accountsList.add(new Account(id, name, type, locale, symbol, initialBalance, currentBalance, description, hiddenFlag));

                cursor.moveToNext();
            }

            cursor.close();
            database.close();

            return accountsList.toArray(new Account[accountsList.size()]);
        }
        catch(Exception exception) {
            Log.d("getAccountList()","Attempt to get accounts failed");
            exception.printStackTrace();
            return null;
        }
    }

    //will return the account info as long as the id is valid, regardless if the account is hidden
    @SuppressLint("DefaultLocale")
    Account getAccountInfo(int accountId, int userId) {
        try {
            final SQLiteDatabase database = this.getReadableDatabase();

            final String ARGUMENTS[] = {Integer.toString(accountId), Integer.toString(userId)};

            Cursor cursor;

            cursor = database.query(TABLE_ACCOUNTS, COLUMNS_ACCOUNTS, COLUMNS_ACCOUNTS[0] + " = ? AND " + COLUMNS_ACCOUNTS[1] + " = ?", ARGUMENTS, null, null, null, "1");

            cursor.moveToFirst();

            if(cursor.getCount() > 0) {
                //get the account details from the database
                final int id = cursor.getInt(cursor.getColumnIndex(COLUMNS_ACCOUNTS[0]));

                final String name = cursor.getString(cursor.getColumnIndex(COLUMNS_ACCOUNTS[2]));

                final String type = cursor.getString(cursor.getColumnIndex(COLUMNS_ACCOUNTS[3]));

                //only want the value inside the brackets (locale) not the country name
                String locale = cursor.getString(cursor.getColumnIndex(COLUMNS_ACCOUNTS[4]));

                final String symbol = getLocaleCurrencySymbol(locale);

                //get the amount as a string, only keep 2 decimal places
                double initialBalance = cursor.getDouble((cursor.getColumnIndex(COLUMNS_ACCOUNTS[5])));
                initialBalance = Double.parseDouble(String.format("%.2f", initialBalance));

                double currentBalance = cursor.getDouble((cursor.getColumnIndex(COLUMNS_ACCOUNTS[6])));
                currentBalance = Double.parseDouble(String.format("%.2f", currentBalance));

                String description = cursor.getString(cursor.getColumnIndex(COLUMNS_ACCOUNTS[7]));

                boolean hiddenFlag = cursor.getInt(cursor.getColumnIndex(COLUMNS_ACCOUNTS[8])) != 0;

                cursor.close();
                database.close();

                return new Account(id, name, type, locale, symbol, initialBalance, currentBalance, description, hiddenFlag);
            }
            else {
                return null;
            }
       }
        catch(Exception exception) {
            Log.d("getAccountInfo()","Attempt to get account info failed");
            exception.printStackTrace();
            return null;
        }
    }

    boolean updateAccount(int userId, Account account) {
        final SQLiteDatabase database = this.getWritableDatabase();

        final String ARGUMENTS[] = {Integer.toString(account.getId()), Integer.toString(userId)};

        final ContentValues VALUES = new ContentValues();
        VALUES.put(COLUMNS_ACCOUNTS[2], account.getName());
        VALUES.put(COLUMNS_ACCOUNTS[3], account.getType());
        VALUES.put(COLUMNS_ACCOUNTS[4], account.getLocale());
        VALUES.put(COLUMNS_ACCOUNTS[5], Double.toString(account.getInitialBalance()));
        VALUES.put(COLUMNS_ACCOUNTS[6], Double.toString(account.getCurrentBalance()));
        VALUES.put(COLUMNS_ACCOUNTS[7], account.getDescription());

        if(account.getHiddenFlag()) {
            VALUES.put(COLUMNS_ACCOUNTS[8], 1);
        }
        else {
            VALUES.put(COLUMNS_ACCOUNTS[8], 0);
        }

        Log.d("description", account.getDescription());

        database.update(TABLE_ACCOUNTS, VALUES, COLUMNS_ACCOUNTS[0] + " = ? AND " + COLUMNS_ACCOUNTS[1] + " = ?", ARGUMENTS);
        database.close();

        return true;
    }

    boolean deleteAccount(int accountId, int userId) {
        try {
            SQLiteDatabase databaseWrite = this.getWritableDatabase();

            String ARGUMENTS[] = {Integer.toString(accountId), Integer.toString(userId)};

            databaseWrite.delete(TABLE_ACCOUNTS, COLUMNS_ACCOUNTS[0] + " = ? AND " + COLUMNS_ACCOUNTS[1] + " = ?", ARGUMENTS);

            //also delete transactions for that account for AccountFromId
            //want to keep transactions where accountTo, will replace those in checks
            databaseWrite.delete(TABLE_TRANSACTIONS, COLUMNS_TRANSACTIONS[2] + " = ? AND " + COLUMNS_TRANSACTIONS[1] + " = ?", ARGUMENTS);

            databaseWrite.close();

            return true;
        }
        catch(Exception e) {
            return false;
        }
    }

    boolean hideAccount(int accountId, int userId) {
        final SQLiteDatabase database = this.getWritableDatabase();

        final String ARGUMENTS[] = {Integer.toString(accountId), Integer.toString(userId)};

        final ContentValues VALUES = new ContentValues();
        VALUES.put(COLUMNS_ACCOUNTS[8], 1);

        database.update(TABLE_ACCOUNTS, VALUES, COLUMNS_ACCOUNTS[0] + " = ? AND " + COLUMNS_ACCOUNTS[1] + " = ?", ARGUMENTS);
        database.close();

        return true;
    }

    boolean createNewTransaction(Transaction transaction, int userId) {
        try {
            SQLiteDatabase database = this.getWritableDatabase();
            ContentValues VALUES = new ContentValues();
            VALUES.put(COLUMNS_TRANSACTIONS[1], userId);
            VALUES.put(COLUMNS_TRANSACTIONS[2], transaction.getAccountFromId());
            VALUES.put(COLUMNS_TRANSACTIONS[3], transaction.getAccountToId());
            VALUES.put(COLUMNS_TRANSACTIONS[4], transaction.getType());
            VALUES.put(COLUMNS_TRANSACTIONS[5], transaction.getLocale());
            VALUES.put(COLUMNS_TRANSACTIONS[6], transaction.getAmount());
            VALUES.put(COLUMNS_TRANSACTIONS[7], transaction.getDate());
            VALUES.put(COLUMNS_TRANSACTIONS[8], transaction.getNote());
            VALUES.put(COLUMNS_TRANSACTIONS[9], transaction.getPayee());

            database.insert(TABLE_TRANSACTIONS, null, VALUES);
            database.close();

            return true;
        }
        catch(Exception exception) {
            Log.d("newTransaction()","Attempt to create new transaction failed");
            exception.printStackTrace();
            return false;
        }
    }

    Transaction[] getTransactionsRange(int accountId, int userId, String startDay, String endDay) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor;
        String ARGUMENTS[];
        ArrayList<Transaction> transactionList = new ArrayList<>();

        String sqlQuery =
            "SELECT \n" +
                TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[2] + ",\n" +
                TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[3] + ",\n" +
                TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[4] + ",\n" +
                TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[5] + ",\n" +
                TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[6] + ",\n" +
                TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[7] + ",\n" +
                TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[8] + ", \n" +
                TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[9] + " \n" +
            "FROM \n" +
                TABLE_TRANSACTIONS + " \n";

        if(startDay.isEmpty() && endDay.isEmpty()) {
            //get all transactions of all time
            if(accountId > 0) {
                //grab specific account transactions
                if(displayHiddenFlag) {
                    //ignore the hidden flag
                    sqlQuery +=
                        "WHERE \n" +
                            TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[1] + " = ? \n" +
                        "AND " + TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[2] + " = ? \n";
                }
                else {
                    //don't grab transactions that come from a hidden account
                    sqlQuery +=
                        "JOIN " + TABLE_ACCOUNTS + " ON " + TABLE_ACCOUNTS + "." + COLUMNS_ACCOUNTS[0] + " = " + TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[2] + "\n" +
                        "WHERE \n" +
                            TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[1] + " = ? \n" +
                        "AND " + TABLE_ACCOUNTS + "." + COLUMNS_ACCOUNTS[8] + " = 0 \n" +
                        "AND " + TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[2] + " = ? \n";
                }

                sqlQuery += "ORDER BY " + COLUMNS_TRANSACTIONS[7] + " DESC;";

                ARGUMENTS = new String[]{Integer.toString(userId),Integer.toString(accountId)};

                cursor = database.rawQuery(sqlQuery, ARGUMENTS);
            }
            else {
                //grab all account transactions
                if(displayHiddenFlag) {
                    sqlQuery +=
                        "WHERE \n" +
                            TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[1] + " = ? \n";
                }
                else {
                    //don't grab transactions that come from a hidden account
                    sqlQuery +=
                        "JOIN " + TABLE_ACCOUNTS + " ON " + TABLE_ACCOUNTS + "." + COLUMNS_ACCOUNTS[0] + " = " + TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[2] + "\n" +
                        "WHERE \n" +
                            TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[1] + " = ? \n" +
                        "AND " + TABLE_ACCOUNTS + "." + COLUMNS_ACCOUNTS[8] + " = 0 \n";
                }

                sqlQuery += "ORDER BY " + COLUMNS_TRANSACTIONS[7] + " DESC;";

                ARGUMENTS = new String[]{Integer.toString(userId)};

                cursor = database.rawQuery(sqlQuery, ARGUMENTS);
            }
        }
        else {
            //get all transactions that match a date range
            if(accountId > 0) {
                //grab specific account transactions
                if(displayHiddenFlag) {
                    //ignore the hidden flag
                    sqlQuery +=
                        "WHERE \n" +
                            TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[1] + " = ? \n" +
                            "AND " + TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[2] + " = ? \n" +
                            "AND DATETIME(" + TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[7] + ") >= DATETIME(?) \n" +
                            "AND DATETIME(" + TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[7] + ") <= DATETIME(?) \n";
                }
                else {
                    //don't grab transactions that come from a hidden account
                    sqlQuery +=
                        "JOIN " + TABLE_ACCOUNTS + " ON " + TABLE_ACCOUNTS + "." + COLUMNS_ACCOUNTS[0] + " = " + TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[2] + "\n" +
                        "WHERE \n" +
                        TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[1] + " = ? \n" +
                        "AND " + TABLE_ACCOUNTS + "." + COLUMNS_ACCOUNTS[8] + " = 0 \n" +
                        "AND " + TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[2] + " = ? \n" +
                        "AND DATETIME(" + TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[7] + ") >= DATETIME(?) \n" +
                        "AND DATETIME(" + TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[7] + ") <= DATETIME(?) \n";
                }

                sqlQuery += "ORDER BY " + COLUMNS_TRANSACTIONS[7] + " DESC;";

                ARGUMENTS = new String[]{Integer.toString(userId),Integer.toString(accountId), startDay, endDay};

                cursor = database.rawQuery(sqlQuery, ARGUMENTS);
            }
            else {
                //grab all account transactions
                if(displayHiddenFlag) {
                    sqlQuery +=
                        "WHERE \n" +
                            TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[1] + " = ? \n" +
                        "AND DATETIME(" + TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[7] + ") >= DATETIME(?) \n" +
                        "AND DATETIME(" + TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[7] + ") <= DATETIME(?) \n";
                }
                else {
                    //don't grab transactions that come from a hidden account
                    sqlQuery +=
                        "JOIN " + TABLE_ACCOUNTS + " ON " + TABLE_ACCOUNTS + "." + COLUMNS_ACCOUNTS[0] + " = " + TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[2] + "\n" +
                        "WHERE \n" +
                            TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[1] + " = ? \n" +
                        "AND " + TABLE_ACCOUNTS + "." + COLUMNS_ACCOUNTS[8] + " = 0 \n" +
                        "AND DATETIME(" + TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[7] + ") >= DATETIME(?) \n" +
                        "AND DATETIME(" + TABLE_TRANSACTIONS + "." + COLUMNS_TRANSACTIONS[7] + ") <= DATETIME(?) \n";
                }

                sqlQuery += "ORDER BY " + COLUMNS_TRANSACTIONS[7] + " DESC;";

                ARGUMENTS = new String[]{Integer.toString(userId), startDay, endDay};

                cursor = database.rawQuery(sqlQuery, ARGUMENTS);
            }
        }

        cursor.moveToFirst();

        int accountFromId;
        int accountToId;
        String type;
        String locale;
        String symbol;
        double amount;
        String date;
        String note;
        String payee;

        for(int i = 0; i < cursor.getCount(); i ++) {
            accountFromId = cursor.getInt(cursor.getColumnIndex(COLUMNS_TRANSACTIONS[2]));
            accountToId = cursor.getInt(cursor.getColumnIndex(COLUMNS_TRANSACTIONS[3]));
            type = cursor.getString(cursor.getColumnIndex(COLUMNS_TRANSACTIONS[4]));
            locale = cursor.getString(cursor.getColumnIndex(COLUMNS_TRANSACTIONS[5]));
            symbol = getLocaleCurrencySymbol(locale);
            amount = cursor.getDouble(cursor.getColumnIndex(COLUMNS_TRANSACTIONS[6]));
            date = cursor.getString(cursor.getColumnIndex(COLUMNS_TRANSACTIONS[7]));
            note = cursor.getString(cursor.getColumnIndex(COLUMNS_TRANSACTIONS[8]));
            payee = cursor.getString(cursor.getColumnIndex(COLUMNS_TRANSACTIONS[9]));

            transactionList.add(new Transaction(accountFromId,accountToId,type,locale,symbol,amount,date,note, payee));

            cursor.moveToNext();
        }

        cursor.close();
        database.close();

        return transactionList.toArray(new Transaction[transactionList.size()]);
    }

    private String getLocaleCurrencySymbol(String locale) {
        String symbol = Currency.getInstance(locale).getSymbol();

        final StringTokenizer stringTokenizer = new StringTokenizer(symbol, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
        return stringTokenizer.nextToken();
    }

    static boolean getDisplayHiddenFlag() {
        return displayHiddenFlag;
    }

    static void setDisplayHiddenFlag(boolean flag) {
        displayHiddenFlag = flag;
    }
}