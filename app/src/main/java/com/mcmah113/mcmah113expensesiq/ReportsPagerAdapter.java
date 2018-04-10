package com.mcmah113.mcmah113expensesiq;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ReportsPagerAdapter extends FragmentStatePagerAdapter {
    ReportsPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    //returns a specific fragment based on what tab is being displayed
    public Fragment getItem(int position) {
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

    //returns the number of tabs
    public int getCount() {
        return 4;
    }

    //don't do anything, which will result in a complete redraw of the tab layout
    public void restoreState(Parcelable state, ClassLoader loader) {}
}