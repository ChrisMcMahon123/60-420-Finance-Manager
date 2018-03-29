package com.mcmah113.mcmah113expensesiq;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.HashMap;

public class SettingsDialogFragment extends DialogFragment {
    public interface OnCompleteListener {
        void onCompleteUserSettings();
    }

    private static final String languageArray[] = GlobalConstants.getLanguageArray();

    //array holds the currency name and its symbol
    private static final String currencyArray[] = GlobalConstants.getCurrencyArray();

    public SettingsDialogFragment() {

    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final SettingsDialogFragment.OnCompleteListener onCompleteListener = (OnCompleteListener) getActivity();

        final AlertDialog.Builder settingsDialog = new AlertDialog.Builder(getActivity());

        final DatabaseHelper databaseHelper = new DatabaseHelper(getContext());

        final HashMap<String, String> userData = databaseHelper.getUserSettings(Overview.getUserId());

        //getting the layout from the fragment
        final LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        final LinearLayout view = (LinearLayout) layoutInflater.inflate(R.layout.fragment_settings_dialog, null);

        //setting up the checkbox properties
        final CheckBox checkBoxShowHidden = view.findViewById(R.id.checkBoxShowHidden);
        checkBoxShowHidden.setChecked(DatabaseHelper.getDisplayHiddenFlag());

        //setting the spinners properties
        final ArrayAdapter<String> arrayAdapterLanguage = new ArrayAdapter<>(getContext(), R.layout.layout_spinner, languageArray);

        final Spinner spinnerLanguage = view.findViewById(R.id.spinnerLanguage);
        spinnerLanguage.setAdapter(arrayAdapterLanguage);

        //set default selected based on user settings
        for(int i = 0; i < languageArray.length; i ++) {
            if(userData.get("language").equals(languageArray[i])) {
                spinnerLanguage.setSelection(i);
                break;
            }
        }

        final ArrayAdapter<String> arrayAdapterCurrency = new ArrayAdapter<>(getContext(), R.layout.layout_spinner, currencyArray);

        final Spinner spinnerCurrency = view.findViewById(R.id.spinnerCurrency);
        spinnerCurrency.setAdapter(arrayAdapterCurrency);

        //set default selected based on user settings
        for(int i = 0; i < currencyArray.length; i ++) {
            if(userData.get("locale").equals(currencyArray[i])) {
                spinnerCurrency.setSelection(i);
                break;
            }
        }

        settingsDialog.setView(view);
        settingsDialog.setTitle("User Settings");
        settingsDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                final String language = spinnerLanguage.getSelectedItem().toString();
                String locale = spinnerCurrency.getSelectedItem().toString();

                //only want the code inside the brackets
                locale = locale.substring(locale.indexOf('(') + 1, locale.indexOf(')'));

                if(databaseHelper.setUserSettings(Overview.getUserId(), language, locale)) {
                    Toast.makeText(getContext(), "User Settings Updated", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getContext(), "Failed to Update User Settings", Toast.LENGTH_SHORT).show();
                }

                //allow hidden accounts to be shown
                DatabaseHelper.setDisplayHiddenFlag(checkBoxShowHidden.isChecked());
                onCompleteListener.onCompleteUserSettings();
            }
        });
        settingsDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                onCompleteListener.onCompleteUserSettings();
            }
        });

        return settingsDialog.create();
    }

    public void onStart() {
        super.onStart();

        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary, getContext().getTheme()));
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary, getContext().getTheme()));
    }
}