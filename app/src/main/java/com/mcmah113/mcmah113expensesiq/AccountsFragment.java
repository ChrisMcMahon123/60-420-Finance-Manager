package com.mcmah113.mcmah113expensesiq;

import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

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
        insertionPoint.addView(createListHeader(summary, accounts.length));
        listView.addHeaderView(headerLayout);
        listView.setAdapter(accountsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //list element the user selected, 0 being the header
                if(position != 0) {
                    //need to re-align the position, since listView(1) = accounts(2)
                    final Bundle args = new Bundle();
                    args.putInt("accountId", accounts[position-1].getId());

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
        Double doubleObject;
        String locale;
        double balance;

        for(Account account : accounts) {
            locale = account.getLocale() + "-" + account.getSymbol();
            balance = account.getCurrentBalance();

            if(hashMap.containsKey(locale)) {
                //currency is not unique
                doubleObject = hashMap.get(locale);
                doubleObject += balance;
                hashMap.put(locale, doubleObject);
            }
            else {
                //currency is unique
                doubleObject = balance;
                hashMap.put(locale, doubleObject);
            }
        }

        //convert HashMap to 2D string array
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
    public View createListHeader(String summary[][], int totalAccounts) {
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(24,5,24,5);

        final LinearLayout verticalLayout = new LinearLayout(getContext());
        verticalLayout.setLayoutParams(params);
        verticalLayout.setOrientation(LinearLayout.VERTICAL);
        verticalLayout.setPadding(10, 10, 10, 10);

        final String overview = "Overview";
        final TextView textViewTitle = new TextView(getContext());
        textViewTitle.setText(overview);
        textViewTitle.setTextSize(24);

        final String total = "Number of Accounts: " + totalAccounts;
        final TextView textViewTotal = new TextView(getContext());
        textViewTotal.setText(total);
        textViewTotal.setTextSize(18);
        textViewTotal.setPadding(0,0,0,10);

        verticalLayout.addView(textViewTitle);
        verticalLayout.addView(textViewTotal);

        //loop through all the currencies and add them to the layout
        LinearLayout horizontalLayout;

        TextView textViewCurrency;
        TextView textViewAmount;
        String textCurrency;
        String textAmount;

        for(String[] data : summary) {
            horizontalLayout = new LinearLayout(getContext());
            horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
            horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            textCurrency = "Currency: " + data[0];
            textViewCurrency = new TextView(getContext());
            textViewCurrency.setText(textCurrency);
            textViewCurrency.setTextSize(18);
            textViewCurrency.setLayoutParams((new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)));
            textViewCurrency.setGravity(Gravity.START);

            textAmount = " Total: " + data[1];
            textViewAmount = new TextView(getContext());
            textViewAmount.setText(textAmount);
            textViewAmount.setTextSize(18);
            textViewAmount.setLayoutParams((new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)));
            textViewAmount.setGravity(Gravity.END);

            horizontalLayout.addView(textViewCurrency);
            horizontalLayout.addView(textViewAmount);
            verticalLayout.addView(horizontalLayout);
        }

        return verticalLayout;
    }

    //the alert dialog callback goes to the parent activity
    //still need to dismiss the dialog somehow, so the activity will get
    //the instance from calling this function
    public static DialogFragment getAlertDialog() {
        return accountDialog;
    }
}