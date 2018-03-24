package com.mcmah113.mcmah113expensesiq;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Currency;
import java.util.Locale;

public class SettingsDialogFragment extends DialogFragment {
    public interface OnCompleteListener {
        void onCompleteUserSettings();
    }


    private static final String languageArray[] = {
        Locale.ENGLISH.getDisplayLanguage() + " (" + Locale.ENGLISH.getLanguage() + ")",
        Locale.JAPANESE.getDisplayLanguage() + " (" + Locale.JAPANESE.getLanguage() + ")",
        Locale.FRENCH.getDisplayLanguage() + " (" + Locale.FRENCH.getLanguage() + ")",
        Locale.CHINESE.getDisplayLanguage() + " (" + Locale.CHINESE.getLanguage() + ")"
    };

    //array holds the currency name and its symbol
    private static final String currencyArray[] = {
        Locale.US.getDisplayCountry() + " (" + Currency.getInstance(Locale.US) + ")",
        Locale.CANADA.getDisplayCountry() + " (" + Currency.getInstance(Locale.CANADA) + ")",
        Locale.JAPAN.getDisplayCountry() + " (" + Currency.getInstance(Locale.JAPAN) + ")",
        Locale.UK.getDisplayCountry() + " (" + Currency.getInstance(Locale.UK) + ")",
        Locale.FRANCE.getDisplayCountry() + " (" + Currency.getInstance(Locale.FRANCE) + ")",
        Locale.CHINA.getDisplayCountry() + " (" + Currency.getInstance(Locale.CHINA) + ")"
    };

    //set the index of the spinners to 0 for the first array item
    private int languageIndex = 0;
    private int currencyIndex = 0;

    public SettingsDialogFragment() {

    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final SettingsDialogFragment.OnCompleteListener onCompleteListener = (OnCompleteListener) getActivity();

        final AlertDialog.Builder settingsDialog = new AlertDialog.Builder(getActivity());

        final DatabaseHelper databaseHelper = new DatabaseHelper(getContext());

        final String userData[] = databaseHelper.getUserSettings(Overview.getUserId());

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
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                languageIndex = position;
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //set default selected based on user settings
        for(int i = 0; i < languageArray.length; i ++) {
            if(userData[0].equals(languageArray[i])) {
                spinnerLanguage.setSelection(i);
                languageIndex = i;
                break;
            }
        }

        final ArrayAdapter<String> arrayAdapterCurrency = new ArrayAdapter<>(getContext(), R.layout.layout_spinner, currencyArray);

        final Spinner spinnerCurrency = view.findViewById(R.id.spinnerCurrency);
        spinnerCurrency.setAdapter(arrayAdapterCurrency);
        spinnerCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currencyIndex = position;
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //set default selected based on user settings
        for(int i = 0; i < currencyArray.length; i ++) {
            if(userData[1].equals(currencyArray[i])) {
                spinnerCurrency.setSelection(i);
                currencyIndex = i;
                break;
            }
        }

        settingsDialog.setView(view);
        settingsDialog.setTitle("User Settings");
        settingsDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                final String language = spinnerLanguage.getItemAtPosition(languageIndex).toString();
                final String locale = spinnerCurrency.getItemAtPosition(currencyIndex).toString();

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