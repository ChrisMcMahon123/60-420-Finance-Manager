package com.mcmah113.mcmah113expensesiq;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class ReportsFragment extends Fragment {
    public static int listPosition = 0;

    public ReportsFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports, container, false);
    }

    public void onViewCreated(View view, Bundle bundle) {
        final int userId = Overview.getUserId();
        final String username = Overview.getUsername();

        ReportsPagerAdapter reportsPagerAdapter = new ReportsPagerAdapter(getActivity().getSupportFragmentManager());

        ViewPager viewPager = view.findViewById(R.id.container);
        viewPager.setAdapter(reportsPagerAdapter);

        TabLayout tabLayout = view.findViewById(R.id.tabs);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
    }

    public static void setPosition(int position) {
        listPosition = position;
        Log.d("position", "" + listPosition);
    }
}