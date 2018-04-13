package com.mcmah113.mcmah113expensesiq;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TransactionsAdapter extends ArrayAdapter<Transaction> {
    TransactionsAdapter(Context context, Transaction[] transaction) {
        super(context,0, transaction);
    }

    public @NonNull
    View getView(int position, View row, @Nullable ViewGroup parent) {
        final DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
        final int userId = Overview.getUserId();

        if(row == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            row = layoutInflater.inflate(R.layout.layout_listview_transactions, null);
        }

        final Transaction transaction = getItem(position);

        if(transaction != null) {
            //get the accounts details to display
            final Account accountFrom = databaseHelper.getAccountInfo(transaction.getAccountFromId(), userId);

            final TextView textViewName = row.findViewById(R.id.textViewAccount);
            textViewName.setText(accountFrom.getName());

            final TextView textViewAmount = row.findViewById(R.id.textViewAmount);

            if (transaction.getAmount() > 0) {
                textViewAmount.setTextColor(getContext().getResources().getColor(R.color.colorGreen, getContext().getTheme()));
            }
            else if (transaction.getAmount() < 0) {
                textViewAmount.setTextColor(getContext().getResources().getColor(R.color.colorRed, getContext().getTheme()));
            }

            textViewAmount.setText(String.format(transaction.getSymbol() + "%.2f", (Math.abs(transaction.getAmount()))));

            final TextView textViewDate = row.findViewById(R.id.textViewDate);
            textViewDate.setText(transaction.getDate());

            final TextView textViewNote = row.findViewById(R.id.textViewNote);
            final String transactionNote = getContext().getResources().getString(R.string.note_text) + "\n" + transaction.getNote();
            textViewNote.setText(transactionNote);

            final TextView textViewType = row.findViewById(R.id.textViewType);
            final TextView textViewPayee = row.findViewById(R.id.textViewPayee);

            if(transaction.getAccountToId() > 0 && (transaction.getType().equals("Transfer") || transaction.getType().equals("Receive"))) {
                //need to display a little more information like who the money went to / where it came from
                textViewPayee.setText(getContext().getResources().getString(R.string.transferred_funds_label));

                final Account accountTo = databaseHelper.getAccountInfo(transaction.getAccountToId(), userId);
                String type = "";

                if(accountTo != null) {
                    if(accountTo.getHiddenFlag() && !DatabaseHelper.getDisplayHiddenFlag()) {
                        if(transaction.getType().equals("Transfer")) {
                            //sent money to an account
                            type = getContext().getResources().getString(R.string.transferred_money_to_text) + " " + getContext().getResources().getString(R.string.account_hidden_text);
                        }
                        else if(transaction.getType().equals("Receive")) {
                            //received money from an account
                            type = getContext().getResources().getString(R.string.recieved_money_from_transaction) + " " + getContext().getResources().getString(R.string.account_hidden_text);
                        }
                    }
                    else {
                        if(transaction.getType().equals("Transfer")) {
                            //sent money to an account
                            type = getContext().getResources().getString(R.string.transferred_money_to_text) + " " + accountTo.getName();
                        }
                        else if(transaction.getType().equals("Receive")) {
                            //received money from an account
                            type = getContext().getResources().getString(R.string.recieved_money_from_transaction) + " " + accountTo.getName();
                        }
                    }
                }
                else {
                    if(transaction.getType().equals("Transfer")) {
                        //sent money to an account
                        type = getContext().getResources().getString(R.string.transferred_money_to_text) + " " + getContext().getResources().getString(R.string.account_deleted_transaction);
                    }
                    else if(transaction.getType().equals("Receive")) {
                        //received money from an account
                        type = getContext().getResources().getString(R.string.recieved_money_from_transaction) + " " + getContext().getResources().getString(R.string.account_deleted_transaction);
                    }
                }

                textViewType.setText(type);
            }
            else {
                //show the transaction type
                textViewPayee.setText(transaction.getPayee());
                textViewType.setText(transaction.getType());
            }
        }

        return row;
    }
}