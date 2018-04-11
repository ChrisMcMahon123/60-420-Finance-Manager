package com.mcmah113.mcmah113expensesiq;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TransactionsFragment extends Fragment {
    private DatabaseHelper databaseHelper;
    private ListView listViewTransactions;

    private int accountId;

    public TransactionsFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transactions, container, false);
    }

    public void onViewCreated(View view, Bundle bundle) {
        databaseHelper = new DatabaseHelper(getContext());

        final int userId = Overview.getUserId();

        final Account[] accountsList = databaseHelper.getAccountList(userId);
        final String spinnerString[] = new String[accountsList.length +1];

        //creating array that will be used for the spinners
        spinnerString[0] = "All Accounts";

        for(int i = 0; i < accountsList.length; i ++) {
            spinnerString[i+1] = accountsList[i].getName() + " (" + accountsList[i].getLocale() + ")";
        }

        listViewTransactions = view.findViewById(R.id.listviewTransactions);

        final ArrayAdapter<String> arrayAdapterAccount = new ArrayAdapter<>(getContext(), R.layout.layout_spinner_alt_text, spinnerString);
        final ArrayAdapter<String> arrayAdapterPeriod = new ArrayAdapter<>(getContext(), R.layout.layout_spinner_alt_text, GlobalConstants.getTransactionPeriods());

        final Spinner spinnerAccount = view.findViewById(R.id.spinnerAccount);
        final Spinner spinnerPeriod = view.findViewById(R.id.spinnerPeriod);

        spinnerAccount.setAdapter(arrayAdapterAccount);
        spinnerAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) {
                    //all has been selected
                    accountId = -1;
                }
                else {
                    //get account id from real account
                    accountId = accountsList[position-1].getId();
                }

                updateTransactions(userId, accountId, spinnerPeriod.getSelectedItemPosition());
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerPeriod.setAdapter(arrayAdapterPeriod);
        spinnerPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTransactions(userId, accountId, spinnerPeriod.getSelectedItemPosition());
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        if(getArguments() != null) {
            //coming from accounts, set the selected account as the default
            accountId = getArguments().getInt("accountId");

            if(accountId > 0) {
                //setting the default selection, off by one with 'All Accounts' being at 0
                for(int i = 0; i < accountsList.length; i ++) {
                    if(accountId == accountsList[i].getId()) {
                        spinnerAccount.setSelection(i+1);
                        break;
                    }
                }
            }
        }
        else {
            //no Id passed in, therefore set it to the first id in the list
            accountId = -1;
            spinnerAccount.setSelection(0);
        }

        updateTransactions(userId, accountId, 0);
    }

    @SuppressLint("SimpleDateFormat")
    private void updateTransactions(int userId, int accountId, int positionOfSelected) {
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

        Transaction transactionList[] = databaseHelper.getTransactionsRange(accountId, userId, startDay, endDay);
        final TransactionsAdapter transactionsAdapter = new TransactionsAdapter(getContext(), transactionList);
        listViewTransactions.setAdapter(transactionsAdapter);
    }
}