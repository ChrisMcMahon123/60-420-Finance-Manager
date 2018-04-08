package com.mcmah113.mcmah113expensesiq;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class OverviewFragment extends Fragment {
    public OverviewFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    public void onViewCreated(View view, Bundle bundle) {
        final DatabaseHelper databaseHelper = new DatabaseHelper(getContext());

        final int userId = Overview.getUserId();

        final Account accountsList[] = databaseHelper.getAccountList(userId);

        final OverviewAdapter recyclerAdapter = new OverviewAdapter(accountsList);
        final RecyclerView recyclerView = view.findViewById(R.id.recyclerViewAccounts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setAdapter(recyclerAdapter);
    }
}