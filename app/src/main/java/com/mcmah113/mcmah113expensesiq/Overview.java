package com.mcmah113.mcmah113expensesiq;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Overview extends AppCompatActivity implements
                NavigationView.OnNavigationItemSelectedListener,
                AccountsDialogFragment.OnCompleteListener,
                AccountsEditFragment.OnCompleteListener,
                OverviewFragment.OnCompleteListener,
                DeleteDialogFragment.OnCompleteListener,
                SettingsDialogFragment.OnCompleteListener,
                TransactionsNewFragment.OnCompleteListener,
                TransactionsTransferDialogFragment.OnCompleteListener,
                ReportsExpenseFragment.OnCompleteListener,
                ReportsIncomeFragment.OnCompleteListener,
                ReportsCashFlowFragment.OnCompleteListener,
                ReportsBalanceFragment.OnCompleteListener {

    private static int userId;
    private static String username;

    private Toolbar toolbarCustom;
    private DrawerLayout drawerLayout;
    private LinearLayout linearLayoutExpense;
    private LinearLayout linearLayoutIncome;
    private FloatingActionButton floatingActionButtonMain;
    private NavigationView navigationView;
    private DatabaseHelper databaseHelper;

    private static String currentTag;
    private static int currentAccountId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        databaseHelper = new DatabaseHelper(this);

        //get the userId passed from the login activity
        userId = getIntent().getExtras().getInt("userId");
        username = getIntent().getExtras().getString("username");

        //set toolbar properties
        toolbarCustom = findViewById(R.id.toolbarCustom);
        setSupportActionBar(toolbarCustom);

        //set the drawer layout for the navigation drawer
        drawerLayout = findViewById(R.id.drawerLayout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbarCustom, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //set the navigation view for the navigation drawer and the default fragment
        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.navOverview);

        //set the fab and their layout properties
        linearLayoutExpense = findViewById(R.id.layoutFabExpense);
        linearLayoutIncome = findViewById(R.id.layoutFabIncome);

        final CustomOnTouchListener onTouchListener = new CustomOnTouchListener(getResources().getColor(R.color.colorPrimaryDark, getTheme()));

        floatingActionButtonMain = findViewById(R.id.floatingActionButtonMain);
        floatingActionButtonMain.setOnTouchListener(onTouchListener);//ignore this warning...
        floatingActionButtonMain.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                toggleVisibility();
            }
        });

        final FloatingActionButton floatingActionButtonExpense = findViewById(R.id.floatingActionButtonExpense);
        floatingActionButtonExpense.setOnTouchListener(onTouchListener);//ignore this warning...
        floatingActionButtonExpense.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                toggleVisibility();
                //go to the transactions fragment
                setFragment(new TransactionsNewFragment(), "Add Transaction", -3);
            }
        });

        final FloatingActionButton floatingActionButtonIncome = findViewById(R.id.floatingActionButtonIncome);
        floatingActionButtonIncome.setOnTouchListener(onTouchListener);//ignore this warning...
        floatingActionButtonIncome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                toggleVisibility();
                //go to the transactions fragment
                setFragment(new TransactionsNewFragment(), "Add Transaction", -2);
            }
        });

        setFragment(new OverviewFragment(), "Overview",  -1);
    }

    public void onBackPressed() {
        //close the navigation drawer on back button press if its open
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            //currently showing Edit Account
            //put the accounts overview up when back is pressed
            if(getSupportFragmentManager().findFragmentByTag("Edit Account") != null) {
                setFragment(new AccountsFragment(), "Accounts",  -1);
            }
            else if(getSupportFragmentManager().findFragmentByTag("New Account") != null) {
                setFragment(new OverviewFragment(), "Overview",  -1);
            }
            else if(getSupportFragmentManager().findFragmentByTag("Make a Transfer") != null) {
                setFragment(new AccountsFragment(), "Accounts",  -1);
            }
        }

        hideKeyboard(navigationView);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_overview, menu);

        //setting the menu textView, so have to wait until the menu is created
        final TextView textViewUsername = findViewById(R.id.textViewUsername);
        textViewUsername.setText(username);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menuLogout:
                //launch the login activity
                Intent intent = new Intent(this, Login.class);
                intent.putExtra("username", username);
                startActivity(intent);
                Toast.makeText(this, "Successfully logged out", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menuSettings:
                //launch the settings dialog option
                SettingsDialogFragment settingsDialogFragment = new SettingsDialogFragment();
                settingsDialogFragment.show(getFragmentManager(), "Delete Dialog");
                return true;
            default:
                return false;
        }
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //check to see if the current fragment is the one the user wants to see
        switch(item.getItemId()) {
            case R.id.navOverview:
                setFragment(new OverviewFragment(), "Overview", -1);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            case R.id.navAccounts:
                setFragment(new AccountsFragment(), "Accounts",  -1);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            case R.id.navTransactions:
                setFragment(new TransactionsFragment(), "Transactions", -1);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            case R.id.navReports:
                setFragment(new ReportsFragment(), "Reports",  -1);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            default:
                return false;
        }
    }

    //takes an accountId which will be passed to the fragment
    //through a bundle. Does not affect any existing fragments.
    //This is to allow more fragment launching that needs an account id
    public void setFragment(Fragment fragment, String tag, int accountId) {
        currentTag = tag;
        currentAccountId = accountId;

        toolbarCustom.setTitle(tag);

        final Bundle args = new Bundle();
        args.putInt("accountId", accountId);

        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.disallowAddToBackStack();
        fragmentTransaction.replace(R.id.fragmentLocation, fragment, tag);
        fragmentTransaction.commit();

        drawerLayout.closeDrawer(GravityCompat.START);
    }

    public void toggleVisibility() {
        //toggle the visibility of the fab icons when clicked
        int visibility;

        if(linearLayoutExpense.getVisibility() == View.VISIBLE || linearLayoutIncome.getVisibility() == View.VISIBLE) {
            visibility = View.INVISIBLE;
            floatingActionButtonMain.setImageResource(R.drawable.ic_add_black_24dp);
        }
        else {
            visibility = View.VISIBLE;
            floatingActionButtonMain.setImageResource(R.drawable.ic_close_black_24dp);
        }

        linearLayoutIncome.setVisibility(visibility);
        linearLayoutExpense.setVisibility(visibility);
    }

    public static int getUserId() {
        return userId;
    }

    public static String getUsername() {
        return username;
    }

    public void onCompleteAccountDialog(Bundle callbackData) {
        final int accountId = callbackData.getInt("accountId");
        final String selection = callbackData.getString("selection");

        //close the dialog since a Pos / Neg button wasn't used
        AccountsFragment.getAlertDialog().dismiss();

        final Bundle args = new Bundle();
        args.putInt("accountId",accountId);

        //launch the new fragment / view
        switch(selection) {
            case "View Transactions":
                setFragment(new TransactionsFragment(), selection, accountId);
                break;
            case "Make a Transfer":
                setFragment(new TransactionsTransferFragment(), selection, accountId);
                break;
            case "Add Transaction":
                setFragment(new TransactionsNewFragment(), selection, accountId);
                break;
            case "Edit Account":
                setFragment(new AccountsEditFragment(), selection, accountId);
                break;
            case "Delete Account":
                //confirm that the user really wants to delete this account
                args.putString("accountName",databaseHelper.getAccountInfo(accountId, userId).getName());
                DeleteDialogFragment deleteDialogFragment = new DeleteDialogFragment();
                deleteDialogFragment.setArguments(args);
                deleteDialogFragment.show(getSupportFragmentManager(), "Delete Dialog");
                break;
            case "Hide Account":
                if(databaseHelper.hideAccount(accountId, userId)) {
                    Toast.makeText(this, "Account Hidden", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this, "Faied to hide account", Toast.LENGTH_SHORT).show();
                }
                //refresh accounts fragment
                setFragment(new AccountsFragment(), "Accounts",-1);
                break;
        }
    }

    public void onCompleteCreateNewTransaction() {
        //return to accounts
        setFragment(new AccountsFragment(), "Accounts", -1);
    }

    public void onCompleteCreateNewAccount() {
        //go to the edit accounts fragment to create a new account
        setFragment(new AccountsEditFragment(), "New Account", -1);
    }

    public void onCompleteAccountEdit() {
        //return to accounts
        setFragment(new AccountsFragment(), "Accounts", -1);
    }

    public void onCompleteDeleteAccount(Bundle callbackData) {
        if(callbackData.getString("response").equals("Yes")) {
            if(databaseHelper.deleteAccount(callbackData.getInt("accountId"), userId)) {
                Toast.makeText(this, "Account Deleted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show();
            }
            setFragment(new AccountsFragment(), "Accounts",-1);
        }
    }

    //reload the current fragment that is being displayed
    public void onCompleteUserSettings() {
        switch(currentTag) {
            case "Overview":
                setFragment(new OverviewFragment(), currentTag, currentAccountId);
                break;
            case "Accounts":
                setFragment(new AccountsFragment(), currentTag, currentAccountId);
                break;
            case "Edit Account":
            case "New Account":
                setFragment(new AccountsEditFragment(), currentTag, currentAccountId);
                break;
            case "Transactions":
                setFragment(new TransactionsFragment(), currentTag, currentAccountId);
                break;
            case "Add Transaction":
                setFragment(new TransactionsNewFragment(), currentTag, currentAccountId);
                break;
            case "Make a Transfer":
                setFragment(new TransactionsTransferFragment(), currentTag, currentAccountId);
                break;
            case "Reports":
                setFragment(new ReportsFragment(), currentTag, currentAccountId);
                break;
            default:
                setFragment(null, currentTag, currentAccountId);
        }

    }

    public void onCompleteTransferFunds() {
        setFragment(new AccountsFragment(), currentTag, currentAccountId);
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if(inputManager != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    //one of the reports was selected, lets find out which one and launch it
    public void onReportsSelection(Bundle callbackData) {
        switch (callbackData.getString("report")) {
            case "Expense by Category":
            case "Daily Expense":
            case "Monthly Expense":
                setFragment(new ReportsExpenseGraph(), callbackData.getString("report"),-1);
                break;
            case "Income by Category":
            case "Daily Income":
            case "Monthly Income":
                setFragment(new ReportsIncomeGraph(), callbackData.getString("report"),-1);
                break;
            case "Income Vs Expense":
                setFragment(new ReportsCashFlowGraph(), callbackData.getString("report"),-1);
                break;
            case "Daily Balance":
                setFragment(new ReportsBalanceGraph(), callbackData.getString("report"),-1);
                break;
        }
    }
}