package com.mcmah113.mcmah113expensesiq;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class ReportsIncomeFragment extends Fragment {
    public interface OnCompleteListener {
        void onCompleteLaunchFragment(Bundle args);
    }

    public ReportsIncomeFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports_income, container, false);
    }

    public void onViewCreated(View view, Bundle bundle) {
        final OnCompleteListener onCompleteListener = (OnCompleteListener) getActivity();
        final ReportsAdapter reportsAdapter = new ReportsAdapter(getContext(), GlobalConstants.getIncomeReports());

        //set up the listView properties
        final ListView listView = view.findViewById(R.id.listviewIncome);
        listView.setAdapter(reportsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Bundle args = new Bundle();
                args.putString("fragment","Income Graph");
                args.putInt("accountId", -1);

                if(position == 0) {
                    //Expense by Category
                    args.putString("report", "Income by Category");
                }
                else if(position == 1) {
                    //Daily Expense
                    args.putString("report", "Daily Income");
                }
                else if(position == 2) {
                    //Monthly Expense
                    args.putString("report", "Monthly Income");
                }

                onCompleteListener.onCompleteLaunchFragment(args);
            }
        });
   }
}