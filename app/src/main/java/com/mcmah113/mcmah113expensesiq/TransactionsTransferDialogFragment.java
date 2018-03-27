package com.mcmah113.mcmah113expensesiq;

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

public class TransactionsTransferDialogFragment extends DialogFragment {
    public interface OnCompleteListener {
        void onCompleteTransferFunds();
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

        final String amountAfterString = String.format(accountTo.getSymbol() + "%.2f", exchangeAmount);
        final TextView textViewAfterAmount = view.findViewById(R.id.textViewTransferAmountAfter);
        textViewAfterAmount.setText(amountAfterString);

        final TextView textViewFromLocale = view.findViewById(R.id.textViewFromLocale);
        textViewFromLocale.setText(accountFrom.getLocale());

        final TextView textViewToLocale = view.findViewById(R.id.textViewToLocale);
        textViewToLocale.setText(accountTo.getLocale());

        //accountFrom
        final TextView textViewAccountFromName = view.findViewById(R.id.textViewAccountFromName);
        textViewAccountFromName.setText(accountFrom.getName()  + " (" + accountFrom.getLocale() + ")");

        final TextView textViewBeforeAccountFrom = view.findViewById(R.id.textViewBeforeAccountFrom);
        textViewBeforeAccountFrom.setText(String.format(accountFrom.getSymbol() + "%.2f", accountFrom.getCurrentBalance()));

        final TextView textViewAfterAccountFrom = view.findViewById(R.id.textViewAfterAccountFrom);
        textViewAfterAccountFrom.setText(String.format(accountFrom.getSymbol() + "%.2f", accountFromResult));

        //accountTo
        final TextView textViewAccountNameTo = view.findViewById(R.id.textViewAccountNameTo);
        textViewAccountNameTo.setText(accountTo.getName() + " (" + accountTo.getLocale() + ")");

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



            Toast.makeText(getContext(), "Successfully transferred the funds", Toast.LENGTH_SHORT).show();

            onCompleteListener.onCompleteTransferFunds();
            }
        });
        transferFundsDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            onCompleteListener.onCompleteTransferFunds();
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