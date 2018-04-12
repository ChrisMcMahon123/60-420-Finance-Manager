package com.mcmah113.mcmah113expensesiq;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportsGraphFragment extends Fragment {
    private DatabaseHelper databaseHelper;
    private int graphType;
    private int accountId;
    private LinearLayout linearLayoutGraphArea;
    private final String weekList[] = new String[7];

    public ReportsGraphFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports_graph, container, false);
    }

    public void onViewCreated(View view, Bundle bundle) {
        databaseHelper = new DatabaseHelper(getContext());

        final int userId = Overview.getUserId();

        final Account[] accountsList = databaseHelper.getAccountList(userId);
        final String spinnerString[] = new String[accountsList.length + 1];

        //creating array that will be used for the spinners
        spinnerString[0] = "All Accounts";

        for (int i = 0; i < accountsList.length; i++) {
            spinnerString[i + 1] = accountsList[i].getName() + " (" + accountsList[i].getLocale() + ")";
        }

        final TextView textViewPeriod = view.findViewById(R.id.textViewPeriod);
        final Spinner spinnerPeriod = view.findViewById(R.id.spinnerPeriod);
        final Spinner spinnerAccount = view.findViewById(R.id.spinnerAccount);
        final Spinner spinnerType = view.findViewById(R.id.spinnerType);

        final ArrayAdapter<String> arrayAdapterAccount = new ArrayAdapter<>(getContext(), R.layout.layout_spinner_alt_text, spinnerString);
        final ArrayAdapter<String> arrayAdapterPeriod = new ArrayAdapter<>(getContext(), R.layout.layout_spinner_alt_text, GlobalConstants.getTransactionPeriods());

        spinnerAccount.setAdapter(arrayAdapterAccount);
        spinnerAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    //all has been selected
                    accountId = -1;
                } else {
                    //get account id from real account
                    accountId = accountsList[position - 1].getId();
                }

                updateGraph(userId, accountId, spinnerPeriod.getSelectedItemPosition(), spinnerType.getSelectedItemPosition());
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerPeriod.setAdapter(arrayAdapterPeriod);
        spinnerPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateGraph(userId, accountId, spinnerPeriod.getSelectedItemPosition(), spinnerType.getSelectedItemPosition());
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        graphType = getArguments().getInt("accountId");

        switch (graphType) {
            case -1:
                spinnerType.setVisibility(View.GONE);
                break;
            case -2:
                spinnerPeriod.setSelection(2);//for this week
                spinnerType.setVisibility(View.GONE);
                spinnerPeriod.setVisibility(View.GONE);
                textViewPeriod.setVisibility(View.GONE);
                break;
            case -3:
                ArrayList<String> arrayListTypes = new ArrayList<>();
                arrayListTypes.add("All Categories");
                arrayListTypes.addAll(Arrays.asList(GlobalConstants.getTransactionTypeList()));

                final ArrayAdapter<String> arrayAdapterType = new ArrayAdapter<>(getContext(),R.layout.layout_spinner_alt_text, arrayListTypes.toArray(new String[arrayListTypes.size()]));
                spinnerAccount.setAdapter(arrayAdapterType);
                spinnerType.setAdapter(arrayAdapterType);
                spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        updateGraph(userId, accountId, spinnerPeriod.getSelectedItemPosition(), spinnerType.getSelectedItemPosition());
                    }

                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                break;
        }

        linearLayoutGraphArea = view.findViewById(R.id.linearLayoutGraphArea);
    }

    @SuppressLint({"SimpleDateFormat", "DefaultLocale"})
    public void updateGraph(int userId, int accountId, int positionOfSelected, int transactionTypePosition) {
        linearLayoutGraphArea.removeAllViewsInLayout();

        final Transaction transactionList[] = updateTransactions(userId, accountId, positionOfSelected);

        final String reportType = getArguments().getString("callFrom");

        final HashMap<String, String> userData = databaseHelper.getUserSettings(userId);

        final HashMap<String, Double> hashMapAmount = new HashMap<>();

        final String monthsList[] = GlobalConstants.getMonthsArray();

        final Date currentTime = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") final String today = new SimpleDateFormat("yyyy-MM-dd").format(currentTime);


        int monthInt = Integer.parseInt(today.substring(today.indexOf('-')+1, today.lastIndexOf('-'))) -1;
        int dayInt = Integer.parseInt(today.substring(today.lastIndexOf('-')+1, today.length()));
        String dayOfWeek = GlobalConstants.getMonthsArray()[monthInt] + " " + dayInt;

        String todayWeekDayFormat = dayOfWeek;
        int dayNumberOfWeek = 0;
        boolean foundDay = false;

        String date;
        String type;
        String locale;
        double amount;
        double exchangeAmount;
        double totalAmount = 0.0;

        final ArrayList<LinearLayout> linearLayoutLegendEntry = new ArrayList<>();

        //now that the inputs have been resolved and the data has been retrieved
        //determine which graph to show
        switch (reportType) {
            case "Expense":
                switch (graphType) {
                    case -1:
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
                                createPieChart(userData, hashMapAmount, totalAmount, linearLayoutLegendEntry);
                            }
                            else {
                                displayNoTransactionsText();
                            }
                        }
                        else {
                            displayNoTransactionsText();
                        }
                        break;
                    case -2:
                        if(transactionList.length > 0) {
                            //add all the weeks to the HashMap first
                            for(String weekDay: weekList) {
                                hashMapAmount.put(weekDay, 0.0);

                                if(!foundDay) {
                                    dayNumberOfWeek ++;//starts at 0.
                                }

                                if(todayWeekDayFormat.equals(weekDay)) {
                                    foundDay = true;
                                }
                            }

                            for (Transaction transaction : transactionList) {
                                amount = transaction.getAmount();

                                if(amount < 0) {
                                    date = transaction.getDate();
                                    locale = transaction.getLocale();
                                    exchangeAmount = currencyExchange(userData.get("locale"), locale, amount);

                                    monthInt = Integer.parseInt(date.substring(date.indexOf('-')+1, date.lastIndexOf('-'))) -1;
                                    dayInt = Integer.parseInt(date.substring(date.lastIndexOf('-')+1, date.length()));
                                    dayOfWeek = GlobalConstants.getMonthsArray()[monthInt] + " " + dayInt;
                                    totalAmount += exchangeAmount;

                                    //specifically looking at the Month - Day
                                    if(hashMapAmount.containsKey(dayOfWeek)) {
                                        hashMapAmount.put(dayOfWeek, (hashMapAmount.get(dayOfWeek) + exchangeAmount));
                                    }
                                    else {
                                        hashMapAmount.put(dayOfWeek, exchangeAmount);
                                    }
                                }
                            }

                            if(hashMapAmount.size() > 0) {
                                for(Map.Entry<String, Double> pair : hashMapAmount.entrySet()) {
                                    Log.d(pair.getKey(), "" + pair.getValue());
                                }

                                createBarChart(userData, hashMapAmount, totalAmount, dayNumberOfWeek, "Expense", Arrays.asList(weekList), "weekly");
                            }
                            else {
                                displayNoTransactionsText();
                            }
                        }
                        else {
                            displayNoTransactionsText();
                        }
                        break;
                    case -3:
                        if(transactionList.length > 0) {
                            //add all the months to the HashMap first
                            for(String month: monthsList) {
                                hashMapAmount.put(month, 0.0);
                            }

                            for (Transaction transaction : transactionList) {
                                amount = transaction.getAmount();
                                type = transaction.getType();

                                if(amount < 0) {
                                    if(transactionTypePosition == 0 || (transactionTypePosition > 0 && type.equals(GlobalConstants.getTransactionTypeList()[transactionTypePosition -1]))) {
                                        date = transaction.getDate();
                                        locale = transaction.getLocale();
                                        exchangeAmount = currencyExchange(userData.get("locale"), locale, amount);

                                        totalAmount += exchangeAmount;

                                        //specifically looking at the month part of the string

                                        monthInt = Integer.parseInt(date.substring(date.indexOf('-') + 1, date.lastIndexOf('-'))) -1;

                                        if(hashMapAmount.containsKey(monthsList[monthInt])) {
                                            hashMapAmount.put(monthsList[monthInt], (hashMapAmount.get(monthsList[monthInt]) + exchangeAmount));
                                        }
                                        else {
                                            hashMapAmount.put(monthsList[monthInt], exchangeAmount);
                                        }
                                    }
                                }
                            }

                            if(hashMapAmount.size() > 0) {
                                for(Map.Entry<String, Double> pair : hashMapAmount.entrySet()) {
                                    Log.d(pair.getKey(), "" + pair.getValue());
                                }

                                createBarChart(userData, hashMapAmount, totalAmount, Integer.parseInt(today.substring(today.indexOf('-') + 1, today.lastIndexOf('-'))), "Expense", Arrays.asList(GlobalConstants.getMonthsArray()), "monthly");
                            }
                            else {
                                displayNoTransactionsText();
                            }
                        }
                        else {
                            displayNoTransactionsText();
                        }
                        break;
                }
                break;
            case "Income":
                switch (graphType) {
                    case -1:
                        if(transactionList.length > 0) {
                            for (Transaction transaction : transactionList) {
                                amount = transaction.getAmount();
                                if (amount > 0) {
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
                                createPieChart(userData, hashMapAmount, totalAmount, linearLayoutLegendEntry);
                            }
                            else {
                                displayNoTransactionsText();
                            }
                        }
                        else {
                            displayNoTransactionsText();
                        }
                        break;
                    case -2:
                        if(transactionList.length > 0) {
                            //add all the weeks to the HashMap first
                            for(String weekDay: weekList) {
                                hashMapAmount.put(weekDay, 0.0);

                                if(!foundDay) {
                                    dayNumberOfWeek ++;//starts at 0.
                                }

                                if(todayWeekDayFormat.equals(weekDay)) {
                                    foundDay = true;
                                }
                            }

                            for (Transaction transaction : transactionList) {
                                amount = transaction.getAmount();

                                if(amount > 0) {
                                    date = transaction.getDate();
                                    locale = transaction.getLocale();
                                    exchangeAmount = currencyExchange(userData.get("locale"), locale, amount);

                                    monthInt = Integer.parseInt(date.substring(date.indexOf('-')+1, date.lastIndexOf('-'))) -1;
                                    dayInt = Integer.parseInt(date.substring(date.lastIndexOf('-')+1, date.length()));
                                    dayOfWeek = GlobalConstants.getMonthsArray()[monthInt] + " " + dayInt;
                                    totalAmount += exchangeAmount;

                                    //specifically looking at the Month - Day
                                    if(hashMapAmount.containsKey(dayOfWeek)) {
                                        hashMapAmount.put(dayOfWeek, (hashMapAmount.get(dayOfWeek) + exchangeAmount));
                                    }
                                    else {
                                        hashMapAmount.put(dayOfWeek, exchangeAmount);
                                    }
                                }
                            }

                            if(hashMapAmount.size() > 0) {
                                for(Map.Entry<String, Double> pair : hashMapAmount.entrySet()) {
                                    Log.d(pair.getKey(), "" + pair.getValue());
                                }

                                createBarChart(userData, hashMapAmount, totalAmount, dayNumberOfWeek, "Income", Arrays.asList(weekList), "weekly");
                            }
                            else {
                                displayNoTransactionsText();
                            }
                        }
                        else {
                            displayNoTransactionsText();
                        }
                        break;
                    case -3:
                        if(transactionList.length > 0) {
                            //add all the months to the HashMap first
                            for(String month: monthsList) {
                                hashMapAmount.put(month, 0.0);
                            }

                            for (Transaction transaction : transactionList) {
                                amount = transaction.getAmount();
                                type = transaction.getType();

                                if(amount > 0) {
                                    if(transactionTypePosition == 0 || (transactionTypePosition > 0 && type.equals(GlobalConstants.getTransactionTypeList()[transactionTypePosition -1]))) {
                                        date = transaction.getDate();
                                        locale = transaction.getLocale();
                                        exchangeAmount = currencyExchange(userData.get("locale"), locale, amount);

                                        totalAmount += exchangeAmount;

                                        //specifically looking at the month part of the string
                                        monthInt = Integer.parseInt(date.substring(date.indexOf('-') + 1, date.lastIndexOf('-'))) -1;

                                        if(hashMapAmount.containsKey(monthsList[monthInt])) {
                                            hashMapAmount.put(monthsList[monthInt], (hashMapAmount.get(monthsList[monthInt]) + exchangeAmount));
                                        }
                                        else {
                                            hashMapAmount.put(monthsList[monthInt], exchangeAmount);
                                        }
                                    }
                                }
                            }

                            if(hashMapAmount.size() > 0) {
                                for(Map.Entry<String, Double> pair : hashMapAmount.entrySet()) {
                                    Log.d(pair.getKey(), "" + pair.getValue());
                                }

                                createBarChart(userData, hashMapAmount, totalAmount, Integer.parseInt(today.substring(today.indexOf('-') + 1, today.lastIndexOf('-'))), "Income", Arrays.asList(GlobalConstants.getMonthsArray()), "monthly");
                            }
                            else {
                                displayNoTransactionsText();
                            }
                        }
                        else {
                            displayNoTransactionsText();
                        }
                        break;
                }
                break;
            case "Cash Flow":
                //only one report
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

                        createPieChart(userData, hashMapAmount, totalAmount, linearLayoutLegendEntry);
                    }
                    else {
                        displayNoTransactionsText();
                    }
                }
                else {
                    displayNoTransactionsText();
                }
                break;
            case "Balance":
                //only one report
                if(transactionList.length > 0) {
                    //add all the weeks to the HashMap first
                    //database ordered by date! so the order is least to greatest
                    //earliest date in the set is at the end
                    //this the line charts X-Axis
                    final String endDate = transactionList[0].getDate();
                    date = transactionList[transactionList.length-1].getDate();

                    final ArrayList<String> calendarsList = new ArrayList<>();

                    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                    final Calendar calendar = Calendar.getInstance();

                    try {
                        calendar.setTime(simpleDateFormat.parse(date));
                    }
                    catch (Exception exception) {
                        exception.printStackTrace();
                    }

                    while(date.compareTo(endDate) <= 0) {
                        //got the date in string format, parse it and add it the ArrayList
                        date = simpleDateFormat.format(calendar.getTime());

                        monthInt = Integer.parseInt(date.substring(date.indexOf('-')+1, date.lastIndexOf('-'))) -1;
                        dayInt = Integer.parseInt(date.substring(date.lastIndexOf('-')+1, date.length()));
                        dayOfWeek = GlobalConstants.getMonthsArray()[monthInt] + " " + dayInt;

                        calendarsList.add(dayOfWeek);

                        //move ahead a day
                        calendar.add(Calendar.DATE, 1);

                        Log.d("Day", dayOfWeek);
                    }

                    calendarsList.remove(calendarsList.size()-1);

                    final HashMap<String, Double> hashMapIncome = new HashMap<>();
                    final HashMap<String, Double> hashMapExpense = new HashMap<>();

                    double totalExpense = 0.0;
                    double totalIncome = 0.0;

                    for (Transaction transaction : transactionList) {
                        amount = transaction.getAmount();

                        date = transaction.getDate();
                        locale = transaction.getLocale();
                        exchangeAmount = currencyExchange(userData.get("locale"), locale, amount);

                        monthInt = Integer.parseInt(date.substring(date.indexOf('-')+1, date.lastIndexOf('-'))) -1;
                        dayInt = Integer.parseInt(date.substring(date.lastIndexOf('-')+1, date.length()));
                        dayOfWeek = GlobalConstants.getMonthsArray()[monthInt] + " " + dayInt;

                        if(amount > 0) {
                            //income
                            totalIncome += exchangeAmount;

                            if(hashMapIncome.containsKey(dayOfWeek)) {
                                hashMapIncome.put(dayOfWeek, (hashMapIncome.get(dayOfWeek) + exchangeAmount));
                            }
                            else {
                                hashMapIncome.put(dayOfWeek, exchangeAmount);
                            }
                        }
                        else if(amount < 0) {
                            //expense
                            totalExpense += exchangeAmount;

                            if(hashMapExpense.containsKey(dayOfWeek)) {
                                hashMapExpense.put(dayOfWeek, (hashMapExpense.get(dayOfWeek) + exchangeAmount));
                            }
                            else {
                                hashMapExpense.put(dayOfWeek, exchangeAmount);
                            }
                        }
                    }

                    if(hashMapExpense.size() > 0 || hashMapIncome.size() > 0) {
                        Log.d("Expense", "Expense");
                        for(Map.Entry<String, Double> pair : hashMapExpense.entrySet()) {
                            Log.d(pair.getKey(), "" + pair.getValue());
                        }

                        Log.d("Income", "Income");
                        for(Map.Entry<String, Double> pair : hashMapIncome.entrySet()) {
                            Log.d(pair.getKey(), "" + pair.getValue());
                        }

                        createLineChart(calendarsList, hashMapExpense, hashMapIncome, userData, totalExpense, totalIncome);
                    }
                    else {
                        displayNoTransactionsText();
                    }
                }
                else {
                    displayNoTransactionsText();
                }
                break;
        }
    }

    @SuppressLint("SimpleDateFormat")
    private Transaction[] updateTransactions(int userId, int accountId, int positionOfSelected) {
        //show transaction histories for each account

        Calendar calendar = Calendar.getInstance();
        Calendar start;
        Calendar end;

        String startDay = "";
        String endDay = "";

        switch (positionOfSelected) {
            case 0:
                //all periods
                startDay = "";
                endDay = "";
                break;
            case 1:
                //this month
                start = (Calendar) calendar.clone();
                start.set(Calendar.DAY_OF_MONTH, start.getActualMinimum(Calendar.DAY_OF_MONTH));

                end = (Calendar) start.clone();
                end.add(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH)-1);

                startDay = new SimpleDateFormat("yyyy-MM-dd").format(start.getTime());
                endDay = new SimpleDateFormat("yyyy-MM-dd").format(end.getTime());
                break;
            case 2:
                //this week
                start = (Calendar) calendar.clone();
                start.add(Calendar.DAY_OF_WEEK, start.getFirstDayOfWeek() - start.get(Calendar.DAY_OF_WEEK));

                end = (Calendar) start.clone();
                end.add(Calendar.DAY_OF_YEAR, 6);

                startDay = new SimpleDateFormat("yyyy-MM-dd").format(start.getTime());
                endDay = new SimpleDateFormat("yyyy-MM-dd").format(end.getTime());

                //build the array of week days - Month - Day
                final Calendar dayOfWeekCalender = (Calendar) start.clone();
                String day;
                int monthInt;
                int dayInt;

                for(int i = 0; i < 7; i ++) {
                    day = new SimpleDateFormat("yyyy-MM-dd").format(dayOfWeekCalender.getTime());

                    monthInt = Integer.parseInt(day.substring(day.indexOf('-')+1, day.lastIndexOf('-'))) -1;
                    dayInt = Integer.parseInt(day.substring(day.lastIndexOf('-')+1, day.length()));

                    weekList[i] = GlobalConstants.getMonthsArray()[monthInt] + " " + dayInt;

                    dayOfWeekCalender.add(Calendar.DAY_OF_WEEK, 1);
                }

                break;
            case 3:
                //today
                startDay = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
                endDay = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
                break;
        }

        return databaseHelper.getTransactionsRange(accountId, userId, startDay, endDay);
    }

    private double currencyExchange(String userLocale, String amountLocale, double amount) {
        final HashMap<String, String> hashMapData = GlobalConstants.getHashMapExchangeRates();
        final double currency2 = Double.parseDouble(hashMapData.get(userLocale));
        final double currency1 = Double.parseDouble(hashMapData.get(amountLocale));
        final double exchangeRate = currency1 / currency2;

        return Math.abs(amount / exchangeRate);
    }

    @SuppressLint("DefaultLocale")
    public void createLineChart(final List<String> daysList, HashMap<String, Double> hashMapExpense, HashMap<String, Double> hashMapIncome, HashMap<String, String> userData, double totalExpense, double totalIncome) {
        final LineChart lineChart = new LineChart(getContext());

        final List<Entry> entriesExpense = new ArrayList<>();
        final List<Entry> entriesIncome = new ArrayList<>();

        final int colorPalette[] = GlobalConstants.getColorPalette();

        //need a minimum of two entries or else there's massive issues (crashing)s
        if(daysList.size() == 1) {
            daysList.add(daysList.get(0));
        }

        Log.d("Month Size", "" + daysList.size());

        int j = -1;//last valid entry

        for(int i = 0; i < daysList.size(); i++) {
            Log.d("keysList: " + i, daysList.get(i));
            if(hashMapExpense.containsKey(daysList.get(i))) {
                entriesExpense.add(new Entry(i, hashMapExpense.get(daysList.get(i)).floatValue()));
                j = i;
            }
            else {
                if(j > 0) {
                    //add the last valid entry
                    entriesExpense.add(new Entry(i, hashMapExpense.get(daysList.get(j)).floatValue()));
                }
           }
        }

        j = -1;

        for(int i = 0; i < daysList.size(); i++) {
            Log.d("keysList: " + i, daysList.get(i));
            if(hashMapIncome.containsKey(daysList.get(i))) {
                entriesIncome.add(new Entry(i, hashMapIncome.get(daysList.get(i)).floatValue()));
                j = i;
            }
            else {
                if(j > 0) {
                    //add the last valid entry
                    entriesIncome.add(new Entry(i, hashMapIncome.get(daysList.get(j)).floatValue()));
                }
            }
        }

        final LineDataSet lineDataSetExpense = new LineDataSet(entriesExpense, "Expense");
        lineDataSetExpense.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSetExpense.setCircleColor(Color.BLACK);
        lineDataSetExpense.setColor(colorPalette[0]);
        lineDataSetExpense.setLineWidth(3);
        lineDataSetExpense.setValueTextSize(18);

        final LineDataSet lineDataSetIncome = new LineDataSet(entriesIncome, "Income");
        lineDataSetIncome.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSetIncome.setCircleColor(Color.BLACK);
        lineDataSetIncome.setColor(colorPalette[1]);
        lineDataSetIncome.setLineWidth(3);
        lineDataSetIncome.setValueTextSize(18);

        List<ILineDataSet> dataSets = new ArrayList<>();

        if(entriesExpense.size() > 0) {
            dataSets.add(lineDataSetExpense);
        }

        if(entriesIncome.size() > 0) {
            dataSets.add(lineDataSetIncome);
        }

        final LineData data = new LineData(dataSets);

        lineChart.setData(data);
        lineChart.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1500));
        lineChart.setScaleEnabled(false);
        lineChart.setTouchEnabled(false);

        final XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(18);
        xAxis.setLabelRotationAngle(270);
        xAxis.setLabelCount(daysList.size());
        xAxis.setAxisMaximum(daysList.size()-1);

        xAxis.setValueFormatter(new IAxisValueFormatter() {
            public String getFormattedValue(float value, AxisBase axis) {
                return daysList.get((int) value);
            }
        });

        final YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setEnabled(false);

        final YAxis yAxisRight = lineChart.getAxisRight();
        yAxisRight.setEnabled(false);

        //shows the name of the chart in the lower corner
        final Description description = new Description();
        description.setText(" ");
        description.setTextSize(18);
        lineChart.setDescription(description);

        final Legend legend = lineChart.getLegend();
        legend.setTextSize(22);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setEnabled(false);

        lineChart.invalidate();

        linearLayoutGraphArea.addView(lineChart);

        //create my own legend
        final TextView textViewTitle = new TextView(getContext());
        textViewTitle.setText(getContext().getResources().getString(R.string.legend_title));
        textViewTitle.setTextSize(22);
        textViewTitle.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        textViewTitle.setPadding(15, 100, 15, 15);

        linearLayoutGraphArea.addView(textViewTitle);

        final LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        LinearLayout view = (LinearLayout) layoutInflater.inflate(R.layout.layout_graph_legend, null);

        TextView textViewLegend = view.findViewById(R.id.textViewLegend);
        textViewLegend.setText("Expense");

        ImageView imageViewLegend = view.findViewById(R.id.imageViewLegend);
        imageViewLegend.setColorFilter(colorPalette[0], PorterDuff.Mode.SRC_ATOP);

        TextView textViewPercent = view.findViewById(R.id.textViewPercent);
        String text = userData.get("symbol") + String.format("%.2f", totalExpense);
        textViewPercent.setText(text);

        linearLayoutGraphArea.addView(view);

        view = (LinearLayout) layoutInflater.inflate(R.layout.layout_graph_legend, null);

        textViewLegend = view.findViewById(R.id.textViewLegend);
        textViewLegend.setText("Income");

        imageViewLegend = view.findViewById(R.id.imageViewLegend);
        imageViewLegend.setColorFilter(colorPalette[1], PorterDuff.Mode.SRC_ATOP);

        textViewPercent = view.findViewById(R.id.textViewPercent);
        text = userData.get("symbol") + String.format("%.2f", totalIncome);
        textViewPercent.setText(text);

        linearLayoutGraphArea.addView(view);
    }

    @SuppressLint("DefaultLocale")
    public void createBarChart(HashMap<String, String> userData, HashMap<String, Double> hashMapAmount, double totalAmount, int divisor, String incomeExpense, final List<String> keysList, String type) {
        final BarChart barChart = new BarChart(getContext());
        final List<BarEntry> entries = new ArrayList<>();
        final int colorPalette[] = GlobalConstants.getColorPalette();

        Log.d("Month Size", "" + keysList.size());

        for(int i = 0; i < keysList.size(); i++) {
            Log.d("keysList: " + i, keysList.get(i));
            entries.add(new BarEntry(i, hashMapAmount.get(keysList.get(i)).floatValue()));
        }

        //shows the name of the chart in the lower corner
        final Description description = new Description();
        description.setText(" ");
        description.setTextSize(18);

        final BarDataSet set = new BarDataSet(entries, " ");
        set.setColors(colorPalette);

        final BarData data = new BarData(set);
        data.setValueTextSize(18);

        barChart.setDescription(description);
        barChart.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1500));
        barChart.setData(data);
        barChart.setFitBars(true);
        barChart.setScaleEnabled(false);
        barChart.setTouchEnabled(false);

        final XAxis xAxis = barChart.getXAxis();
        xAxis.setTextSize(18);
        xAxis.setLabelRotationAngle(270);
        xAxis.setLabelCount(keysList.size());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMaximum(keysList.size()-1);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            public String getFormattedValue(float value, AxisBase axis) {
                return keysList.get((int) value);
            }
        });

        final YAxis yAxisLeft = barChart.getAxisLeft();
        yAxisLeft.setEnabled(false);

        final YAxis yAxisRight = barChart.getAxisRight();
        yAxisRight.setEnabled(false);

        final Legend legend = barChart.getLegend();
        legend.setTextSize(22);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setEnabled(false);

        barChart.invalidate();

        //set the average amount
        String average = "";

        if(incomeExpense.equals("Income")) {
            //dealing with income
            average = "Average " + type + " income is " + userData.get("symbol") + String.format("%.2f", totalAmount / divisor);
        }
        else if(incomeExpense.equals("Expense")) {
            //dealing with expense
            average = "Average " + type + " expense is " + userData.get("symbol") + String.format("%.2f", totalAmount / divisor);
        }

        final TextView textViewTitle = new TextView(getContext());
        textViewTitle.setText(average);
        textViewTitle.setTextSize(22);
        textViewTitle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textViewTitle.setPadding(15, 15, 15, 15);

        linearLayoutGraphArea.addView(textViewTitle);
        linearLayoutGraphArea.addView(barChart);
    }

    public void createPieChart(HashMap<String, String> userData, HashMap<String, Double> hashMapAmount, double totalAmount, ArrayList<LinearLayout> linearLayoutLegendEntry) {
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
            entries.add(new PieEntry((float) (pair.getValue() / totalAmount), pair.getKey()));

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

        linearLayoutGraphArea.addView(pieChart);
        linearLayoutGraphArea.addView(textViewTitle);
        linearLayoutGraphArea.addView(linearLayoutLegend);
    }

    public void displayNoTransactionsText() {
        //no transactions, show text;
        final TextView textView = new TextView(getContext());
        textView.setTextSize(18);
        textView.setTextColor(Color.BLACK);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setText("No Transactions \n Unable to Show Report");
        linearLayoutGraphArea.addView(textView);
    }
}