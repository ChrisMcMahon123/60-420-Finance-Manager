package com.mcmah113.mcmah113expensesiq;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
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
        final DeleteDialogFragment.OnCompleteListener onCompleteListener = (DeleteDialogFragment.OnCompleteListener) getActivity();

        final int accountId = getArguments().getInt("accountId");
        final String accountName = getArguments().getString("accountName");

        final Bundle args = new Bundle();

        final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getActivity());
        deleteDialog.setTitle("Delete Account");
        deleteDialog.setMessage("Are you sure you want to delete " + accountName + "?");
        deleteDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                args.putString("response", "Yes");
                args.putInt("accountId", accountId);
                onCompleteListener.onCompleteDeleteAccount(args);
            }
        });
        deleteDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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