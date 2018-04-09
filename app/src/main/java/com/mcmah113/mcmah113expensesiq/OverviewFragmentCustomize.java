package com.mcmah113.mcmah113expensesiq;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class OverviewFragmentCustomize extends DialogFragment {

    public OverviewFragmentCustomize() {

    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder customizeDialog = new AlertDialog.Builder(getActivity());

        //getting the layout from the fragment
        final LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        final LinearLayout view = (LinearLayout) layoutInflater.inflate(R.layout.fragment_overview_customize, null);

        customizeDialog.setView(view);
        customizeDialog.setTitle("Customize Overview");
        customizeDialog.setMessage("Choose what to show or hide on the Overview screen");
        customizeDialog.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        customizeDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return customizeDialog.create();
    }

    public void onStart() {
        super.onStart();

        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary, getContext().getTheme()));
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary, getContext().getTheme()));
    }
}