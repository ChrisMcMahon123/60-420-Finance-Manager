package com.mcmah113.mcmah113expensesiq;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ReportsBalanceFragment extends Fragment {
    public ReportsBalanceFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports_balance, container, false);
    }

    public void onViewCreated(View view, Bundle bundle) {
        final TextView textView = view.findViewById(R.id.textView);
        textView.setText(Overview.getUsername() + " id:" + Overview.getUserId());
    }
}