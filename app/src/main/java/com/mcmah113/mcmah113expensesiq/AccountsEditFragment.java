package com.mcmah113.mcmah113expensesiq;

import android.annotation.SuppressLint;
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
import java.util.HashMap;

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

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    public void onViewCreated(View view, Bundle bundle) {
        final DatabaseHelper databaseHelper = new DatabaseHelper(getContext());

        final int userId = Overview.getUserId();
        final int accountId = getArguments().getInt("accountId");
        final String callFrom = getArguments().getString("callFrom");

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
            //get user settings to set account currency default
            HashMap<String, String> userSettings = databaseHelper.getUserSettings(userId);

            //set default selected based on account object
            for(int i = 0; i < currencyArray.length; i ++) {
                if(currencyArray[i].contains(userSettings.get("locale"))) {
                    spinnerCurrency.setSelection(i);
                    break;
                }
            }

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
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.Toast_Required), Toast.LENGTH_SHORT).show();
                }
                else {
                    try {
                        //check to see if valid numbers
                        double initialBalance = Double.parseDouble(initialBalanceString);
                        double currentBalance = Double.parseDouble(currentBalanceString);

                        if(accountId > 0) {
                            //already existing account, updating it
                            //check for account changes
                            boolean editFlag = false;
                            String note = "";

                            if(currentBalance != account.getCurrentBalance()) {
                                editFlag = true;
                                note += getContext().getResources().getString(R.string.account_updated_1) + account.getCurrentBalance() + " " + getContext().getResources().getString(R.string.to_label_string) + " " + currentBalance + "\n";
                            }
                            if(initialBalance != account.getInitialBalance()) {
                                editFlag = true;
                                note += getContext().getResources().getString(R.string.account_change_2)+ account.getInitialBalance() + " " + getContext().getResources().getString(R.string.to_label_string) + " " + initialBalance + "\n";
                            }
                            if(!locale.equals(account.getLocale())) {
                                editFlag = true;
                                note += getContext().getResources().getString(R.string.account_change_3)+ account.getLocale() + " " + getContext().getResources().getString(R.string.to_label_string) + " " + locale + "\n";
                            }
                            if(!type.equals(account.getType())) {
                                editFlag = true;
                                note += getContext().getResources().getString(R.string.account_changed_4)+ account.getType() + " " + getContext().getResources().getString(R.string.to_label_string) + " " + type + "\n";
                            }

                            //save changes to existing account
                            account.setName(name);
                            account.setType(type);
                            account.setLocale(locale);
                            //when locale changes, so does symbol
                            account.setInitialBalance(initialBalance);
                            account.setCurrentBalance(currentBalance);
                            account.setDescription(description);
                            account.setHiddenFlag(checkboxHiddenAccount.isChecked());

                            if(databaseHelper.updateAccount(userId, account)) {
                                //record the transaction in the table
                                if(editFlag) {
                                    final Date currentTime = Calendar.getInstance().getTime();
                                    @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd").format(currentTime);

                                    Transaction transaction = new Transaction(account.getId(),-1, "Account Edit",account.getLocale(), account.getSymbol(), 0, date, note, "");

                                    databaseHelper.createNewTransaction(transaction, userId);
                                }

                                Toast.makeText(getContext(), getContext().getResources().getString(R.string.toast_account_changes), Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getContext(), getContext().getResources().getString(R.string.toast_failed_account_changes), Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            //no account id, which means we create a new account
                            if(databaseHelper.createAccount(userId, name, type, locale, initialBalance, currentBalance, description, checkboxHiddenAccount.isChecked())) {
                                Toast.makeText(getContext(), getContext().getResources().getString(R.string.account_success_created), Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getContext(), getContext().getResources().getString(R.string.account_failed_create), Toast.LENGTH_SHORT).show();
                            }
                        }

                        final Bundle args = new Bundle();
                        args.putInt("accountId", -1);
                        args.putString("fragment", callFrom);

                        //return to the main activity which will redirect to Accounts fragment
                        final OnCompleteListener onCompleteListener = (OnCompleteListener) getActivity();
                        onCompleteListener.onCompleteLaunchFragment(args);
                    }
                    catch(Exception exception) {
                        //invalid bank account or cash account input
                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.toast_invalid_money), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}