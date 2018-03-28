package com.mcmah113.mcmah113expensesiq;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

        final ArrayAdapter<String> arrayAdapterAccount = new ArrayAdapter<>(getContext(), R.layout.layout_spinner, spinnerString);
        final ArrayAdapter<String> arrayAdapterPeriod = new ArrayAdapter<>(getContext(), R.layout.layout_spinner, GlobalConstants.getTransactionPeriods());

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

                updateTransactions(accountId, userId, spinnerPeriod.getSelectedItemPosition());
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerPeriod.setAdapter(arrayAdapterPeriod);
        spinnerPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTransactions(accountId, userId, spinnerPeriod.getSelectedItemPosition());
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if(getArguments() != null) {
            //coming from accounts, set the selected account as the default
            accountId = getArguments().getInt("accountId");

            //setting the default selection as the accountId passed in
            //accountsList and spinnerString[] have the same indexes
            for(int i = 0; i < accountsList.length; i ++) {
                if(accountId == accountsList[i].getId()) {
                    spinnerAccount.setSelection(i);
                    break;
                }
            }
        }
        else {
            //no Id passed in, therefore set it to the first id in the list
            accountId = -1;
        }

        updateTransactions(accountId, userId, 0);
    }

    private void updateTransactions(int accountId, int userId, int positionOfSelected) {
        //show transaction histories for each account
        Date dateToday = Calendar.getInstance().getTime();
        String today = new SimpleDateFormat("yyyy-MM-dd").format(dateToday);

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
                int month = Integer.parseInt(today.substring(today.indexOf('-') +1,today.lastIndexOf('-')));
                int maxDate = Calendar.getInstance().getActualMaximum(month);
                startDay = "";
                endDay = "";
                break;
            case 2:
                //this week
                startDay = "";
                endDay = "";
                Calendar.getInstance().getFirstDayOfWeek();
                break;
            case 3:
                //today
                dateToday = Calendar.getInstance().getTime();
                startDay = new SimpleDateFormat("yyyy-MM-dd").format(dateToday);
                endDay = "";
                break;
        }

        TransactionsAdapter transactionsAdapter = new TransactionsAdapter(getContext(), databaseHelper.getTransactionsRange(accountId, userId, startDay, endDay));
        listViewTransactions.setAdapter(transactionsAdapter);
    }
}