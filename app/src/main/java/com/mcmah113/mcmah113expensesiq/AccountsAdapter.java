package com.mcmah113.mcmah113expensesiq;

import android.content.Context;
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
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            row = layoutInflater.inflate(R.layout.layout_listview_account_item, null);

            final Account account = getItem(position);

            if(account != null) {
                final String mainInfo = account.getName() + " (" + account.getLocale() + ")";
                final String balance = account.getSymbol() + account.getCurrentBalance();
                final String initialBalance = "Initial: " + account.getSymbol() + account.getInitialBalance();
                final String description = R.string.account_description_text + account.getDescription();

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