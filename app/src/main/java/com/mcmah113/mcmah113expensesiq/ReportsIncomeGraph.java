package com.mcmah113.mcmah113expensesiq;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ReportsIncomeGraph extends Fragment {
    public ReportsIncomeGraph() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports_income_graph, container, false);
    }
}