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
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

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

        int monthInt;

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
                        break;
                    case -3:
                        //add all the months to the HashMap first
                        for(String month: monthsList) {
                            hashMapAmount.put(month, 0.0);
                        }

                        if(transactionList.length > 0) {
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

                                createBarChart(userData, hashMapAmount, totalAmount, Integer.parseInt(today.substring(today.indexOf('-') + 1, today.lastIndexOf('-'))), "Expense");
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


                        break;
                    case -3:
                        //add all the months to the HashMap first
                        for(String month: monthsList) {
                            hashMapAmount.put(month, 0.0);
                        }

                        if(transactionList.length > 0) {
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

                                createBarChart(userData, hashMapAmount, totalAmount, Integer.parseInt(today.substring(today.indexOf('-') + 1, today.lastIndexOf('-'))), "Income");
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
    public void createBarChart(HashMap<String, String> userData, HashMap<String, Double> hashMapAmount, double totalAmount, int currentMonth, String incomeExpense) {
        final ArrayList<String> monthsList = new ArrayList<>();
        monthsList.addAll(Arrays.asList(GlobalConstants.getMonthsArray()));

        final List<BarEntry> entries = new ArrayList<>();
        final int colorPalette[] = GlobalConstants.getColorPalette();

        Log.d("Month Size", "" + monthsList.size());

        for(int i = 0; i < monthsList.size(); i++) {
            Log.d("Month" + i, monthsList.get(i));
            entries.add(new BarEntry(i, hashMapAmount.get(monthsList.get(i)).floatValue()));
        }

        //shows the name of the chart in the lower corner
        final Description description = new Description();
        description.setText(" ");
        description.setTextSize(18);

        BarDataSet set = new BarDataSet(entries, " ");
        set.setColors(colorPalette);

        BarData data = new BarData(set);
        data.setValueTextSize(18);

        BarChart barChart = new BarChart(getContext());
        barChart.setDescription(description);
        barChart.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1500));
        barChart.setData(data);
        barChart.setFitBars(true);
        barChart.setScaleEnabled(false);
        barChart.setTouchEnabled(false);

        final XAxis xAxis = barChart.getXAxis();
        xAxis.setTextSize(18);
        xAxis.setLabelRotationAngle(270);
        xAxis.setLabelCount(monthsList.size());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMaximum(monthsList.size()-1);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            public String getFormattedValue(float value, AxisBase axis) {
                return monthsList.get((int) value);
            }
        });

        YAxis yAxisLeft = barChart.getAxisLeft();
        yAxisLeft.setEnabled(false);
        YAxis yAxisRight = barChart.getAxisRight();
        yAxisRight.setEnabled(false);

        final Legend legend = barChart.getLegend();
        legend.setTextSize(22);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setEnabled(false);


        //set the average amount
        String average = "";

        if(incomeExpense.equals("Income")) {
            //dealing with income
            average = "Average monthly income is " + userData.get("symbol") + String.format("%.2f", totalAmount / currentMonth);
        }
        else if(incomeExpense.equals("Expense")) {
            //dealing with expense
            average = "Average monthly expense is " + userData.get("symbol") + String.format("%.2f", totalAmount / currentMonth);
        }

        TextView textViewTitle = new TextView(getContext());
        textViewTitle.setText(average);
        textViewTitle.setTextSize(22);
        textViewTitle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textViewTitle.setPadding(15, 15, 15, 15);

        linearLayoutGraphArea.addView(textViewTitle);
        linearLayoutGraphArea.addView(barChart);
    }

    public void createPieChart(HashMap<String, String> userData, HashMap<String, Double> hashMapAmount, double totalAmount, ArrayList<LinearLayout> linearLayoutLegendEntry) {
        //get the text of the inner pie that represents the
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

        final PieChart pieChart = new PieChart(getContext());
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

        linearLayoutGraphArea.addView(pieChart);

        //create my own legend
        TextView textViewTitle = new TextView(getContext());
        textViewTitle.setText(getContext().getResources().getString(R.string.legend_title));
        textViewTitle.setTextSize(22);
        textViewTitle.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        textViewTitle.setPadding(15, 15, 15, 15);

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