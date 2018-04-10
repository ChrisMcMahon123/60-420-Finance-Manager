package com.mcmah113.mcmah113expensesiq;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import java.util.HashMap;

public class OverviewCustomizeDialogFragment extends DialogFragment {
    public interface OnCompleteListener {
        void onCompleteSettingsChange();
    }

    private String flag1;
    private String flag2;
    private String flag3;
    private String flag4;

    public OverviewCustomizeDialogFragment() {

    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final OnCompleteListener onCompleteListener = (OnCompleteListener) getActivity();

        final DatabaseHelper databaseHelper = new DatabaseHelper(getContext());

        final HashMap<String, String> userData = databaseHelper.getUserSettings(Overview.getUserId());

        final AlertDialog.Builder customizeDialog = new AlertDialog.Builder(getActivity());

        //getting the layout from the fragment
        final LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        final LinearLayout view = (LinearLayout) layoutInflater.inflate(R.layout.fragment_overview_customize, null);

        final Switch switch1 = view.findViewById(R.id.switchAccounts);
        final Switch switch2 = view.findViewById(R.id.switchTransactions);
        final Switch switch3 = view.findViewById(R.id.switchReports1);
        final Switch switch4 = view.findViewById(R.id.switchReports2);

        if("1".equals(userData.get("flag1"))) {
            switch1.setChecked(true);
        }

        if("1".equals(userData.get("flag2"))) {
            switch2.setChecked(true);
        }

        if("1".equals(userData.get("flag3"))) {
            switch3.setChecked(true);
        }

        if("1".equals(userData.get("flag4"))) {
            switch4.setChecked(true);
        }

        customizeDialog.setView(view);
        customizeDialog.setTitle("Customize Overview");
        customizeDialog.setMessage("Choose what to show or hide on the Overview screen");
        customizeDialog.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //update the user settings, flags are the only thing that gets touched here
                //so return the same values for everything else
                if(switch1.isChecked()) {
                    flag1 = "1";
                }
                else {
                    flag1 = "0";
                }

                if(switch2.isChecked()) {
                    flag2 = "1";
                }
                else {
                    flag2 = "0";
                }

                if(switch3.isChecked()) {
                    flag3 = "1";
                }
                else {
                    flag3 = "0";
                }

                if(switch4.isChecked()) {
                    flag4 = "1";
                }
                else {
                    flag4 = "0";
                }

                if(databaseHelper.setUserSettings(Overview.getUserId(), userData.get("language"), userData.get("locale"),flag1,flag2,flag3,flag4)) {
                    Toast.makeText(getContext(), "Overview updated", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getContext(), "Failed to update the Overview screen", Toast.LENGTH_SHORT).show();
                }

                onCompleteListener.onCompleteSettingsChange();
            }
        });
        customizeDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                onCompleteListener.onCompleteSettingsChange();
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