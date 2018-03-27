package com.mcmah113.mcmah113expensesiq;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ReportsIncomeFragment extends Fragment {
    public ReportsIncomeFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports_income, container, false);
    }

    public void onViewCreated(View view, Bundle bundle) {
        final ReportsAdapter reportsAdapter = new ReportsAdapter(getContext(), GlobalConstants.getIncomeReports());

        //set up the listView properties
        final ListView listView = view.findViewById(R.id.listviewIncome);
        listView.setAdapter(reportsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Bundle args = new Bundle();
                args.putString("accountId", "");

                Toast.makeText(getContext(), "Income Report at " + position, Toast.LENGTH_SHORT).show();
            }
        });
   }
}