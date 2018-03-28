package com.mcmah113.mcmah113expensesiq;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AccountsAdapter extends ArrayAdapter<Account> {
    AccountsAdapter(Context context, Account[] accounts) {
        super(context,0, accounts);
    }

    public @NonNull View getView(int position, View row, @Nullable ViewGroup parent) {
        if(row == null) {
            //will contain info about that specific account
            final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            row = layoutInflater.inflate(R.layout.layout_listview_account_item, null);

            final Account account = getItem(position);

            if(account != null) {
                final String mainInfo = account.getName() + " (" + account.getLocale() + ")";
                final String balance = account.getSymbol() + account.getCurrentBalance();
                final String initialBalance = getContext().getResources().getString(R.string.initial_text) + " " + account.getSymbol() + account.getInitialBalance();
                final String description = getContext().getResources().getString(R.string.account_description_text) + " " + account.getDescription();

                final TextView textViewAccountName = row.findViewById(R.id.textViewAccountName);
                textViewAccountName.setText(mainInfo);

                final TextView textViewAccountBalance = row.findViewById(R.id.textViewAccountCurrentBalance);
                textViewAccountBalance.setText(balance);

                final TextView textViewAccountType = row.findViewById(R.id.textViewAccountType);
                textViewAccountType.setText(account.getType());

                final TextView textViewAccountInitialBalance = row.findViewById(R.id.textViewAccountInitialBalance);
                textViewAccountInitialBalance.setText(initialBalance);

                final TextView textViewAccountDescription = row.findViewById(R.id.textViewAccountDescription);
                textViewAccountDescription.setText(description);

                final TextView textViewHidden = row.findViewById(R.id.textViewHiddenAccount);

                final TextView textViewDifference = row.findViewById(R.id.textViewAccountDifference);

                double difference = account.getCurrentBalance() - account.getInitialBalance();

                if(difference == 0) {
                    textViewDifference.setText("");
                }
                else if(difference > 0) {
                    //in the positive

                    textViewDifference.setTextColor(getContext().getResources().getColor(R.color.colorGreen, getContext().getTheme()));
                    textViewDifference.setText(String.format(account.getSymbol() +"%.2f", difference));
                }
                else {
                    //in the negative
                    textViewDifference.setTextColor(getContext().getResources().getColor(R.color.colorRed, getContext().getTheme()));
                    textViewDifference.setText(String.format(account.getSymbol() + "%.2f", (-1 * difference)));
                }

                if(account.getHiddenFlag()) {
                    textViewHidden.setText(R.string.hidden_account_text);
                }
                else {
                    textViewHidden.setVisibility(View.INVISIBLE);
                }

                final ImageView imageViewAccountIcon = row.findViewById(R.id.imageViewAccountIcon);
                //imageViewAccountIcon.setImageBitmap(account.getIcon());
            }
        }

        return row;
    }
}