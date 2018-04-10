package com.mcmah113.mcmah113expensesiq;

import android.annotation.SuppressLint;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class AccountsFragment extends Fragment {
    private static DialogFragment accountDialog;

    public AccountsFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_accounts, container, false);
    }

    public void onViewCreated(View view, Bundle bundle) {
        final int userId = Overview.getUserId();
        //final String username = Overview.getUsername();

        final DatabaseHelper databaseHelper = new DatabaseHelper(getContext());

        final Account accounts[] = databaseHelper.getAccountList(userId);
        final String summary[][] = getSummary(accounts);

        final AccountsAdapter accountsAdapter = new AccountsAdapter(getContext(), accounts);

        //set up the listView properties, add the header and entries
        final ListView listView = view.findViewById(R.id.listViewAccounts);
        LayoutInflater layoutInflater = getLayoutInflater();
        View headerLayout = layoutInflater.inflate(R.layout.layout_listview_header_accounts,null);
        LinearLayout insertionPoint = headerLayout.findViewById(R.id.listViewHeader);
        insertionPoint.addView(createListHeader(summary, accounts, databaseHelper.getUserSettings(userId)));
        listView.addHeaderView(headerLayout);
        listView.setAdapter(accountsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //list element the user selected, 0 being the header
                if(position != 0) {
                    //need to re-align the position, since listView(1) = accounts(2)
                    final Bundle args = new Bundle();
                    args.putInt("accountId", accounts[position-1].getId());
                    args.putString("callFrom", "Accounts");

                    accountDialog = new AccountsDialogFragment();
                    accountDialog.setArguments(args);
                    accountDialog.show(getFragmentManager(), "Account Dialog");
                }
            }
        });
    }

    //creates a hash map to calculate the total amount that each currency
    //has in all accounts, then returns a 2D string array with the information
    public String[][] getSummary(Account accounts[]) {
        //go through each account element,
        //if the currency is unique, add a new hash entry
        //if it isn't unique, add it to the existing total
        HashMap<String, Double> hashMap = new HashMap<>();
        String locale;
        double balance;

        for(Account account : accounts) {
            locale = account.getLocale() + "-" + account.getSymbol();
            balance = account.getCurrentBalance();

            if(hashMap.containsKey(locale)) {
                //currency is not unique
                hashMap.put(locale, (hashMap.get(locale) + balance));
            }
            else {
                //currency is unique
                hashMap.put(locale, balance);
            }
        }

        //convert HashMap to 2D string array
        //[][0] is the locale of the currency
        //[][1] is the currency symbol + the amount
        final String summary[][] = new String[hashMap.size()][2];
        String key;

        int i = 0;
        for(Map.Entry<String, Double> pair : hashMap.entrySet()) {
            key = pair.getKey();
            summary[i][0] = key.substring(0,key.indexOf('-'));
            summary[i][1] = key.substring(key.indexOf('-') + 1,key.length()) + pair.getValue();

            i ++;
        }

        return summary;
    }

    //creates the listView header that will show the accounts overview
    //data like total currencies for all accounts and total accounts
    @SuppressLint("SetTextI18n")
    public View createListHeader(String summary[][], Account[] accountList, HashMap<String, String> userData) {
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(24,5,24,5);

        final LinearLayout verticalLayout = new LinearLayout(getContext());
        verticalLayout.setLayoutParams(params);
        verticalLayout.setOrientation(LinearLayout.VERTICAL);
        verticalLayout.setPadding(10, 10, 10, 10);

        //header title
        final String overview = getString(R.string.overview_text);
        final TextView textViewTitle = new TextView(getContext());
        textViewTitle.setText(overview);
        textViewTitle.setTextSize(24);

        //loop through all the currencies and add them to the layout

        //number of accounts
        final String total = getString(R.string.numberofAccounts_text);
        final TextView textViewTotal = new TextView(getContext());
        textViewTotal.setText(total);
        textViewTotal.setTextSize(18);
        textViewTotal.setLayoutParams((new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)));
        textViewTotal.setGravity(Gravity.START);

        final TextView textViewTotalValue = new TextView(getContext());
        textViewTotalValue.setText(Integer.toString(accountList.length));
        textViewTotalValue.setTextSize(18);
        textViewTotalValue.setLayoutParams((new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)));
        textViewTotalValue.setGravity(Gravity.END);

        LinearLayout horizontalLayout = new LinearLayout(getContext());
        horizontalLayout.addView(textViewTotal);
        horizontalLayout.addView(textViewTotalValue);

        verticalLayout.addView(textViewTitle);
        verticalLayout.addView(horizontalLayout);

        //total amount of currency
        final String totalMoney = getString(R.string.totalAmountofMoney_text) + " (" + userData.get("locale") + ")";
        final TextView textCurrencyTotalAmount = new TextView(getContext());
        textCurrencyTotalAmount.setText(totalMoney);
        textCurrencyTotalAmount.setTextSize(18);
        textCurrencyTotalAmount.setLayoutParams((new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)));
        textCurrencyTotalAmount.setGravity(Gravity.START);

        final TextView textViewTotalAmountValue = new TextView(getContext());
        //will set text value at the very end when total is calculated
        textViewTotalAmountValue.setTextSize(18);
        textViewTotalAmountValue.setLayoutParams((new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)));
        textViewTotalAmountValue.setGravity(Gravity.END);

        horizontalLayout = new LinearLayout(getContext());
        horizontalLayout.setPadding(0,0,0,10);
        horizontalLayout.addView(textCurrencyTotalAmount);
        horizontalLayout.addView(textViewTotalAmountValue);

        //HashSet contains all of the account locales and
        //will be sent to the api call
        //will reference the locales using the HashMap for exchange
        //rates of the locales
        HashMap<String, String> hashMapData = new HashMap<>();
        final HashSet<String> hashSetLocales = new HashSet<>();
        hashSetLocales.add(userData.get("locale"));

        for(Account account : accountList) {
            hashSetLocales.add(account.getLocale());
        }

        double totalMoneyAmount = 0.00;
        double currency2 = 1;
        boolean errorFlag;

        //check to see if there's more than 1 type, then convert OR
        //if there's only 1, if its not the users locales
        //save an API call
        if(hashSetLocales.size() > 1 || (hashSetLocales.size() == 1 && !hashSetLocales.contains(userData.get("locale")))) {
            try {
                //API call to get all currency exchange rates
                hashMapData = new FixerCurrencyAPI().execute(hashSetLocales.toArray(new String[hashSetLocales.size()])).get();

                currency2 = Double.parseDouble(hashMapData.get(userData.get("locale")));

                verticalLayout.addView(horizontalLayout);

                errorFlag = false;
            }
            catch(Exception e) {
                //couldn't get conversions
                //don't show the max money entry in the header
                e.printStackTrace();
                errorFlag = true;
            }
        }
        else {
            //only one or no types of locales, no point in
            //reporting total locale currencies when there is only 1
            errorFlag = true;
        }

        TextView textViewCurrency;
        TextView textViewAmount;
        String textCurrency;

        for(String[] data : summary) {
            horizontalLayout = new LinearLayout(getContext());
            horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
            horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            textCurrency = getString(R.string.totalCurrency_text) + " (" + data[0] + ")";
            textViewCurrency = new TextView(getContext());
            textViewCurrency.setText(textCurrency);
            textViewCurrency.setTextSize(18);
            textViewCurrency.setLayoutParams((new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)));
            textViewCurrency.setGravity(Gravity.START);

            textViewAmount = new TextView(getContext());
            textViewAmount.setText(data[1]);
            textViewAmount.setTextSize(18);
            textViewAmount.setLayoutParams((new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)));
            textViewAmount.setGravity(Gravity.END);

            horizontalLayout.addView(textViewCurrency);
            horizontalLayout.addView(textViewAmount);
            verticalLayout.addView(horizontalLayout);

            if(!errorFlag) {
                //convert currency and add to total money
                double value = Double.parseDouble(data[1].substring(1));
                double currency1 = Double.parseDouble(hashMapData.get(data[0]));
                double exchangeRate = currency1 / currency2;
                totalMoneyAmount += (value / exchangeRate);
            }
        }

        //set the text of the total currency
        //will not show up if the exchange rates weren't retrieved
        textViewTotalAmountValue.setText(String.format(userData.get("symbol") + "%.2f", totalMoneyAmount));

        return verticalLayout;
    }

    //the alert dialog callback goes to the parent activity
    //still need to dismiss the dialog somehow, so the activity will get
    //the instance from calling this function
    public static DialogFragment getAlertDialog() {
        return accountDialog;
    }

    public static void setAlertDialog() {
        accountDialog = null;
    }
}