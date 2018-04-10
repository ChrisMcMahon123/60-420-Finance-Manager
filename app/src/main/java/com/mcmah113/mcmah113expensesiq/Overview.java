package com.mcmah113.mcmah113expensesiq;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

public class Overview extends AppCompatActivity implements
                NavigationView.OnNavigationItemSelectedListener,
                AccountsDialogFragment.OnCompleteListener,
                AccountsEditFragment.OnCompleteListener,
                OverviewAdapter.OnCompleteListener,
                DeleteDialogFragment.OnCompleteListener,
                SettingsDialogFragment.OnCompleteListener,
                TransactionsNewFragment.OnCompleteListener,
                TransactionsTransferDialogFragment.OnCompleteListener,
                ReportsExpenseFragment.OnCompleteListener,
                ReportsIncomeFragment.OnCompleteListener,
                ReportsCashFlowFragment.OnCompleteListener,
                ReportsBalanceFragment.OnCompleteListener,
                OverviewCustomizeDialogFragment.OnCompleteListener {

    private Toolbar toolbarCustom;
    private DrawerLayout drawerLayout;
    private LinearLayout linearLayoutExpense;
    private LinearLayout linearLayoutIncome;
    private FloatingActionButton floatingActionButtonMain;
    private NavigationView navigationView;
    private DatabaseHelper databaseHelper;
    private FragmentManager fragmentManager;
    private MenuItem menuItemOverviewCustomize;

    private static int userId;
    private static String username;
    private static String currentTag;
    private static int currentAccountId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        databaseHelper = new DatabaseHelper(this);

        //get the userId passed from the login activity
        if(getIntent().getExtras() != null) {
            userId = getIntent().getExtras().getInt("userId");
            username = getIntent().getExtras().getString("username");
        }

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

        //set the fab icons and their properties
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

        final AssetManager assetManager = getAssets();
        InputStream inputStream;

        try {
            inputStream = assetManager.open("account_send.png");
            floatingActionButtonExpense.setImageBitmap(BitmapFactory.decodeStream(inputStream));

            inputStream = assetManager.open("account_send.png");
            floatingActionButtonIncome.setImageBitmap(BitmapFactory.decodeStream(inputStream));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        fragmentManager = getSupportFragmentManager();
    }

    public void onBackPressed() {
        //close the navigation drawer on back button press if its open
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            //remove the latest fragment on the stack
            //need to -2 to get to the right fragment, as the stack
            //acts like an array, count = length, starting from 1

            //weird stuff happening here
            //so the keyboard counts as a fragment...
            //which means this will hide the keyboard if its open
            //and do nothing else. When the keyboard is hidden,
            //this will pop the fragment and go back to the previous one
            if(fragmentManager.getBackStackEntryCount() > 1) {
                fragmentManager.popBackStack();
                FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount()-2);
                currentTag = backStackEntry.getName();
                toolbarCustom.setTitle(currentTag);

                menuItemOverviewCustomize.setVisible(false);

                switch (currentTag) {
                    case "Overview":
                        navigationView.setCheckedItem(R.id.navOverview);
                        menuItemOverviewCustomize.setVisible(true);
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
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_overview, menu);

        menuItemOverviewCustomize = menu.findItem(R.id.menuOverviewCustomize);

        //setting the menu textView, so have to wait until the menu is created
        final TextView textViewUsername = findViewById(R.id.textViewUsername);
        textViewUsername.setText(username);

        final Menu navMenu = navigationView.getMenu();
        final LinearLayout linearLayout = findViewById(R.id.headerBackground);
        final MenuItem menuItem1 = navMenu.findItem(R.id.navOverview);
        final MenuItem menuItem2 = navMenu.findItem(R.id.navAccounts);
        final MenuItem menuItem3 = navMenu.findItem(R.id.navTransactions);
        final MenuItem menuItem4 = navMenu.findItem(R.id.navReports);

        //setting the images for each of the menu options,
        final AssetManager assetManager = getAssets();
        InputStream inputStream;
        BitmapDrawable bitmapDrawable;

        try {
            inputStream = assetManager.open("wallpaper.jpg");
            bitmapDrawable = new BitmapDrawable(getResources(), inputStream);
            linearLayout.setBackground(bitmapDrawable);

            inputStream = assetManager.open("account_view.png");
            bitmapDrawable = new BitmapDrawable(getResources(), inputStream);
            menuItem1.setIcon(bitmapDrawable);

            inputStream = assetManager.open("type_cash.png");
            bitmapDrawable = new BitmapDrawable(getResources(), inputStream);
            menuItem2.setIcon(bitmapDrawable);

            inputStream = assetManager.open("account_send.png");
            bitmapDrawable = new BitmapDrawable(getResources(), inputStream);
            menuItem3.setIcon(bitmapDrawable);

            inputStream = assetManager.open("reports_chart2.png");
            bitmapDrawable = new BitmapDrawable(getResources(), inputStream);
            menuItem4.setIcon(bitmapDrawable);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        //need to wait until menu's are created before showing overview fragment
        setFragment(new OverviewFragment(), "Overview",  -1, "");

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menuOverviewCustomize:
                //show the overview customize screen
                //launch the settings dialog option
                OverviewCustomizeDialogFragment customizeDialog = new OverviewCustomizeDialogFragment();
                customizeDialog.show(getSupportFragmentManager(), "Customize Dialog");
                return true;
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
                settingsDialogFragment.show(getSupportFragmentManager(), "Settings Dialog");
                return true;
            default:
                return false;
        }
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //check to see if the current fragment is the one the user wants to see
        switch(item.getItemId()) {
            case R.id.navOverview:
                setFragment(new OverviewFragment(), "Overview", -1, "");
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            case R.id.navAccounts:
                setFragment(new AccountsFragment(), "Accounts",  -1, "");
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            case R.id.navTransactions:
                setFragment(new TransactionsFragment(), "Transactions", -1, "");
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            case R.id.navReports:
                setFragment(new ReportsFragment(), "Reports",  -1, "");
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            default:
                return false;
        }
    }

    //takes an accountId which will be passed to the fragment
    //through a bundle. Does not affect any existing fragments.
    //This is to allow more fragment launching that needs an account id
    public void setFragment(Fragment fragment, String tag, int accountId, String callFrom) {
        currentTag = tag;
        currentAccountId = accountId;

        toolbarCustom.setTitle(tag);

        final Bundle args = new Bundle();
        args.putInt("accountId", accountId);
        args.putString("callFrom", callFrom);

        if(tag.equals("Overview")) {
            //show the menu item to customize the fragment
            menuItemOverviewCustomize.setVisible(true);
        }
        else {
            //hide this option, not applicable
            menuItemOverviewCustomize.setVisible(false);
        }

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
        final int accountId = callback.getInt("accountId");
        final String report = callback.getString("report");

        //close the dialog since a Pos / Neg button wasn't used
        //must close it from the object, can't do it from inside the class
        if(AccountsFragment.getAlertDialog() != null) {
            AccountsFragment.getAlertDialog().dismiss();
            AccountsFragment.setAlertDialog();
        }
        else if(OverviewAdapter.getAlertDialog() != null) {
            OverviewAdapter.getAlertDialog().dismiss();
            OverviewAdapter.setAlertDialog();
        }

        if(tag != null) {
            switch(tag) {
                case "Overview":
                    navigationView.setCheckedItem(R.id.navOverview);
                    setFragment(new OverviewFragment(), tag, accountId, "");
                    break;
                case "Accounts":
                    navigationView.setCheckedItem(R.id.navAccounts);
                    setFragment(new AccountsFragment(), tag, accountId, "");
                    break;
                case "Edit Account":
                case "New Account":
                    //New Account and Edit account use the same fragment
                    navigationView.setCheckedItem(R.id.navAccounts);
                    setFragment(new AccountsEditFragment(), tag, accountId, callback.getString("callFrom"));
                    break;
                case "Delete Account":
                    //open a dialog asking the user to confirm account deletion
                    Bundle args = new Bundle();
                    args.putString("accountName",databaseHelper.getAccountInfo(accountId, userId).getName());
                    args.putInt("accountId",accountId);
                    args.putString("callFrom", callback.getString("callFrom"));

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
                    if("Overview".equals(callback.getString("callFrom"))) {
                        navigationView.setCheckedItem(R.id.navOverview);
                        setFragment(new OverviewFragment(), callback.getString("callFrom"),-1, "");
                    }
                    else if("Accounts".equals(callback.getString("callFrom"))) {
                        navigationView.setCheckedItem(R.id.navAccounts);
                        setFragment(new AccountsFragment(), callback.getString("callFrom"),-1, "");
                    }

                    break;
                case "View Transactions":
                    tag = "Transactions";
                case "Transactions":
                    navigationView.setCheckedItem(R.id.navTransactions);
                    setFragment(new TransactionsFragment(), tag, accountId, "");
                    break;
                case "Make a Transfer":
                    navigationView.setCheckedItem(R.id.navTransactions);
                    setFragment(new TransactionsTransferFragment(), tag, accountId, "");
                    break;
                case "Add Transaction":
                    navigationView.setCheckedItem(R.id.navTransactions);
                    setFragment(new TransactionsNewFragment(), tag, accountId, "");
                    break;
                case "Reports":
                    navigationView.setCheckedItem(R.id.navReports);
                    setFragment(new ReportsFragment(), tag, accountId, "");
                    break;
                case "Expense Graph":
                    navigationView.setCheckedItem(R.id.navReports);
                    setFragment(new ReportsGraphFragment(), report, accountId, "Expense");
                    break;
                case "Income Graph":
                    navigationView.setCheckedItem(R.id.navReports);
                    setFragment(new ReportsGraphFragment(), report, accountId, "Income");
                    break;
                case "Cash Flow Graph":
                    navigationView.setCheckedItem(R.id.navReports);
                    setFragment(new ReportsGraphFragment(), report, accountId, "Cash Flow");
                    break;
                case "Balance Graph":
                    navigationView.setCheckedItem(R.id.navReports);
                    setFragment(new ReportsGraphFragment(), report, accountId, "Balance");
                    break;
            }
        }
    }

    //the delete dialog has completed, delete the account if
    //the user confirmed their decision
    public void onCompleteDeleteAccount(Bundle callback) {
        if("Yes".equals(callback.getString("response"))) {
            if(databaseHelper.deleteAccount(callback.getInt("accountId"), userId)) {
                Toast.makeText(this, "Account Deleted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Failed to Delete Account", Toast.LENGTH_SHORT).show();
            }

            //refresh accounts fragment
            if("Overview".equals(callback.getString("callFrom"))) {
                navigationView.setCheckedItem(R.id.navOverview);
                setFragment(new OverviewFragment(), callback.getString("callFrom"),-1, "");
            }
            else if("Accounts".equals(callback.getString("callFrom"))) {
                navigationView.setCheckedItem(R.id.navAccounts);
                setFragment(new AccountsFragment(), callback.getString("callFrom"),-1, "");
            }
        }
    }

    //reload the current fragment that is being displayed
    public void onCompleteSettingsChange() {
        fragmentManager.popBackStack();
        final FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount()-1);
        currentTag = backStackEntry.getName();
        toolbarCustom.setTitle(currentTag);

        final Bundle args = new Bundle();
        args.putString("fragment", currentTag);
        args.putInt("accountId", currentAccountId);

        onCompleteLaunchFragment(args);
    }
}