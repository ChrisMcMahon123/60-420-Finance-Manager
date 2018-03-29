package com.mcmah113.mcmah113expensesiq;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ReportsFragment extends Fragment {
    public ReportsFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports, container, false);
    }

    public void onViewCreated(View view, Bundle bundle) {
        final ReportsPagerAdapter reportsPagerAdapter = new ReportsPagerAdapter(getActivity().getSupportFragmentManager());

        final ViewPager viewPager = view.findViewById(R.id.container);
        viewPager.setAdapter(reportsPagerAdapter);
        viewPager.setCurrentItem(GlobalConstants.getReportsPosition(), true);

        final TabLayout tabLayout = view.findViewById(R.id.tabs);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
    }
}