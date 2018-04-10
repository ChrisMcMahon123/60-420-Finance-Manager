package com.mcmah113.mcmah113expensesiq;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class ReportsBalanceFragment extends Fragment {
    public interface OnCompleteListener {
        void onCompleteLaunchFragment(Bundle args);
    }

    public ReportsBalanceFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports_balance, container, false);
    }

    public void onViewCreated(View view, Bundle bundle) {
        final OnCompleteListener onCompleteListener = (OnCompleteListener) getActivity();
        final ReportsAdapter reportsAdapter = new ReportsAdapter(getContext(), GlobalConstants.getBalanceReports());

        //set up the listView properties
        final ListView listView = view.findViewById(R.id.listviewBalance);
        listView.setAdapter(reportsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Bundle args = new Bundle();
                args.putString("fragment","Balance Graph");

                if(position == 0) {
                    args.putString("report", "Daily Balance");
                    args.putInt("accountId", -1);
                }

                onCompleteListener.onCompleteLaunchFragment(args);
            }
        });
    }
}