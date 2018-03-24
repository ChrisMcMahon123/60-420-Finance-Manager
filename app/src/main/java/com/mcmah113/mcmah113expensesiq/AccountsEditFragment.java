package com.mcmah113.mcmah113expensesiq;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Currency;
import java.util.Locale;

public class AccountsEditFragment extends Fragment {
    //array holds the currency name and its symbol
    private static final String currencyArray[] = {
        Locale.US.getDisplayCountry() + " (" + Currency.getInstance(Locale.US) + ")",
        Locale.CANADA.getDisplayCountry() + " (" + Currency.getInstance(Locale.CANADA) + ")",
        Locale.JAPAN.getDisplayCountry() + " (" + Currency.getInstance(Locale.JAPAN) + ")",
        Locale.UK.getDisplayCountry() + " (" + Currency.getInstance(Locale.UK) + ")",
        Locale.FRANCE.getDisplayCountry() + " (" + Currency.getInstance(Locale.FRANCE) + ")",
        Locale.CHINA.getDisplayCountry() + " (" + Currency.getInstance(Locale.CHINA) + ")"
    };

    private static final String typesArray[] = {"Bank", "Cash"};

    //set the index of the spinners to 0 for the first array item
    private int typeIndex = 0;
    private int currencyIndex = 0;

    private Account account;

    public interface OnCompleteListener {
        void onCompleteAccountEdit();
    }

    public AccountsEditFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_accounts_edit, container, false);
    }

    public void onViewCreated(View view, Bundle bundle) {
        final DatabaseHelper databaseHelper = new DatabaseHelper(getContext());

        final int userId = Overview.getUserId();
        final int accountId = getArguments().getInt("accountId");

        final EditText editTextName = view.findViewById(R.id.editTextAccountName);
        final EditText editTextInitialBalance = view.findViewById(R.id.editTextAccountBalanceInitial);
        final EditText editTextCurrentBalance = view.findViewById(R.id.editTextAccountBalanceCurrent);
        final EditText editTextDescription = view.findViewById(R.id.editTextAccountDescription);
        final Button buttonInteract = view.findViewById(R.id.buttonAccountInteract);
        final CheckBox checkboxHiddenAccount = view.findViewById(R.id.checkboxHiddenAccount);

        //set spinner properties
        final ArrayAdapter arrayAdapterType = new ArrayAdapter<>(getContext(), R.layout.layout_spinner, typesArray);

        final Spinner spinnerType = view.findViewById(R.id.spinnerType);
        spinnerType.setAdapter(arrayAdapterType);
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                typeIndex = position;
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final ArrayAdapter<String> arrayAdapterCurrency = new ArrayAdapter<>(getContext(), R.layout.layout_spinner, currencyArray);

        final Spinner spinnerCurrency = view.findViewById(R.id.spinnerCurrency);
        spinnerCurrency.setAdapter(arrayAdapterCurrency);
        spinnerCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currencyIndex = position;
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if(accountId > 0) {
            //editing an already existing account
            account = databaseHelper.getAccountInfo(accountId, userId);
            editTextName.setText(account.getName());
            editTextInitialBalance.setText(Double.toString(account.getInitialBalance()));
            editTextCurrentBalance.setText(Double.toString(account.getCurrentBalance()));
            editTextDescription.setText(account.getDescription());
            checkboxHiddenAccount.setChecked(account.getHiddenFlag());

            //set default selected based on account object
            for(int i = 0; i < typesArray.length; i ++) {
                if(account.getType().equals(typesArray[i])) {
                    spinnerType.setSelection(i);
                    typeIndex = i;
                    break;
                }
            }

            //set default selected based on account object
            for(int i = 0; i < currencyArray.length; i ++) {
                if(currencyArray[i].contains(account.getLocale())) {
                    spinnerCurrency.setSelection(i);
                    currencyIndex = i;
                    break;
                }
            }
        }
        else {
            //creating a new account
            account = null;
        }

        //set sign up button properties
        final CustomOnTouchListener onTouchListener = new CustomOnTouchListener(getResources().getColor(R.color.colorPrimaryDark, getContext().getTheme()));
        buttonInteract.setOnTouchListener(onTouchListener);//ignore this warning...
        buttonInteract.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String name = editTextName.getText().toString();
                final String initialBalanceString = editTextInitialBalance.getText().toString();
                final String currentBalanceString = editTextCurrentBalance.getText().toString();
                final String description = editTextDescription.getText().toString();
                final String type = spinnerType.getItemAtPosition(typeIndex).toString();
                final String locale = spinnerCurrency.getItemAtPosition(currencyIndex).toString();

                boolean hiddenFlag;

                if(checkboxHiddenAccount.isChecked()) {
                    hiddenFlag = true;
                }
                else {
                    hiddenFlag = false;
                }

                try {
                    //check to see if valid numbers
                    double initialBalance = Double.parseDouble(initialBalanceString);
                    double currentBalance = Double.parseDouble(currentBalanceString);

                    if(initialBalance >= 0 && currentBalance >= 0) {
                        if(name.isEmpty()) {
                            Toast.makeText(getContext(), "(*) Fields are required", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            if(accountId > 0) {
                                //save changes to existing account
                                account.setName(name);
                                account.setType(type);
                                account.setLocale(locale);
                                account.setInitialBalance(initialBalance);
                                account.setCurrentBalance(currentBalance);
                                account.setDescription(description);
                                account.setHiddenFlag(hiddenFlag);

                                if (databaseHelper.updateAccount(userId, account)) {
                                    Toast.makeText(getContext(), "Saved account changes", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Failed to save account changes", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                //create a new account
                                if (databaseHelper.createAccount(userId, name, type, locale, initialBalance, description, hiddenFlag)) {
                                    Toast.makeText(getContext(), "Account successfully created", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Failed to create account", Toast.LENGTH_SHORT).show();
                                }
                            }

                            //return to the main activity which will redirect to Accounts fragment
                            final AccountsEditFragment.OnCompleteListener onCompleteListener = (AccountsEditFragment.OnCompleteListener) getActivity();
                            onCompleteListener.onCompleteAccountEdit();
                        }
                    }
                    else {
                        Toast.makeText(getContext(), "Invalid money inputs", Toast.LENGTH_SHORT).show();
                    }
                }
                catch(Exception exception) {
                    //invalid bank account or cash account input
                    Toast.makeText(getContext(), "Invalid money inputs", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}