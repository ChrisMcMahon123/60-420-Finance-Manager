package com.mcmah113.mcmah113expensesiq;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

    private Toolbar toolbarCustom;
    private DrawerLayout drawerLayout;
    private LinearLayout linearLayoutExpense;
    private LinearLayout linearLayoutIncome;
    private FloatingActionButton floatingActionButtonMain;
    private NavigationView navigationView;
    private DatabaseHelper databaseHelper;
    FragmentManager fragmentManager;

    private static int userId;
    private static String username;
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
                final Bundle args = new Bundle();
                args.putString("fragment", "Add Transaction");
                args.putInt("accountId", -3);
                onCompleteLaunchFragment(args);
            }
        });

        final FloatingActionButton floatingActionButtonIncome = findViewById(R.id.floatingActionButtonIncome);
        floatingActionButtonIncome.setOnTouchListener(onTouchListener);//ignore this warning...
        floatingActionButtonIncome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                toggleVisibility();
                //go to the transactions fragment
                final Bundle args = new Bundle();
                args.putString("fragment", "Add Transaction");
                args.putInt("accountId", -2);
                onCompleteLaunchFragment(args);
            }
        });

        fragmentManager = getSupportFragmentManager();

        setFragment(new OverviewFragment(), "Overview",  -1);
    }

    public void onBackPressed() {
        //close the navigation drawer on back button press if its open
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            //remove the latest fragment on the stack
            //need to -2 to get to the right fragment
            if(fragmentManager.getBackStackEntryCount() > 1) {
                fragmentManager.popBackStack();
                FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount()-2);
                currentTag = backStackEntry.getName();
                toolbarCustom.setTitle(currentTag);

                switch (currentTag) {
                    case "Overview":
                        navigationView.setCheckedItem(R.id.navOverview);
                        break;
                    case "Accounts":
                        navigationView.setCheckedItem(R.id.navAccounts);
                        break;
                    case "Transactions":
                        navigationView.setCheckedItem(R.id.navTransactions);
                        break;
                    case "Reports":
                        navigationView.setCheckedItem(R.id.navReports);
                        break;
                }
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
        fragmentTransaction.addToBackStack(tag);
        fragmentTransaction.replace(R.id.fragmentLocation, fragment, tag);
        fragmentTransaction.commit();

        drawerLayout.closeDrawer(GravityCompat.START);
    }

    //toggle the visibility of the fab icons when the main one is clicked
    //if they are visible change the main fab icon to an x to close
    //if they are hidden change the main fab icon to + to open
    public void toggleVisibility() {
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

    //instead of passing the userId to every single fragment
    //use a static function to get the userId whenever its needed
    public static int getUserId() {
        return userId;
    }

    //is the bridge between setFragment() and the other fragments
    //can't make setFragment() as static, so this callback will
    //be used to determine what fragment to be shown when another
    //fragment is done its tasks
    public void onCompleteLaunchFragment(Bundle callback) {
        String tag = callback.getString("fragment");
        int accountId = callback.getInt("accountId");
        String report = callback.getString("report");

        //close the dialog since a Pos / Neg button wasn't used
        if(AccountsFragment.getAlertDialog() != null) {
            AccountsFragment.getAlertDialog().dismiss();
            AccountsFragment.setAlertDialog();
        }

        switch(tag) {
            case "Overview":
                navigationView.setCheckedItem(R.id.navOverview);
                setFragment(new OverviewFragment(), tag, accountId);
                break;
            case "Accounts":
                navigationView.setCheckedItem(R.id.navAccounts);
                setFragment(new AccountsFragment(), tag, accountId);
                break;
            case "Edit Account":
            case "New Account":
                //New Account and Edit account use the same fragment
                navigationView.setCheckedItem(R.id.navAccounts);
                setFragment(new AccountsEditFragment(), tag, accountId);
                break;
            case "Delete Account":
                //open a dialog asking the user to confirm account deletion
                Bundle args = new Bundle();
                args.putString("accountName",databaseHelper.getAccountInfo(accountId, userId).getName());

                DeleteDialogFragment deleteDialogFragment = new DeleteDialogFragment();
                deleteDialogFragment.setArguments(args);
                deleteDialogFragment.show(getSupportFragmentManager(), "Delete Dialog");
                navigationView.setCheckedItem(R.id.navReports);
                break;
            case "Hide Account":
                if(databaseHelper.hideAccount(accountId, userId)) {
                    Toast.makeText(this, "Account Hidden", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this, "Failed to hide account", Toast.LENGTH_SHORT).show();
                }
                //refresh accounts fragment
                navigationView.setCheckedItem(R.id.navAccounts);
                setFragment(new AccountsFragment(), "Accounts",-1);
                break;
            case "View Transactions":
                tag = "Transactions";
            case "Transactions":
                navigationView.setCheckedItem(R.id.navTransactions);
                setFragment(new TransactionsFragment(), tag, accountId);
                break;
            case "Make a Transfer":
                navigationView.setCheckedItem(R.id.navTransactions);
                setFragment(new TransactionsTransferFragment(), tag, accountId);
                break;
            case "Add Transaction":
                navigationView.setCheckedItem(R.id.navTransactions);
                setFragment(new TransactionsNewFragment(), tag, accountId);
                break;
            case "Reports":
                navigationView.setCheckedItem(R.id.navReports);
                setFragment(new ReportsFragment(), tag, accountId);
                break;
            case "Expense Graph":
                switch(report) {
                    case "Expense by Category":
                        accountId = -1;
                        break;
                    case "Daily Expense":
                        accountId = -2;
                        break;
                    case "Monthly Expense":
                        accountId = -3;
                        break;
                }

                navigationView.setCheckedItem(R.id.navReports);
                setFragment(new ReportsExpenseGraph(), report, accountId);
                break;
            case "Income Graph":
                switch (report) {
                    case "Income by Category":
                        accountId = -1;
                        break;
                    case "Daily Income":
                        accountId = -2;
                        break;
                    case "Monthly Income":
                        accountId = -3;
                        break;
                }

                navigationView.setCheckedItem(R.id.navReports);
                setFragment(new ReportsIncomeGraph(), report, accountId);
                break;
            case "Cash Flow Graph":
                navigationView.setCheckedItem(R.id.navReports);
                setFragment(new ReportsCashFlowGraph(), "Income Vs Expense",accountId);
                break;
            case "Balance Income":
                navigationView.setCheckedItem(R.id.navReports);
                setFragment(new ReportsBalanceGraph(), "Daily Balance",accountId);
                break;
        }
    }

    //the delete dialog has completed, delete the account if
    //the user confirmed their decision
    public void onCompleteDeleteAccount(Bundle callbackData) {
        if("Yes".equals(callbackData.getString("response"))) {
            if(databaseHelper.deleteAccount(callbackData.getInt("accountId"), userId)) {
                Toast.makeText(this, "Account Deleted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Failed to Delete Account", Toast.LENGTH_SHORT).show();
            }

            //refresh accounts fragment
            setFragment(new AccountsFragment(), "Accounts", -1);
        }
    }

    //reload the current fragment that is being displayed
    public void onCompleteUserSettings() {
        fragmentManager.popBackStack();
        final FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount()-1);
        currentTag = backStackEntry.getName();
        toolbarCustom.setTitle(currentTag);

        final Bundle args = new Bundle();
        args.putString("fragment", currentTag);
        args.putInt("accountId", currentAccountId);

        onCompleteLaunchFragment(args);
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if(inputManager != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}