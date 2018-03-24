package com.mcmah113.mcmah113expensesiq;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Currency;
import java.util.StringTokenizer;

public class DatabaseHelper extends SQLiteOpenHelper {
    //database information
    private static final String DATABASE_NAME = "ExpenseIq.db";
    private static final int DATABASE_VERSION = 1111;

    //table names
    private static final String TABLE_USERS = "Users";
    private static final String TABLE_ACCOUNTS = "Accounts";

    //tables columns
    private static final String COLUMNS_USERS[] = {
                                                "userId",
                                                "username",
                                                "password",
                                                "language",
                                                "locale"
                                            };

    private static final String COLUMNS_ACCOUNTS[] = {
                                                    "accountId",
                                                    "userId",
                                                    "name",
                                                    "type",
                                                    "locale",
                                                    "starting_balance",
                                                    "current_balance",
                                                    "description",
                                                    "hidden"
                                                };

    private static boolean displayHiddenFlag = false;

    DatabaseHelper(Context context) {
        //only care about the database name being accessed and the context
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase database) {
        //building the sql queries that will create the tables
        String createUsersTable =
            "CREATE TABLE " + TABLE_USERS + " (\n" +
                COLUMNS_USERS[0] + " INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                COLUMNS_USERS[1] + " TEXT,\n" +
                COLUMNS_USERS[2] + " TEXT, \n" +
                COLUMNS_USERS[3] + " TEXT, \n" +
                COLUMNS_USERS[4] + " TEXT \n" +
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

        try {
            database.execSQL(createUsersTable);
            database.execSQL(createAccountsTable);

            Log.d("Users SQL Query", createUsersTable);
            Log.d("Accounts SQL Query", createAccountsTable);
        }
        catch(Exception exception){
            //failed to create the tables
            Log.d("onCreate()", "Failed to create the database tables");
            Log.d("Users SQL Query", createUsersTable);
            Log.d("Accounts SQL Query", createAccountsTable);
            exception.printStackTrace();
        }
    }

    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        //delete the tables when the database gets updated
        String dropTableUsers = "DROP TABLE IF EXISTS "+ TABLE_USERS;
        String dropTableAccounts = "DROP TABLE IF EXISTS " + TABLE_ACCOUNTS;
        database.execSQL(dropTableUsers);
        database.execSQL(dropTableAccounts);

        this.onCreate(database);
    }

    public void onDowngrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        onUpgrade(database, oldVersion, newVersion);
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

                database.insert(TABLE_USERS, null, VALUES);

                //get the new usersId
                int userId = userLogin(username, password);//will close the database connection

                //make the users accounts (2)
                boolean bankFlag = createAccount(userId, "Bank","Bank",locale,bankBalance,"", false);
                boolean cashFlag = createAccount(userId, "Cash","Cash",locale,cashBalance,"", false);

                if(bankFlag && cashFlag) {
                    return true;
                }
                else {
                    return false;
                }
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

    boolean setUserSettings(int userId, String language, String locale) {
        final SQLiteDatabase database = this.getWritableDatabase();

        final String ARGUMENTS[] = {Integer.toString(userId)};

        final ContentValues VALUES = new ContentValues();
        VALUES.put(COLUMNS_USERS[3], language);
        VALUES.put(COLUMNS_USERS[4], locale);

        database.update(TABLE_USERS, VALUES, COLUMNS_USERS[0] + " = ?", ARGUMENTS);
        database.close();

        return true;
    }

    String[] getUserSettings(int userId) {
        final SQLiteDatabase database = this.getReadableDatabase();

        final String ARGUMENTS[] = {Integer.toString(userId)};

        final Cursor cursor = database.query(TABLE_USERS, COLUMNS_USERS, COLUMNS_USERS[0] + " = ?", ARGUMENTS, null, null, null, "1");
        cursor.moveToFirst();

        //only need language and locale for now, if time, allow username and password change
        String userData[] = new String[2];

        //userData[1] = cursor.getString(cursor.getColumnIndex(COLUMNS_USERS[1]));
        //userData[2] = cursor.getString(cursor.getColumnIndex(COLUMNS_USERS[2]));
        userData[0] = cursor.getString(cursor.getColumnIndex(COLUMNS_USERS[3]));
        userData[1] = cursor.getString(cursor.getColumnIndex(COLUMNS_USERS[4]));

        cursor.close();
        database.close();

        return userData;
    }

    //new accounts initial balance = current balance
    boolean createAccount(int userId, String name, String type, String locale, double balance, String description, boolean hidden) {
        try {
            SQLiteDatabase database = this.getWritableDatabase();
            ContentValues VALUES = new ContentValues();

            VALUES.put(COLUMNS_ACCOUNTS[1], userId);
            VALUES.put(COLUMNS_ACCOUNTS[2], name);
            VALUES.put(COLUMNS_ACCOUNTS[3], type);
            VALUES.put(COLUMNS_ACCOUNTS[4], locale);
            VALUES.put(COLUMNS_ACCOUNTS[5], balance);
            VALUES.put(COLUMNS_ACCOUNTS[6], balance);
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
                locale = locale.substring(locale.indexOf('(') + 1, locale.indexOf(')'));

                symbol = getLocaleCurrencySymbol(locale);

                //get the amount as a string, only keep 2 decimal places
                initialBalance = cursor.getDouble((cursor.getColumnIndex(COLUMNS_SELECTED[5])));
                initialBalance = Double.parseDouble(String.format("%.2f", initialBalance));

                currentBalance = cursor.getDouble((cursor.getColumnIndex(COLUMNS_SELECTED[6])));
                currentBalance = Double.parseDouble(String.format("%.2f", currentBalance));

                description = cursor.getString(cursor.getColumnIndex(COLUMNS_SELECTED[7]));


                if(cursor.getInt(cursor.getColumnIndex(COLUMNS_ACCOUNTS[8])) == 0) {
                    //account is not hidden
                    hiddenFlag = false;
                }
                else {
                    //account is hidden
                    hiddenFlag = true;
                }

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

    Account getAccountInfo(int accountId, int userId) {
        try {
            final SQLiteDatabase database = this.getReadableDatabase();

            final String ARGUMENTS[] = {Integer.toString(accountId), Integer.toString(userId)};

            Cursor cursor;

            if(displayHiddenFlag) {
                //show hidden accounts
                cursor = database.query(TABLE_ACCOUNTS, COLUMNS_ACCOUNTS, COLUMNS_ACCOUNTS[0] + " = ? AND " + COLUMNS_ACCOUNTS[1] + " = ?", ARGUMENTS, null, null, null, "1");
            }
            else {
                //don't show hidden accounts
                cursor = database.query(TABLE_ACCOUNTS, COLUMNS_ACCOUNTS, COLUMNS_ACCOUNTS[0] + " = ? AND " + COLUMNS_ACCOUNTS[1] + " = ? AND " + COLUMNS_ACCOUNTS[8] + " = 0", ARGUMENTS, null, null, null, "1");
            }

            cursor.moveToFirst();

            //get the account details from the database
            final int id = cursor.getInt(cursor.getColumnIndex(COLUMNS_ACCOUNTS[0]));

            final String name = cursor.getString(cursor.getColumnIndex(COLUMNS_ACCOUNTS[2]));

            final String type = cursor.getString(cursor.getColumnIndex(COLUMNS_ACCOUNTS[3]));

            //only want the value inside the brackets (locale) not the country name
            String locale = cursor.getString(cursor.getColumnIndex(COLUMNS_ACCOUNTS[4]));
            locale = locale.substring(locale.indexOf('(') + 1, locale.indexOf(')'));

            final String symbol = getLocaleCurrencySymbol(locale);

            //get the amount as a string, only keep 2 decimal places
            double initialBalance = cursor.getDouble((cursor.getColumnIndex(COLUMNS_ACCOUNTS[5])));
            initialBalance = Double.parseDouble(String.format("%.2f", initialBalance));

            double currentBalance = cursor.getDouble((cursor.getColumnIndex(COLUMNS_ACCOUNTS[6])));
            currentBalance = Double.parseDouble(String.format("%.2f", currentBalance));

            String description = cursor.getString(cursor.getColumnIndex(COLUMNS_ACCOUNTS[7]));

            boolean hiddenFlag;

            if(cursor.getInt(cursor.getColumnIndex(COLUMNS_ACCOUNTS[8])) == 0) {
                //account is not hidden
                hiddenFlag = false;
            }
            else {
               //account is hidden
                hiddenFlag = true;
            }

            cursor.close();
            database.close();

            return new Account(id, name, type, locale, symbol, initialBalance, currentBalance, description, hiddenFlag);
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
        VALUES.put(COLUMNS_ACCOUNTS[8], account.getHiddenFlag());

        database.update(TABLE_ACCOUNTS, VALUES, COLUMNS_ACCOUNTS[0] + " = ? AND " + COLUMNS_ACCOUNTS[1] + " = ?", ARGUMENTS);
        database.close();

        return true;
    }

    boolean deleteAccount(int accountId, int userId) {
        try {
            SQLiteDatabase databaseWrite = this.getWritableDatabase();

            String ARGUMENTS[] = {Integer.toString(accountId), Integer.toString(userId)};

            databaseWrite.delete(TABLE_ACCOUNTS, COLUMNS_ACCOUNTS[0] + " = ? AND " + COLUMNS_ACCOUNTS[1] + " = ?", ARGUMENTS);
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

    private String getLocaleCurrencySymbol(String locale) {
        String symbol = Currency.getInstance(locale).getSymbol();

        final StringTokenizer stringTokenizer = new StringTokenizer(symbol, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
        return stringTokenizer.nextToken();
    }

    public static boolean getDisplayHiddenFlag() {
        return displayHiddenFlag;
    }

    public static void setDisplayHiddenFlag(boolean flag) {
        displayHiddenFlag = flag;
    }
}