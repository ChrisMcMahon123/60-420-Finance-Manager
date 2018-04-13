package com.mcmah113.mcmah113expensesiq;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class OverviewFragment extends Fragment {
    private DatabaseHelper databaseHelper;
    private LinearLayout linearLayoutRecentTransactions;
    private LinearLayout linearLayoutExpenseByCategoryReport;
    private LinearLayout linearLayoutIncomeVsExpense;

    private TextView textViewNoTransactions;

    public OverviewFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    public void onViewCreated(View view, Bundle bundle) {
        databaseHelper = new DatabaseHelper(getContext());

        final int userId = Overview.getUserId();

        final HashMap<String, String> userData = databaseHelper.getUserSettings(Overview.getUserId());

        //dealing with accounts
        if("1".equals(userData.get("flag1"))) {
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
        }
        else {
            //flag is false ("0"), so don't show / perform anything for that layout
            //since we are hiding it
            final LinearLayout linearLayoutAccounts = view.findViewById(R.id.linearLayoutOverviewAccounts);
            linearLayoutAccounts.setVisibility(View.GONE);
        }

        //dealing with transactions and reports using them
        final Calendar calendar = Calendar.getInstance();
        final Calendar start = (Calendar) calendar.clone();
        start.add(Calendar.DAY_OF_WEEK, start.getFirstDayOfWeek() - start.get(Calendar.DAY_OF_WEEK));

        final Calendar end = (Calendar) start.clone();
        end.add(Calendar.DAY_OF_YEAR, 6);

        @SuppressLint("SimpleDateFormat") final String startDay = new SimpleDateFormat("yyyy-MM-dd").format(start.getTime());
        @SuppressLint("SimpleDateFormat") final String endDay = new SimpleDateFormat("yyyy-MM-dd").format(end.getTime());

        final Transaction transactionList[] = databaseHelper.getTransactionsRange(-1, userId, startDay, endDay);

        if("1".equals(userData.get("flag2"))) {
            //set up the recent transactions of the overview
            textViewNoTransactions = view.findViewById(R.id.textViewNoTransactions);
            linearLayoutRecentTransactions = view.findViewById(R.id.linearLayoutRecentTransactions);
            getRecentTransactions(userId, transactionList);
        }
        else {
            final LinearLayout linearLayoutTransactions = view.findViewById(R.id.linearLayoutOverviewRecentTransactions);
            linearLayoutTransactions.setVisibility(View.GONE);
        }

        linearLayoutExpenseByCategoryReport = view.findViewById(R.id.linearLayoutExpenseByCategoryReport);

        if("1".equals(userData.get("flag3"))) {
            //show expense by category report
            getExpenseByCategory(userId, transactionList);
        }
        else {
            final LinearLayout linearLayoutTransactions = view.findViewById(R.id.linearLayoutExpensebyCategory);
            linearLayoutTransactions.setVisibility(View.GONE);
        }

        linearLayoutIncomeVsExpense = view.findViewById(R.id.linearLayoutIncomeVsExpense);

        if("1".equals(userData.get("flag4"))) {
            //show income vs Expense report
            getIncomeVsExpenseReport(userId, transactionList);
        }
        else {
            final LinearLayout linearLayoutTransactions = view.findViewById(R.id.linearLayoutIncomeExpense);
            linearLayoutTransactions.setVisibility(View.GONE);
        }
    }

    //creates a hash map to calculate the total amount that each currency
    //has in all accounts, then returns a 2D string array with the information
    public String[][] getSummary(Account accounts[]) {
        //go through each account element,
        //if the currency is unique, add a new hash entry
        //if it isn't unique, add it to the existing total
        HashMap<String, Double> hashMap = new HashMap<>();
        String locale;
        double balance;

        for(Account account : accounts) {
            locale = account.getLocale() + "-" + account.getSymbol();
            balance = account.getCurrentBalance();

            if(hashMap.containsKey(locale)) {
                //currency is not unique
                hashMap.put(locale, (hashMap.get(locale) + balance));
            }
            else {
                //currency is unique
                hashMap.put(locale, balance);
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

        if(summary.length > 0) {
            //HashSet contains all of the account locales and
            //will be sent to the api call
            //will reference the locales using the HashMap for exchange
            //rates of the locales
            HashMap<String, String> hashMapData;
            final HashSet<String> hashSetLocales = new HashSet<>();
            hashSetLocales.add(userData.get("locale"));

            for(Account account : accountList) {
                hashSetLocales.add(account.getLocale());
            }

            double totalMoneyAmount = 0.00;
            double currency2;

            //check to see if there's more than 1 type, then convert OR
            //if there's only 1, if its not the users locales
            //save an API call
            if(hashSetLocales.size() > 1 ||(hashSetLocales.size() == 1 && !hashSetLocales.contains(userData.get("locale")))) {
                try {
                    //API call to get all currency exchange rates
                    hashMapData = new FixerCurrencyAPI().execute(hashSetLocales.toArray(new String[hashSetLocales.size()])).get();

                    currency2 = Double.parseDouble(hashMapData.get(userData.get("locale")));
                }
                catch(Exception e) {
                    //couldn't get conversions
                    //don't show the max money entry in the header
                    e.printStackTrace();
                    return "";
                }
            }
            else {
                //only one or no types of locales, no point in
                //reporting total locale currencies when there is only 1
                return summary[0][1];
            }

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
            //return the only value, which is the total of the only currency
            return "";
        }
    }

    public void getRecentTransactions(int userId, Transaction transactionList[]) {
        //show transaction histories for each account recently (this week)
        final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View row;

        int count = 0;

        if(transactionList.length > 0) {
            textViewNoTransactions.setVisibility(View.GONE);

            for(Transaction transaction : transactionList) {
                if (count == 5) {
                    break;
                } else {
                    count++;
                }

                row = layoutInflater.inflate(R.layout.layout_listview_transactions, null);

                if (transaction != null) {
                    //get the accounts details to display
                    final Account accountFrom = databaseHelper.getAccountInfo(transaction.getAccountFromId(), userId);

                    final TextView textViewName = row.findViewById(R.id.textViewAccount);
                    textViewName.setText(accountFrom.getName());

                    final TextView textViewAmount = row.findViewById(R.id.textViewAmount);

                    if (transaction.getAmount() > 0) {
                        textViewAmount.setTextColor(getContext().getResources().getColor(R.color.colorGreen, getContext().getTheme()));
                    } else if (transaction.getAmount() < 0) {
                        textViewAmount.setTextColor(getContext().getResources().getColor(R.color.colorRed, getContext().getTheme()));
                    }

                    textViewAmount.setText(String.format(transaction.getSymbol() + "%.2f", (Math.abs(transaction.getAmount()))));

                    final TextView textViewDate = row.findViewById(R.id.textViewDate);
                    textViewDate.setText(transaction.getDate());

                    final TextView textViewNote = row.findViewById(R.id.textViewNote);
                    final String transactionNote = getContext().getResources().getString(R.string.note_text) + "\n" + transaction.getNote();
                    textViewNote.setText(transactionNote);

                    final TextView textViewType = row.findViewById(R.id.textViewType);
                    final TextView textViewPayee = row.findViewById(R.id.textViewPayee);

                    if (transaction.getAccountToId() > 0 && (transaction.getType().equals("Transfer") || transaction.getType().equals("Receive"))) {
                        //need to display a little more information like who the money went to / where it came from
                        textViewPayee.setText(getContext().getResources().getString(R.string.transferred_funds_label));

                        final Account accountTo = databaseHelper.getAccountInfo(transaction.getAccountToId(), userId);
                        String type = "";

                        if (accountTo != null) {
                            if (accountTo.getHiddenFlag() && !DatabaseHelper.getDisplayHiddenFlag()) {
                                if (transaction.getType().equals("Transfer")) {
                                    //sent money to an account
                                    type = getContext().getResources().getString(R.string.transferred_money_to_text) + " " + getContext().getResources().getString(R.string.account_hidden_text);
                                } else if (transaction.getType().equals("Receive")) {
                                    //received money from an account
                                    type = getContext().getResources().getString(R.string.recieved_money_from_transaction) + " " + getContext().getResources().getString(R.string.account_hidden_text);
                                }
                            } else {
                                if (transaction.getType().equals("Transfer")) {
                                    //sent money to an account
                                    type = getContext().getResources().getString(R.string.transferred_money_to_text) + " " + accountTo.getName();
                                } else if (transaction.getType().equals("Receive")) {
                                    //received money from an account
                                    type = getContext().getResources().getString(R.string.recieved_money_from_transaction) + " " + accountTo.getName();
                                }
                            }
                        } else {
                            if (transaction.getType().equals("Transfer")) {
                                //sent money to an account
                                type = getContext().getResources().getString(R.string.transferred_money_to_text) + " " + getContext().getResources().getString(R.string.account_deleted_transaction);
                            } else if (transaction.getType().equals("Receive")) {
                                //received money from an account
                                type = getContext().getResources().getString(R.string.recieved_money_from_transaction) + " " + getContext().getResources().getString(R.string.account_deleted_transaction);
                            }
                        }

                        textViewType.setText(type);
                    } else {
                        //show the transaction type
                        textViewPayee.setText(transaction.getPayee());
                        textViewType.setText(transaction.getType());
                    }
                }

                linearLayoutRecentTransactions.addView(row);
            }
        }
        else {
            //no transactions, show the text
            textViewNoTransactions.setVisibility(View.VISIBLE);
        }
    }

    public void getExpenseByCategory(int userId, Transaction transactionList[]) {
        final HashMap<String, String> userData = databaseHelper.getUserSettings(userId);

        final HashMap<String, Double> hashMapAmount = new HashMap<>();

        String type;
        String locale;
        double amount;
        double exchangeAmount;
        double totalAmount = 0.0;

        final ArrayList<LinearLayout> linearLayoutLegendEntry = new ArrayList<>();

        if(transactionList.length > 0) {
            for (Transaction transaction : transactionList) {
                amount = transaction.getAmount();
                if (amount < 0) {
                    type = transaction.getType();
                    locale = transaction.getLocale();
                    exchangeAmount = currencyExchange(userData.get("locale"), locale, amount);

                    totalAmount += exchangeAmount;

                    if (hashMapAmount.containsKey(type)) {
                        //that specific type already exists, increment count by 1
                        hashMapAmount.put(type, (hashMapAmount.get(type) + exchangeAmount));
                    }
                    else {
                        //type is unique, start count at 1
                        hashMapAmount.put(type, exchangeAmount);

                        //add unique legend entry (the row)
                        final LayoutInflater layoutInflater = getActivity().getLayoutInflater();
                        linearLayoutLegendEntry.add((LinearLayout) layoutInflater.inflate(R.layout.layout_graph_legend, null));
                    }
                }
            }

            if(hashMapAmount.size() > 0) {
                linearLayoutExpenseByCategoryReport.addView(createPieChart(userData, hashMapAmount, totalAmount, linearLayoutLegendEntry));
            }
            else {
                linearLayoutExpenseByCategoryReport.addView(displayNoTransactionsText());
            }
        }
        else {
            linearLayoutExpenseByCategoryReport.addView(displayNoTransactionsText());
        }
    }

    public void getIncomeVsExpenseReport(int userId, Transaction transactionList[]) {
        final HashMap<String, String> userData = databaseHelper.getUserSettings(userId);

        final HashMap<String, Double> hashMapAmount = new HashMap<>();

        String locale;
        double amount;
        double exchangeAmount;
        double totalAmount = 0.0;

        final ArrayList<LinearLayout> linearLayoutLegendEntry = new ArrayList<>();

        if(transactionList.length > 0) {
            for (Transaction transaction : transactionList) {
                amount = transaction.getAmount();
                locale = transaction.getLocale();
                exchangeAmount = currencyExchange(userData.get("locale"), locale, amount);

                if(amount > 0) {
                    if (hashMapAmount.containsKey("Income")) {
                        //that specific type already exists, increment count by 1
                        hashMapAmount.put("Income", (hashMapAmount.get("Income") + exchangeAmount));
                    }
                    else {
                        //type is unique, start count at 1
                        hashMapAmount.put("Income", exchangeAmount);
                    }
                }
                else if(amount < 0) {
                    if (hashMapAmount.containsKey("Expense")) {
                        //that specific type already exists, increment count by 1
                        hashMapAmount.put("Expense", (hashMapAmount.get("Expense") + exchangeAmount));
                    }
                    else {
                        //type is unique, start count at 1
                        hashMapAmount.put("Expense", exchangeAmount);
                    }
                }

                //add unique legend entry (the row)
                final LayoutInflater layoutInflater = getActivity().getLayoutInflater();
                linearLayoutLegendEntry.add((LinearLayout) layoutInflater.inflate(R.layout.layout_graph_legend, null));
            }

            if(hashMapAmount.size() > 0) {
                if(hashMapAmount.containsKey("Expense") && hashMapAmount.containsKey("Income")) {
                    totalAmount = Math.abs(hashMapAmount.get("Income") - hashMapAmount.get("Expense"));
                }
                else if(hashMapAmount.containsKey("Income")) {
                    totalAmount = hashMapAmount.get("Income");
                }
                else if(hashMapAmount.containsKey("Expense")) {
                    totalAmount = hashMapAmount.get("Expense") * -1;
                }

                linearLayoutIncomeVsExpense.addView(createPieChart(userData, hashMapAmount, totalAmount, linearLayoutLegendEntry));
            }
            else {
                linearLayoutIncomeVsExpense.addView(displayNoTransactionsText());
            }
        }
        else {
            linearLayoutIncomeVsExpense.addView(displayNoTransactionsText());
        }
    }

    public View createPieChart(HashMap<String, String> userData, HashMap<String, Double> hashMapAmount, double totalAmount, ArrayList<LinearLayout> linearLayoutLegendEntry) {
        //get the text of the inner pie that represents the
        final PieChart pieChart = new PieChart(getContext());

        @SuppressLint("DefaultLocale") String totalAmountString = userData.get("symbol") + String.format("%.2f", totalAmount);

        final int colorPalette[] = GlobalConstants.getColorPalette();

        final LinearLayout linearLayoutLegend = new LinearLayout(getContext());
        linearLayoutLegend.setOrientation(LinearLayout.VERTICAL);

        ImageView imageViewLegend;
        TextView textViewLegend;
        TextView textViewPercent;
        View view;
        int i = 0;

        final List<PieEntry> entries = new ArrayList<>();

        for(Map.Entry<String, Double> pair : hashMapAmount.entrySet()) {
            entries.add(new PieEntry((float) (pair.getValue() / Math.abs(totalAmount)), pair.getKey()));

            view = linearLayoutLegendEntry.get(i);
            textViewLegend = view.findViewById(R.id.textViewLegend);
            textViewLegend.setText(pair.getKey());

            imageViewLegend = view.findViewById(R.id.imageViewLegend);
            imageViewLegend.setColorFilter(colorPalette[i], PorterDuff.Mode.SRC_ATOP);

            textViewPercent = view.findViewById(R.id.textViewPercent);
            @SuppressLint("DefaultLocale") String text = userData.get("symbol") + String.format("%.2f", pair.getValue());
            textViewPercent.setText(text);
            linearLayoutLegend.addView(view);

            i++;
        }

        //customize the graph adding style and data
        final PieDataSet set = new PieDataSet(entries, " ");
        set.setColors(colorPalette);
        set.setValueTextSize(18);
        set.setDrawValues(false);
        set.setValueTextColor(Color.WHITE);

        final PieData data = new PieData(set);

        //shows the name of the chart in the lower corner
        final Description description = new Description();
        description.setText(" ");
        description.setTextSize(18);

        final Legend legend = pieChart.getLegend();
        legend.setTextSize(22);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setEnabled(false);

        pieChart.setDrawEntryLabels(false);//text that shows on each pie slice
        pieChart.setData(data);
        pieChart.setUsePercentValues(true);//percentages that show up on each pie slice
        pieChart.setCenterTextColor(Color.BLACK);
        pieChart.setCenterText(totalAmountString);
        pieChart.setCenterTextSize(18);
        pieChart.setDescription(description);
        pieChart.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1500));
        pieChart.setTouchEnabled(false);
        pieChart.invalidate();

        //create my own legend
        final TextView textViewTitle = new TextView(getContext());
        textViewTitle.setText(getContext().getResources().getString(R.string.legend_title));
        textViewTitle.setTextSize(22);
        textViewTitle.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        textViewTitle.setPadding(15, 15, 15, 15);

        final LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        linearLayout.addView(pieChart);
        linearLayout.addView(textViewTitle);
        linearLayout.addView(linearLayoutLegend);

        return linearLayout;
    }

    public View displayNoTransactionsText() {
        //no transactions, show text;
        final TextView textView = new TextView(getContext());
        textView.setTextSize(18);
        textView.setTextColor(Color.BLACK);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setText(getContext().getResources().getString(R.string.zero_transactions_no_show));
        return textView;
    }

    private double currencyExchange(String userLocale, String amountLocale, double amount) {
        final HashMap<String, String> hashMapData = GlobalConstants.getHashMapExchangeRates();
        final double currency2 = Double.parseDouble(hashMapData.get(userLocale));
        final double currency1 = Double.parseDouble(hashMapData.get(amountLocale));
        final double exchangeRate = currency1 / currency2;

        return Math.abs(amount / exchangeRate);
    }
}