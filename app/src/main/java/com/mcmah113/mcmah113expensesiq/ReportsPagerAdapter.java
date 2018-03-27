package com.mcmah113.mcmah113expensesiq;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ReportsPagerAdapter extends FragmentPagerAdapter {
    ReportsPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    //returns a specific fragment based on what tab is being displayed
    public Fragment getItem(int position) {
        ReportsFragment.setPosition(position);

        switch (position) {
            case 0:
                return new ReportsExpenseFragment();
            case 1:
                return new ReportsIncomeFragment();
            case 2:
                return new ReportsCashFlowFragment();
            case 3:
                return new ReportsBalanceFragment();
            default:
                return null;
        }
    }

    //returns the number of tabs the tab view has
    public int getCount() {
        return 4;
    }
}