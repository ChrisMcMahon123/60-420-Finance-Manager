package com.mcmah113.mcmah113expensesiq;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ReportsAdapter extends ArrayAdapter<String[]> {
    ReportsAdapter(Context context, String[][] entries) {
        super(context,0, entries);
    }

    public @NonNull
    View getView(int position, View row, @Nullable ViewGroup parent) {
        if(row == null) {
            //will contain info about that specific account
            final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            row = layoutInflater.inflate(R.layout.layout_listview_report_item, null);

            final String data[] = getItem(position);

            if(data != null) {
                final TextView textViewTitle = row.findViewById(R.id.textViewTitle);
                textViewTitle.setText(data[0]);

                final TextView textViewDescription = row.findViewById(R.id.textViewDescription);
                textViewDescription.setText(data[1]);

                final ImageView imageViewReportImage = row.findViewById(R.id.imageViewReportPicture);
                //imageViewReportImage.setImageBitmap();
            }
        }

        return row;
    }
}
