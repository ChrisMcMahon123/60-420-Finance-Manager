package com.mcmah113.mcmah113expensesiq;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class TransactionsFragment extends Fragment {
    public TransactionsFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transactions, container, false);
    }

    public void onViewCreated(View view, Bundle bundle) {
        final int userId = Overview.getUserId();
        final String username = Overview.getUsername();
    }
}