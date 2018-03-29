package com.mcmah113.mcmah113expensesiq;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class AccountsDialogFragment extends DialogFragment {
    public interface OnCompleteListener {
        void onCompleteLaunchFragment(Bundle callbackData);
    }

    public AccountsDialogFragment() {

    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int accountId = getArguments().getInt("accountId");

        final AlertDialog.Builder accountDialog = new AlertDialog.Builder(getActivity());

        //getting the layout from the fragment
        final LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        final LinearLayout view = (LinearLayout) layoutInflater.inflate(R.layout.fragment_dialog_accounts, null);

        final LinearLayout buttonViewTransactions = view.findViewById(R.id.ViewTransactions);
        buttonViewTransactions.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                callBack("View Transactions", accountId);
            }
        });

        final LinearLayout buttonMakeATransaction = view.findViewById(R.id.MakeTransfer);
        buttonMakeATransaction.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                callBack("Make a Transfer", accountId);
            }
        });

        final LinearLayout buttonAddTransaction = view.findViewById(R.id.AddTransaction);
        buttonAddTransaction.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                callBack("Add Transaction", accountId);
            }
        });

        final LinearLayout buttonEditAccount = view.findViewById(R.id.EditAccount);
        buttonEditAccount.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                callBack("Edit Account", accountId);
            }
        });

        final LinearLayout buttonDeleteAccount = view.findViewById(R.id.DeleteAccount);
        buttonDeleteAccount.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                callBack("Delete Account", accountId);
            }
        });

        final LinearLayout buttonHideAccount = view.findViewById(R.id.HideAccount);
        buttonHideAccount.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                callBack("Hide Account", accountId);
            }
        });

        accountDialog.setView(view);
        accountDialog.setTitle("Select Action");

        return accountDialog.create();
    }

    private void callBack(String selection, int accountId) {
        final Bundle args = new Bundle();
        args.putInt("accountId", accountId);
        args.putString("fragment", selection);

        //call back to activity with option selection
        //the activity will close the dialog
        final OnCompleteListener onCompleteListener = (OnCompleteListener) getActivity();
        onCompleteListener.onCompleteLaunchFragment(args);
    }
}
