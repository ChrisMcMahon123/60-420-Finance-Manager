package com.mcmah113.mcmah113expensesiq;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TransactionsNewFragment extends Fragment {
    private final String transactionTypeList[] = GlobalConstants.getTransactionTypeList();

    public interface OnCompleteListener {
        void onCompleteLaunchFragment(Bundle args);
    }

    public TransactionsNewFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transactions_new, container, false);
    }

    public void onViewCreated(View view, Bundle bundle) {
        final DatabaseHelper databaseHelper = new DatabaseHelper(getContext());

        final OnCompleteListener onCompleteListener = (OnCompleteListener) getActivity();

        final int userId = Overview.getUserId();
        final int accountId = getArguments().getInt("accountId");

        final EditText editTextNote = view.findViewById(R.id.editTextNote);
        final EditText editTextPayee = view.findViewById(R.id.editTextPayee);
        final EditText editTextDate = view.findViewById(R.id.editTextDate);

        final Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") final String today = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());

        editTextDate.setText(today);

        final Account[] accountsList = databaseHelper.getAccountList(userId);
        final String spinnerString[] = new String[accountsList.length];

        //creating array that will be used for the spinners
        for(int i = 0; i < accountsList.length; i ++) {
            spinnerString[i] = accountsList[i].getName() + " (" + accountsList[i].getLocale() + ")";
        }

        final TextView textViewCurrentBalance = view.findViewById(R.id.textViewCurrentBalance);

        //setting the spinners properties
        final ArrayAdapter<String> arrayAdapterAccount = new ArrayAdapter<>(getContext(), R.layout.layout_spinner, spinnerString);

        final Spinner spinnerAccount = view.findViewById(R.id.spinnerAccount);
        spinnerAccount.setAdapter(arrayAdapterAccount);
        spinnerAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                textViewCurrentBalance.setText(String.format(accountsList[position].getSymbol() + "%.2f",accountsList[position].getCurrentBalance()));
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final ArrayAdapter<String> arrayAdapterTransactionType = new ArrayAdapter<>(getContext(), R.layout.layout_spinner, transactionTypeList);

        final Spinner spinnerTransactionType = view.findViewById(R.id.spinnerTransactionType);
        spinnerTransactionType.setAdapter(arrayAdapterTransactionType);

        final RadioButton radioButtonExpense = view.findViewById(R.id.radioButtonExpense);
        final RadioButton radioButtonIncome = view.findViewById(R.id.radioButtonIncome);

        final EditText editTextAmount = view.findViewById(R.id.editTextAmount);

        if(accountId > 0) {
            //coming from edit account
            radioButtonIncome.setChecked(true);

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
            //coming from FAB icon, income is selected by default
            if(accountId == -3) {
                radioButtonExpense.setChecked(true);
            }
            else {
                //accountId = -2 or an actual accountId
                radioButtonIncome.setChecked(true);
            }
        }

        //set button properties
        final CustomOnTouchListener onTouchListener = new CustomOnTouchListener(getResources().getColor(R.color.colorPrimaryDark, getContext().getTheme()));

        final Button buttonApply = view.findViewById(R.id.buttonTransaction);
        buttonApply.setOnTouchListener(onTouchListener);//ignore this warning...
        buttonApply.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SimpleDateFormat")
            public void onClick(View v) {
                String amountString = editTextAmount.getText().toString();
                final String dateString = editTextDate.getText().toString();

                Account account = null;

                //find out which accounts were selected and get their Ids
                for(int i = 0; i < spinnerString.length; i ++) {
                    if(spinnerString[i].equals(spinnerAccount.getSelectedItem())) {
                        account = accountsList[i];
                        break;
                    }
                }

                if(!amountString.isEmpty() && !dateString.isEmpty() && !editTextPayee.getText().toString().isEmpty() && account != null) {
                    try {
                        //check to see if the inputted date is valid, will throw exception on illegal date
                        final Calendar calendar = Calendar.getInstance();
                        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        simpleDateFormat.setLenient(false);

                        calendar.setTime(simpleDateFormat.parse(dateString));

                        try {
                            final String transactionType = spinnerTransactionType.getSelectedItem().toString();

                            double amount = Double.parseDouble(amountString);

                            if(radioButtonExpense.isChecked()) {
                                //take money out of the users account, can't input signed numbers
                                amount *= -1;
                            }

                            //update the account
                            account.setCurrentBalance(account.getCurrentBalance() + amount);

                            databaseHelper.updateAccount(userId, account);

                            //accountTo will be -1, since this is income / expense
                            //transaction id is -1, since its new
                            final Transaction transaction = new Transaction(account.getId(),-1, transactionType,account.getLocale(),account.getSymbol(), amount, dateString, editTextNote.getText().toString(), editTextPayee.getText().toString());

                            //record the transaction in the table
                            if(databaseHelper.createNewTransaction(transaction, userId)) {
                                Toast.makeText(getContext(), getResources().getString(R.string.transaction_good), Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getContext(), getResources().getString(R.string.transaction_bad), Toast.LENGTH_SHORT).show();
                            }

                            Bundle args = new Bundle();
                            args.putString("fragment", "Accounts");
                            args.putInt("accountId", -1);

                            onCompleteListener.onCompleteLaunchFragment(args);
                        }
                        catch(Exception exception) {
                            Toast.makeText(getContext(), getContext().getResources().getString(R.string.toast_invalid_money), Toast.LENGTH_SHORT).show();
                        }
                    }
                    catch (Exception exception) {
                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.invalid_date), Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.Toast_Required), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
