package com.mcmah113.mcmah113expensesiq;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class ReportsExpenseFragment extends Fragment {
    public interface OnCompleteListener {
        void onReportsSelection(Bundle callbackData);
    }

    public ReportsExpenseFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports_expense, container, false);
    }

    public void onViewCreated(View view, Bundle bundle) {
        final OnCompleteListener onCompleteListener = (OnCompleteListener) getActivity();
        final ReportsAdapter reportsAdapter = new ReportsAdapter(getContext(), GlobalConstants.getExpenseReports());

        //set up the listView properties
        final ListView listView = view.findViewById(R.id.listviewExpense);
        listView.setAdapter(reportsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle args = new Bundle();

                if(position == 0) {
                    //Expense by Category
                    args.putString("report", "Expense by Category");
                }
                else if(position == 1) {
                    //Daily Expense
                    args.putString("report", "Daily Expense");
                }
                else if(position == 2) {
                    //Monthly Expense
                    args.putString("report", "Monthly Expense");
                }

                onCompleteListener.onReportsSelection(args);
            }
        });
    }
}