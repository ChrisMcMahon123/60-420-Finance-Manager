package com.mcmah113.mcmah113expensesiq;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AccountsEditFragment extends Fragment {
    private static final String currencyArray[] = GlobalConstants.getCurrencyArray();
    private static final String typesArray[] = GlobalConstants.getTypesArray();
    private Account account;

    public interface OnCompleteListener {
        void onCompleteLaunchFragment(Bundle args);
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

        final ArrayAdapter<String> arrayAdapterCurrency = new ArrayAdapter<>(getContext(), R.layout.layout_spinner, currencyArray);

        final Spinner spinnerCurrency = view.findViewById(R.id.spinnerCurrency);
        spinnerCurrency.setAdapter(arrayAdapterCurrency);

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
                    break;
                }
            }

            //set default selected based on account object
            for(int i = 0; i < currencyArray.length; i ++) {
                if(currencyArray[i].contains(account.getLocale())) {
                    spinnerCurrency.setSelection(i);
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
                final String type = spinnerType.getSelectedItem().toString();
                String locale = spinnerCurrency.getSelectedItem().toString();

                //only want the code inside the brackets
                locale = locale.substring(locale.indexOf('(') + 1, locale.indexOf(')'));

                if(name.isEmpty() || initialBalanceString.isEmpty() || currentBalanceString.isEmpty()) {
                    Toast.makeText(getContext(), "(*) Fields are required", Toast.LENGTH_SHORT).show();
                }
                else {
                    try {
                        //check to see if valid numbers
                        double initialBalance = Double.parseDouble(initialBalanceString);
                        double currentBalance = Double.parseDouble(currentBalanceString);

                        if(accountId > 0) {
                            //check for account changes
                            boolean editFlag = false;
                            String note = "";

                            if(currentBalance != account.getCurrentBalance()) {
                                editFlag = true;
                                note += "Current balance changed from " + account.getCurrentBalance() + " to " + currentBalance + "\n";
                            }
                            if(initialBalance != account.getInitialBalance()) {
                                editFlag = true;
                                note += "Initial balance changed from "+ account.getInitialBalance() + " to " + initialBalance + "\n";
                            }
                            if(!locale.equals(account.getLocale())) {
                                editFlag = true;
                                note += "Account locale changed from "+ account.getLocale() + " to " + locale + "\n";
                            }
                            if(!type.equals(account.getType())) {
                                editFlag = true;
                                note += "Account type changed from "+ account.getType() + " to " + type + "\n";
                            }

                            //save changes to existing account
                            account.setName(name);
                            account.setType(type);
                            account.setLocale(locale);
                            account.setInitialBalance(initialBalance);
                            account.setCurrentBalance(currentBalance);
                            account.setDescription(description);
                            account.setHiddenFlag(checkboxHiddenAccount.isChecked());

                            if(databaseHelper.updateAccount(userId, account)) {
                                //record the transaction in the table
                                if(editFlag) {
                                    final Date currentTime = Calendar.getInstance().getTime();
                                    String date = new SimpleDateFormat("yyyy-MM-dd").format(currentTime);

                                    Transaction transaction = new Transaction(account.getId(),-1, "Account Edit",account.getLocale(), account.getSymbol(), 0, date, note);

                                    databaseHelper.createNewTransaction(transaction, userId);
                                }

                                Toast.makeText(getContext(), "Saved account changes", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getContext(), "Failed to save account changes", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            //create a new account
                            if(databaseHelper.createAccount(userId, name, type, locale, initialBalance, currentBalance, description, checkboxHiddenAccount.isChecked())) {
                                Toast.makeText(getContext(), "Account successfully created", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getContext(), "Failed to create account", Toast.LENGTH_SHORT).show();
                            }
                        }
                        final Bundle args = new Bundle();
                        args.putInt("accountId", -1);
                        args.putString("fragment", "Accounts");

                        //return to the main activity which will redirect to Accounts fragment
                        final AccountsEditFragment.OnCompleteListener onCompleteListener = (AccountsEditFragment.OnCompleteListener) getActivity();
                        onCompleteListener.onCompleteLaunchFragment(args);
                    }
                    catch(Exception exception) {
                        //invalid bank account or cash account input
                        Toast.makeText(getContext(), "Invalid money inputs", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}