package com.mcmah113.mcmah113expensesiq;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class OverviewFragment extends Fragment {
    private DatabaseHelper databaseHelper;
    private LinearLayout linearLayoutRecentTransactions;
    private TextView textViewNoTransactions;

    public OverviewFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    public void onViewCreated(View view, Bundle bundle) {
        databaseHelper = new DatabaseHelper(getContext());

        final int userId = Overview.getUserId();

        final Account accountsList[] = databaseHelper.getAccountList(userId);

        //set up the My Accounts section of the overview
        final OverviewAdapter recyclerAdapter = new OverviewAdapter(accountsList);
        final RecyclerView recyclerView = view.findViewById(R.id.recyclerViewAccounts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setAdapter(recyclerAdapter);

        final TextView textViewNetBalance = view.findViewById(R.id.textViewNetBalance);

        String netBalanceString = view.getResources().getString(R.string.net_balance_string) + " ";

        final String balance = getNetBalance(userId);

        if(!balance.isEmpty()) {
            //got results back
            netBalanceString += "(" + balance + ")";
            textViewNetBalance.setText(netBalanceString);
        }
        else {
            //no results, so don't show anything
            textViewNetBalance.setText("");
        }

        //set up the recent transactions of the overview
        textViewNoTransactions = view.findViewById(R.id.textViewNoTransactions);
        linearLayoutRecentTransactions = view.findViewById(R.id.linearLayoutRecentTransactions);
        getRecentTransactionsSection(userId);
    }

    //creates a hash map to calculate the total amount that each currency
    //has in all accounts, then returns a 2D string array with the information
    public String[][] getSummary(Account accounts[]) {
        //go through each account element,
        //if the currency is unique, add a new hash entry
        //if it isn't unique, add it to the existing total
        HashMap<String, Double> hashMap = new HashMap<>();
        Double doubleObject;
        String locale;
        double balance;

        for(Account account : accounts) {
            locale = account.getLocale() + "-" + account.getSymbol();
            balance = account.getCurrentBalance();

            if(hashMap.containsKey(locale)) {
                //currency is not unique
                doubleObject = hashMap.get(locale);
                doubleObject += balance;
                hashMap.put(locale, doubleObject);
            }
            else {
                //currency is unique
                doubleObject = balance;
                hashMap.put(locale, doubleObject);
            }
        }

        //convert HashMap to 2D string array
        final String summary[][] = new String[hashMap.size()][2];
        String key;

        int i = 0;
        for(Map.Entry<String, Double> pair : hashMap.entrySet()) {
            key = pair.getKey();
            summary[i][0] = key.substring(0,key.indexOf('-'));
            summary[i][1] = key.substring(key.indexOf('-') + 1,key.length()) + pair.getValue();

            i ++;
        }

        return summary;
    }

    public String getNetBalance(int userId) {
        final DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
        final HashMap<String, String> userData = databaseHelper.getUserSettings(userId);
        final Account accountList[] = databaseHelper.getAccountList(userId);
        final String summary[][] = getSummary(accountList);

        if(summary.length != 0) {
            if(summary.length > 1) {
                //HashSet contains all of the account locales and
                //will be sent to the api call
                //will reference the locales using the HashMap for exchange
                //rates of the locales
                HashMap<String, String> hashMapData = new HashMap<>();
                final HashSet<String> hashSetLocales = new HashSet<>();
                hashSetLocales.add(userData.get("locale"));

                for(Account account : accountList) {
                    hashSetLocales.add(account.getLocale());
                }

                double totalMoneyAmount = 0.00;
                double currency2 = 1;
                boolean errorFlag;

                //check to see if there is at least two different accounts
                //save an API call
                if(hashSetLocales.size() > 1) {
                    try {
                        //API call to get all currency exchange rates
                        hashMapData = new FixerCurrencyAPI().execute(hashSetLocales.toArray(new String[hashSetLocales.size()])).get();

                        currency2 = Double.parseDouble(hashMapData.get(userData.get("locale")));

                        errorFlag = false;
                    }
                    catch(Exception e) {
                        //couldn't get conversions
                        //don't show the max money entry in the header
                        e.printStackTrace();
                        errorFlag = true;
                    }
                }
                else {
                    //only one or no types of locales, no point in
                    //reporting total locale currencies when there is only 1
                    errorFlag = true;
                }

                if(!errorFlag) {
                    //got results, find the net balance
                    for(String[] data : summary) {
                        //convert currency and add to total money
                        double value = Double.parseDouble(data[1].substring(1));
                        double currency1 = Double.parseDouble(hashMapData.get(data[0]));
                        double exchangeRate = currency1 / currency2;
                        totalMoneyAmount += (value / exchangeRate);
                    }

                    return String.format(userData.get("symbol") + "%.2f",totalMoneyAmount);
                }
                else {
                    //the API wasn't able to get any exchange rates
                    return "";
                }
            }
            else {
                //return the only value, which is the total of the only currency
                return summary[0][1];
            }
        }
        else {
            return "";
        }
    }

    @SuppressLint("SimpleDateFormat")
    public void getRecentTransactionsSection(int userId) {
        //show transaction histories for each account recently (this week)
        final Calendar calendar = Calendar.getInstance();
        final Calendar start = (Calendar) calendar.clone();
        start.add(Calendar.DAY_OF_WEEK, start.getFirstDayOfWeek() - start.get(Calendar.DAY_OF_WEEK));

        final Calendar end = (Calendar) start.clone();
        end.add(Calendar.DAY_OF_YEAR, 6);

        final String startDay = new SimpleDateFormat("yyyy-MM-dd").format(start.getTime());
        final String endDay = new SimpleDateFormat("yyyy-MM-dd").format(end.getTime());

        final Transaction transactionList[] = databaseHelper.getTransactionsRange(-1, userId, startDay, endDay);

        View viewTransactions;
        final LayoutInflater layoutInflater = LayoutInflater.from(getContext());

        if(transactionList.length > 0) {
            textViewNoTransactions.setVisibility(View.GONE);

            for(Transaction transaction : transactionList) {
                viewTransactions = layoutInflater.inflate(R.layout.layout_listview_transactions, null);

                if(transaction != null) {
                    final Account account = databaseHelper.getAccountInfo(transaction.getAccountFromId(), userId);

                    if (account != null) {
                        final TextView textViewName = viewTransactions.findViewById(R.id.textViewAccount);
                        textViewName.setText(account.getName());

                        final TextView textViewAmount = viewTransactions.findViewById(R.id.textViewAmount);

                        if (transaction.getAmount() > 0) {
                            textViewAmount.setText(String.format(transaction.getSymbol() + "%.2f", transaction.getAmount()));
                            textViewAmount.setTextColor(getContext().getResources().getColor(R.color.colorGreen, getContext().getTheme()));
                        }
                        else if (transaction.getAmount() < 0) {
                            textViewAmount.setText(String.format(transaction.getSymbol() + "%.2f", (-1 * transaction.getAmount())));
                            textViewAmount.setTextColor(getContext().getResources().getColor(R.color.colorRed, getContext().getTheme()));
                        }

                        final TextView textViewDate = viewTransactions.findViewById(R.id.textViewDate);
                        textViewDate.setText(transaction.getDate());

                        final TextView textViewNote = viewTransactions.findViewById(R.id.textViewNote);
                        final String transactionNote = getContext().getResources().getString(R.string.note_text) + "\n" + transaction.getNote();
                        textViewNote.setText(transactionNote);

                        final TextView textViewType = viewTransactions.findViewById(R.id.textViewType);

                        if (transaction.getAccountToId() > 0) {
                            //need to display a little more information like who the money went to / where it came from
                            final Account accountTo = databaseHelper.getAccountInfo(transaction.getAccountToId(), userId);
                            String type;

                            if (accountTo != null) {
                                if (accountTo.getHiddenFlag() && !DatabaseHelper.getDisplayHiddenFlag()) {
                                    if (transaction.getType().equals("Transfer")) {
                                        //sent money to an account
                                        type = getContext().getResources().getString(R.string.transferred_money_to_text) + " " + getContext().getResources().getString(R.string.account_hidden_text);
                                    }
                                    else {
                                        //received money from an account
                                        type = getContext().getResources().getString(R.string.recieved_money_from_transaction) + " " + getContext().getResources().getString(R.string.account_hidden_text);
                                    }
                                }
                                else {
                                    if (transaction.getType().equals("Transfer")) {
                                        //sent money to an account
                                        type = getContext().getResources().getString(R.string.transferred_money_to_text) + " " + accountTo.getName();
                                    }
                                    else {
                                        //received money from an account
                                        type = getContext().getResources().getString(R.string.recieved_money_from_transaction) + " " + accountTo.getName();
                                    }
                                }
                            }
                            else {
                                if (transaction.getType().equals("Transfer")) {
                                    //sent money to an account
                                    type = getContext().getResources().getString(R.string.transferred_money_to_text) + " " + getContext().getResources().getString(R.string.account_deleted_transaction);
                                }
                                else {
                                    //received money from an account
                                    type = getContext().getResources().getString(R.string.recieved_money_from_transaction) + " " + getContext().getResources().getString(R.string.account_deleted_transaction);
                                }
                            }
                            textViewType.setText(type);
                        }
                        else {
                            //show the transaction type
                            textViewType.setText(transaction.getType());
                        }
                    }
                }

                linearLayoutRecentTransactions.addView(viewTransactions);
            }
        }
        else {
            //no transactions, show the text
            textViewNoTransactions.setVisibility(View.VISIBLE);
        }
    }
}