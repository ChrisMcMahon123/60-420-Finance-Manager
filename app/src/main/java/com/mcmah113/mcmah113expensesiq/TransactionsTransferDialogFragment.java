package com.mcmah113.mcmah113expensesiq;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TransactionsTransferDialogFragment extends DialogFragment {
    public interface OnCompleteListener {
        void onCompleteLaunchFragment(Bundle args);
    }

    public TransactionsTransferDialogFragment() {

    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final DatabaseHelper databaseHelper = new DatabaseHelper(getContext());

        final OnCompleteListener onCompleteListener = (OnCompleteListener) getActivity();

        //get account information
        final int userId = Overview.getUserId();
        final int accountFromId = getArguments().getInt("accountFromId");
        final int accountToId = getArguments().getInt("accountToId");
        final Account accountFrom = databaseHelper.getAccountInfo(accountFromId, userId);
        final Account accountTo = databaseHelper.getAccountInfo(accountToId, userId);

        //determine the amounts based on the exchange rate
        final double amount = getArguments().getDouble("amount");
        final double exchangeRate = getArguments().getDouble("exchangeRate");
        final double exchangeAmount = amount / exchangeRate;

        final double accountFromResult = accountFrom.getCurrentBalance() - amount;
        final double accountsToResult = accountTo.getCurrentBalance() + exchangeAmount;

        //getting the layout from the fragment
        final LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        final LinearLayout view = (LinearLayout) layoutInflater.inflate(R.layout.fragment_transactions_transfer_dialog, null);

        //exchange overview
        final String amountBeforeString = String.format(accountFrom.getSymbol() + "%.2f", amount);
        final TextView textViewBeforeAmount = view.findViewById(R.id.textViewTransferAmountBefore);
        textViewBeforeAmount.setText(amountBeforeString);

        final TextView textViewFromLocale = view.findViewById(R.id.textViewFromLocale);
        textViewFromLocale.setText(accountFrom.getLocale());

        if(!accountFrom.getLocale().equals(accountTo.getLocale())) {
            final String amountAfterString = String.format(accountTo.getSymbol() + "%.2f", exchangeAmount);
            final TextView textViewAfterAmount = view.findViewById(R.id.textViewTransferAmountAfter);
            textViewAfterAmount.setText(amountAfterString);

            final TextView textViewToLocale = view.findViewById(R.id.textViewToLocale);
            textViewToLocale.setText(accountTo.getLocale());
        }

        //accountFrom
        final TextView textViewAccountFromName = view.findViewById(R.id.textViewAccountFromName);
        final String accountFromName = accountFrom.getName()  + " (" + accountFrom.getLocale() + ")";
        textViewAccountFromName.setText(accountFromName);

        final TextView textViewBeforeAccountFrom = view.findViewById(R.id.textViewBeforeAccountFrom);
        textViewBeforeAccountFrom.setText(String.format(accountFrom.getSymbol() + "%.2f", accountFrom.getCurrentBalance()));

        final TextView textViewAfterAccountFrom = view.findViewById(R.id.textViewAfterAccountFrom);
        textViewAfterAccountFrom.setText(String.format(accountFrom.getSymbol() + "%.2f", accountFromResult));

        //accountTo
        final TextView textViewAccountNameTo = view.findViewById(R.id.textViewAccountNameTo);
        final String accountToName = accountTo.getName() + " (" + accountTo.getLocale() + ")";
        textViewAccountNameTo.setText(accountToName);

        final TextView textViewBeforeAccountTo = view.findViewById(R.id.textViewBeforeAccountTo);
        textViewBeforeAccountTo.setText(String.format(accountTo.getSymbol() + "%.2f", accountTo.getCurrentBalance()));

        final TextView textViewAfterAccountTo = view.findViewById(R.id.textViewAfterAccountTo);
        textViewAfterAccountTo.setText(String.format(accountTo.getSymbol() + "%.2f", accountsToResult));

        final AlertDialog.Builder transferFundsDialog = new AlertDialog.Builder(getActivity());
        transferFundsDialog.setView(view);
        transferFundsDialog.setTitle("Transfer Funds");
        transferFundsDialog.setMessage("Are you sure?");
        transferFundsDialog.setPositiveButton("Transfer", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            //Apply the changes to the database
            accountFrom.setCurrentBalance(accountFromResult);
            accountTo.setCurrentBalance(accountsToResult);
            databaseHelper.updateAccount(userId, accountFrom);
            databaseHelper.updateAccount(userId, accountTo);

            //record the transaction in the table
            final Date currentTime = Calendar.getInstance().getTime();
            @SuppressLint("SimpleDateFormat") final String date = new SimpleDateFormat("yyyy-MM-dd").format(currentTime);

            Transaction transaction1 = new Transaction(accountFromId,accountToId, "Transfer",accountFrom.getLocale(),accountFrom.getSymbol(), (-1 * amount), date, getArguments().getString("note"));
            Transaction transaction2 = new Transaction(accountToId,accountFromId, "Receive",accountTo.getLocale(), accountTo.getSymbol(), exchangeAmount, date, getArguments().getString("note"));

            if(databaseHelper.createNewTransaction(transaction1, userId) && databaseHelper.createNewTransaction(transaction2, userId) ) {
                Toast.makeText(getContext(), "Successfully applied the transaction", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getContext(), "Failed to record the transaction", Toast.LENGTH_SHORT).show();
            }

            Toast.makeText(getContext(), "Successfully transferred the funds", Toast.LENGTH_SHORT).show();

            final Bundle args = new Bundle();
            args.putString("fragment", "Accounts");
            args.putInt("accountId", -1);
            onCompleteListener.onCompleteLaunchFragment(args);
            }
        });
        transferFundsDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            final Bundle args = new Bundle();
            args.putString("fragment", "Make a Transfer");
            args.putInt("accountId", accountFromId);
            onCompleteListener.onCompleteLaunchFragment(args);
            }
        });

        return transferFundsDialog.create();
    }

    public void onStart() {
        super.onStart();

        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary, getContext().getTheme()));
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary, getContext().getTheme()));
    }
}