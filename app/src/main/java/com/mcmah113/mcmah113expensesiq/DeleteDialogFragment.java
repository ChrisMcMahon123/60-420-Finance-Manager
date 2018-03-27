package com.mcmah113.mcmah113expensesiq;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;

public class DeleteDialogFragment extends DialogFragment {
    public interface OnCompleteListener {
        void onCompleteDeleteAccount(Bundle callbackData);
    }

    public DeleteDialogFragment() {

    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final OnCompleteListener onCompleteListener = (OnCompleteListener) getActivity();

        final int accountId = getArguments().getInt("accountId");
        final String accountName = getArguments().getString("accountName");

        final Bundle args = new Bundle();

        final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getActivity());
        deleteDialog.setTitle(getResources().getString(R.string.delete_account_dialog_option));
        deleteDialog.setMessage(getResources().getString(R.string.are_you_sure_dialog_option) + " " + accountName + "?");
        deleteDialog.setPositiveButton(getResources().getString(R.string.save_dialog_option), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                args.putString("response", "Yes");
                args.putInt("accountId", accountId);
                onCompleteListener.onCompleteDeleteAccount(args);
            }
        });
        deleteDialog.setNegativeButton(getResources().getString(R.string.cancel_dialog_option), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                args.putString("response", "No");
                onCompleteListener.onCompleteDeleteAccount(args);
            }
        });

        return deleteDialog.create();
    }

    public void onStart() {
        super.onStart();

        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary, getContext().getTheme()));
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary, getContext().getTheme()));
    }
}