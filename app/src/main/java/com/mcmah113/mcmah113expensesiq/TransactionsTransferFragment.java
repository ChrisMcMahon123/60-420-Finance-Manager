package com.mcmah113.mcmah113expensesiq;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class TransactionsTransferFragment extends Fragment {
    private double exchangeRate;

    private TextView textViewRate;
    private TextView textViewRefresh;

    public TransactionsTransferFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transactions_transfer, container, false);
    }

    public void onViewCreated(View view, Bundle bundle) {
        final DatabaseHelper databaseHelper = new DatabaseHelper(getContext());

        final int userId = Overview.getUserId();
        final int accountId = getArguments().getInt("accountId");

        final Account[] accountsList = databaseHelper.getAccountList(userId);
        final String spinnerString[] = new String[accountsList.length];

        //creating array that will be used for the spinners
        for(int i = 0; i < accountsList.length; i ++) {
            spinnerString[i] = accountsList[i].getName() + " (" + accountsList[i].getLocale() + ")";
        }

        //setting up TextView properties
        textViewRate = view.findViewById(R.id.textViewExchangeRate);
        textViewRefresh = view.findViewById(R.id.textViewRefreshDate);

        //setting up the editText properties
        final EditText editTextAmount = view.findViewById(R.id.editTextAmount);
        final EditText editTextNote = view.findViewById(R.id.editTextNote);

        //setting the spinners properties
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), R.layout.layout_spinner, spinnerString);

        final Spinner spinnerAccountFrom = view.findViewById(R.id.spinnerFromAccount);
        final Spinner spinnerAccountTo = view.findViewById(R.id.spinnerToAccount);

        spinnerAccountFrom.setAdapter(arrayAdapter);
        spinnerAccountFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateExchangeRate(spinnerAccountFrom.getSelectedItem().toString(),
                                   spinnerAccountTo.getSelectedItem().toString());
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerAccountTo.setAdapter(arrayAdapter);
        spinnerAccountTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateExchangeRate(spinnerAccountFrom.getSelectedItem().toString(),
                                   spinnerAccountTo.getSelectedItem().toString());
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //setting the default selection as the accountId passed in
        //accountsList and spinnerString[] have the same indexes
        for(int i = 0; i < accountsList.length; i ++) {
            if(accountId == accountsList[i].getId()) {
                spinnerAccountFrom.setSelection(i);
                spinnerAccountTo.setSelection(i);
                break;
            }
        }

        //make the default call to get the currency conversion
        updateExchangeRate(spinnerAccountFrom.getSelectedItem().toString(),
                           spinnerAccountTo.getSelectedItem().toString());

        //set sign up button properties
        final CustomOnTouchListener onTouchListener = new CustomOnTouchListener(getResources().getColor(R.color.colorPrimaryDark, getContext().getTheme()));

        final Button buttonTransaction = view.findViewById(R.id.buttonTransaction);
        buttonTransaction.setOnTouchListener(onTouchListener);//ignore this warning...
        buttonTransaction.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!spinnerAccountFrom.getSelectedItem().toString().equals(spinnerAccountTo.getSelectedItem().toString())) {
                    //2 different accounts selected, good
                    if(!editTextAmount.getText().toString().isEmpty()) {
                        //amount is filled out, try and convert it
                        try {
                            double amount = Double.parseDouble(editTextAmount.getText().toString());
                            int accountFromId = -1;
                            int accountToId = -1;

                            //find out which accounts were selected and get their Ids
                            for(int i = 0; i < spinnerString.length; i ++) {
                                if(spinnerString[i].equals(spinnerAccountFrom.getSelectedItem())) {
                                    accountFromId = accountsList[i].getId();
                                    break;
                                }
                            }

                            for(int i = 0; i < spinnerString.length; i ++) {
                                if(spinnerString[i].equals(spinnerAccountTo.getSelectedItem())) {
                                    accountToId = accountsList[i].getId();
                                    break;
                                }
                            }

                            final Bundle args = new Bundle();
                            args.putInt("accountFromId", accountFromId);
                            args.putInt("accountToId", accountToId);
                            args.putDouble("amount", amount);
                            args.putDouble("exchangeRate", exchangeRate);
                            args.putString("note", editTextNote.toString());

                            TransactionsTransferDialogFragment dialogFragment = new TransactionsTransferDialogFragment();
                            dialogFragment.setArguments(args);
                            dialogFragment.show(getFragmentManager(), "Account Dialog");
                        }
                        catch(Exception exception) {
                            Toast.makeText(getContext(), "Invalid Money input", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(getContext(), "(*) Required fields missing", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    //same account selected, don;t let them transfer funds
                    Toast.makeText(getContext(), "Cannot transfer to the same account", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void updateExchangeRate(String spinnerFrom, String spinnerTo) {
        //only want the locale for the two bank accounts
        String localeFrom = spinnerFrom.substring(spinnerFrom.indexOf('(') +1,spinnerFrom.indexOf(')'));
        String localeTo = spinnerTo.substring(spinnerTo.indexOf('(') +1,spinnerTo.indexOf(')'));
        String refreshDate;
        String rateString;

        if(!localeFrom.equals(localeTo)) {
            try {
                //make the API request to get the information
                HashMap<String, String> dataAPI = new FixerCurrencyAPI().execute(localeFrom, localeTo).get();

                refreshDate = "Refreshed on " + dataAPI.get("Date");
                //rates are in EUR, common base, division to find
                //cross currency ratio
                final double currency1 = Double.parseDouble(dataAPI.get(localeFrom));
                final double currency2 = Double.parseDouble(dataAPI.get(localeTo));
                exchangeRate = currency1 / currency2;

                rateString = String.format("%.2f", exchangeRate) + " " + localeFrom + " = 1.00 " + localeTo;
            } catch (Exception exception) {
                Log.d("Failed", "Couldn't get currency rate");
                exception.printStackTrace();
                exchangeRate = 1.00;
                rateString = "Fixer API is unavailable, assuming 1:1 Ratio";
                refreshDate = "Unavailable";
            }
        }
        else {
            //save an API call and speed the program
            //same format as the API call
            rateString = "1.00 " + localeFrom + " = 1.00 " + localeTo;

            final Date currentTime = Calendar.getInstance().getTime();
            refreshDate = "Refreshed on " + new SimpleDateFormat("yyyy-MM-dd").format(currentTime);
        }

        textViewRate.setText(rateString);
        textViewRefresh.setText(refreshDate);
    }
}