package com.mcmah113.mcmah113expensesiq;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.Configuration;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;

public class SettingsDialogFragment extends DialogFragment {
    public interface OnCompleteListener {
        void onCompleteSettingsChange();
    }

    private static final String languageArray[] = GlobalConstants.getLanguageArray();

    //array holds the currency name and its symbol
    private static final String currencyArray[] = GlobalConstants.getCurrencyArray();

    public SettingsDialogFragment() {

    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final OnCompleteListener onCompleteListener = (OnCompleteListener) getActivity();

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
            if(languageArray[i].contains(userData.get("language"))) {
                spinnerLanguage.setSelection(i);
                break;
            }
        }

        final ArrayAdapter<String> arrayAdapterCurrency = new ArrayAdapter<>(getContext(), R.layout.layout_spinner, currencyArray);

        final Spinner spinnerCurrency = view.findViewById(R.id.spinnerCurrency);
        spinnerCurrency.setAdapter(arrayAdapterCurrency);

        //set default selected based on user settings
        for(int i = 0; i < currencyArray.length; i ++) {
            if(currencyArray[i].contains(userData.get("locale"))) {
                spinnerCurrency.setSelection(i);
                break;
            }
        }

        settingsDialog.setView(view);
        settingsDialog.setTitle(getContext().getResources().getString(R.string.user_settings_dialog));
        settingsDialog.setPositiveButton(getContext().getResources().getString(R.string.save_dialog_settings), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String language = spinnerLanguage.getSelectedItem().toString();
                String locale = spinnerCurrency.getSelectedItem().toString();

                //only want the code inside the brackets
                locale = locale.substring(locale.indexOf('(') + 1, locale.indexOf(')'));
                language = language.substring(language.indexOf('(') + 1, language.indexOf(')'));

                //update the user settings, flags don't get touched here, so return the same values
                if(databaseHelper.setUserSettings(Overview.getUserId(), language, locale,userData.get("flag1"),userData.get("flag2"),userData.get("flag3"),userData.get("flag4"))) {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.user_settings_good), Toast.LENGTH_SHORT).show();

                    //load the users default language
                    Locale LanguageLocale = new Locale(language);
                    DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
                    Configuration configuration = getContext().getResources().getConfiguration();
                    configuration.locale = LanguageLocale;
                    getContext().getResources().updateConfiguration(configuration, displayMetrics);
                }
                else {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.user_settings_failed), Toast.LENGTH_SHORT).show();
                }

                //allow hidden accounts to be shown
                DatabaseHelper.setDisplayHiddenFlag(checkBoxShowHidden.isChecked());
                onCompleteListener.onCompleteSettingsChange();
            }
        });
        settingsDialog.setNegativeButton(getContext().getResources().getString(R.string.user_settings_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                onCompleteListener.onCompleteSettingsChange();
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