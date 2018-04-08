package com.mcmah113.mcmah113expensesiq;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class OverviewAdapter extends RecyclerView.Adapter<OverviewAdapter.ViewHolder> {
    private static DialogFragment accountDialog;

    public interface OnCompleteListener {
        void onCompleteLaunchFragment(Bundle args);
    }

    private ArrayList<Account> accountsList = new ArrayList<>();

    OverviewAdapter(Account accountList[]) {
        this.accountsList.addAll(Arrays.asList(accountList));

        final Account accountAdd = new Account(-1, "Add Account", "Add", "", "", -1, -1, "", false);
        this.accountsList.add(this.accountsList.size(), accountAdd);
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        final View row = layoutInflater.inflate(R.layout.layout_overview_accounts_item, parent, false);

        //setting the images of the accounts since there is finally a context variable
        //add the account 'button' to the list so its displayed last
        for(final Account account : accountsList) {
            account.setImageIcon(parent.getContext());
        }

        return new ViewHolder(row);
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        final Account account = accountsList.get(position);

        holder.textView.setText(account.getName());
        holder.imageView.setImageBitmap(account.getIcon());
        holder.container.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                final Bundle args = new Bundle();

                if(account.getType().equals("Add")) {
                    //go to the new account creation fragment
                    args.putInt("accountId", -1);
                    args.putString("fragment", "New Account");

                    final OnCompleteListener onCompleteListener = (OnCompleteListener) view.getContext();
                    onCompleteListener.onCompleteLaunchFragment(args);
                }
                else {
                    //open the Account dialog like the Account fragment
                    args.putInt("accountId", account.getId());

                    accountDialog = new AccountsDialogFragment();
                    accountDialog.setArguments(args);
                    FragmentManager fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
                    accountDialog.show(fragmentManager, "Account Dialog");
                }
            }
        });
    }

    public int getItemCount() {
        return accountsList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private ImageView imageView;
        private LinearLayout container;

        ViewHolder(View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.textViewAccountName);
            imageView = itemView.findViewById(R.id.imageViewAccountIcon);
            container = itemView.findViewById(R.id.ViewHolderContainer);
        }
    }

    public static DialogFragment getAlertDialog() {
        return accountDialog;
    }

    public static void setAlertDialog() {
        accountDialog = null;
    }
}
