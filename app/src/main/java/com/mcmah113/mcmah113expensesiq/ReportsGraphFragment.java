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

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

                updateGraph(userId, accountId, spinnerPeriod.getSelectedItemPosition());
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerPeriod.setAdapter(arrayAdapterPeriod);
        spinnerPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateGraph(userId, accountId, spinnerPeriod.getSelectedItemPosition());
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        graphType = getArguments().getInt("accountId");

        switch (graphType) {
            case -1:


                break;
            case -2:
                spinnerPeriod.setVisibility(View.GONE);
                textViewPeriod.setVisibility(View.GONE);
                break;
            case -3:


                break;
        }

        linearLayoutGraphArea = view.findViewById(R.id.linearLayoutGraphArea);
    }

    @SuppressLint("SimpleDateFormat")
    public void updateGraph(int userId, int accountId, int positionOfSelected) {
        linearLayoutGraphArea.removeAllViewsInLayout();

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
                end.add(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH) - 1);

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

        final Transaction transactionList[] = databaseHelper.getTransactionsRange(accountId, userId, startDay, endDay);

        final String reportType = getArguments().getString("callFrom");

        //now that the inputs have been resolved and the data has been retrieved
        //determine which graph to show
        switch (reportType) {
            case "Expense":
                switch (graphType) {
                    case -1:
                        final HashMap<String, Double> hashMapCount = new HashMap<>();
                        final HashMap<String, Double> hashMapAmount = new HashMap<>();
                        String type;
                        String locale;
                        double amount;
                        int numExpenses = 0;

                        if(transactionList.length > 0) {
                            for (Transaction transaction : transactionList) {
                                amount = transaction.getAmount();
                                if (amount < 0) {
                                    numExpenses ++;
                                    type = transaction.getType();
                                    locale = transaction.getLocale();

                                    if(hashMapCount.containsKey(type)) {
                                        //that specific type already exists, increment it
                                        hashMapCount.put(type, (hashMapCount.get(type) + 1));
                                    }
                                    else {
                                        //type is unique, default value is 1 / n x 100 = 1%
                                        hashMapCount.put(type, 1.0);
                                    }

                                    if (hashMapAmount.containsKey(locale)) {
                                        //currency is not unique
                                        hashMapAmount.put(locale, (hashMapAmount.get(locale) + amount));
                                    }
                                    else {
                                        //currency is unique
                                        hashMapAmount.put(locale, amount);
                                    }
                                }
                            }

                            final String totalTransactions = calculateTotalTransactionCost(userId, hashMapAmount);
                            final List<PieEntry> entries = new ArrayList<>();

                            //add the HashMap values to the list
                            for (Map.Entry<String, Double> pair : hashMapCount.entrySet()) {
                                Log.d(pair.getKey(), "%" + String.format("%.2f", pair.getValue() / numExpenses));
                                entries.add(new PieEntry((pair.getValue().floatValue() / numExpenses), pair.getKey()));
                            }

                            final PieDataSet set = new PieDataSet(entries, " ");
                            int colorPalette[] = ColorTemplate.COLORFUL_COLORS;
                            set.setColors(colorPalette);
                            set.setValueTextSize(18);
                            set.setDrawValues(false);
                            //the % that shows in each Pie slice
                            final PieData data = new PieData(set);
                            data.setValueTextColor(Color.WHITE);
                            data.setValueTextSize(18);

                            //shows the name of the chart in the lower corner
                            final Description description = new Description();
                            description.setText(" ");
                            description.setTextSize(18);

                            final PieChart pieChart = new PieChart(getContext());
                            final Legend legend = pieChart.getLegend();
                            legend.setTextSize(22);
                            legend.setOrientation(Legend.LegendOrientation.VERTICAL);
                            legend.setEnabled(false);
                            pieChart.setDrawEntryLabels(false);
                            pieChart.setData(data);
                            pieChart.setUsePercentValues(true);
                            pieChart.setCenterTextColor(Color.BLACK);
                            pieChart.setCenterText(totalTransactions);
                            pieChart.setCenterTextSize(18);
                            pieChart.setDescription(description);
                            pieChart.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1500));

                            linearLayoutGraphArea.addView(pieChart);

                            //create my own legend
                            TextView textViewTitle = new TextView(getContext());
                            textViewTitle.setText("Legend");
                            textViewTitle.setTextSize(22);
                            textViewTitle.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                            textViewTitle.setPadding(15,15,15,15);

                            linearLayoutGraphArea.addView(textViewTitle);

                            ImageView imageViewLegend;
                            TextView textViewLegend;
                            TextView textViewPercent;

                            int i = 0;
                            for (Map.Entry<String, Double> pair : hashMapCount.entrySet()) {
                                //getting the layout from the fragment
                                final LayoutInflater layoutInflater = getActivity().getLayoutInflater();
                                final LinearLayout view = (LinearLayout) layoutInflater.inflate(R.layout.layout_graph_legend, null);

                                textViewLegend = view.findViewById(R.id.textViewLegend);
                                textViewLegend.setText(pair.getKey());

                                imageViewLegend = view.findViewById(R.id.imageViewLegend);
                                imageViewLegend.setColorFilter(colorPalette[i], PorterDuff.Mode.SRC_ATOP);

                                textViewPercent = view.findViewById(R.id.textViewPercent);
                                @SuppressLint("DefaultLocale") String text = "%" + String.format("%.0f",((pair.getValue()/ numExpenses) * 100));
                                textViewPercent.setText(text);

                                linearLayoutGraphArea.addView(view);

                                i ++;
                            }
                        }
                        else {
                            //no transactions, show text;
                            final TextView textView = new TextView(getContext());
                            textView.setTextSize(18);
                            textView.setTextColor(Color.BLACK);
                            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            textView.setText("No Transactions \n Unable to Show Report");
                            linearLayoutGraphArea.addView(textView);
                        }
                        break;
                    case -2:


                        break;
                    case -3:


                        break;
                }
                break;
            case "Income":
                switch (graphType) {
                    case -1:


                        break;
                    case -2:


                        break;
                    case -3:


                        break;
                }
                break;
            case "Cash Flow":
                //only one report

                break;
            case "Balance":
                //only one report

                break;
        }
    }

    private String calculateTotalTransactionCost(int userId, HashMap<String, Double> hashMapAmount) {
        HashMap<String, String> userData = databaseHelper.getUserSettings(userId);
        HashMap<String, String> hashMapData = new HashMap<>();
        boolean errorFlag;
        double currency2 = 0.0;
        double totalMoneyAmount = 0.0;

        if(hashMapAmount.size() == 0) {
            return "";
        }
        else {
            if (hashMapAmount.size() > 1 || (hashMapAmount.size() == 1 && !hashMapAmount.containsKey(userData.get("locale")))) {
                try {
                    //API call to get all currency exchange rates
                    if(!hashMapAmount.containsKey(userData.get("locale"))) {
                        hashMapAmount.put(userData.get("locale"),0.0);
                    }

                    hashMapData = new FixerCurrencyAPI().execute(hashMapAmount.keySet().toArray(new String[hashMapAmount.keySet().size()])).get();

                    currency2 = Double.parseDouble(hashMapData.get(userData.get("locale")));

                    errorFlag = false;
                }
                catch (Exception e) {
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

            if (!errorFlag) {
                //got results, find the net balance
                for (Map.Entry<String, Double> pair : hashMapAmount.entrySet()) {
                    //convert currency and add to total money
                    double value = pair.getValue();
                    double currency1 = Double.parseDouble(hashMapData.get(pair.getKey()));
                    double exchangeRate = currency1 / currency2;
                    totalMoneyAmount += (value / exchangeRate);
                }

                return String.format(userData.get("symbol") + "%.2f", Math.abs(totalMoneyAmount));
            }
            else {
                return String.format(userData.get("symbol") + "%.2f", Math.abs(hashMapAmount.get(userData.get("locale"))));
            }
        }
    }
}