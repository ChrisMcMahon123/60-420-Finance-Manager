package com.mcmah113.mcmah113expensesiq;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class OverviewFragment extends Fragment {
    public interface OnCompleteListener {
        void onCompleteLaunchFragment(Bundle args);
    }

    public OverviewFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    public void onViewCreated(View view, Bundle bundle) {
        final int userId = Overview.getUserId();

        final CustomOnTouchListener onTouchListener = new CustomOnTouchListener(getResources().getColor(R.color.colorPrimaryDark, getContext().getTheme()));

        final Button buttonNewAccount = view.findViewById(R.id.buttonNewAccount);
        buttonNewAccount.setOnTouchListener(onTouchListener);//ignore this warning...
        buttonNewAccount.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Bundle args = new Bundle();
                args.putInt("accountId", -1);
                args.putString("fragment", "New Account");

                final OnCompleteListener onCompleteListener = (OnCompleteListener) getActivity();
                onCompleteListener.onCompleteLaunchFragment(args);
            }
        });
    }
}