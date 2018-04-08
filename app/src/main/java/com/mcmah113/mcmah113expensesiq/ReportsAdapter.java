package com.mcmah113.mcmah113expensesiq;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

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

                //setting the images from the assets folder for each option
                final AssetManager assetManager = row.getContext().getAssets();
                InputStream inputStream;

                final ImageView imageViewReportImage = row.findViewById(R.id.imageViewReportPicture);

                try {
                    if(position == 0) {
                        inputStream = assetManager.open("reports_chart.png");
                    }
                    else {
                        inputStream = assetManager.open("reports_chart2.png");
                    }

                    imageViewReportImage.setImageBitmap(BitmapFactory.decodeStream(inputStream));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return row;
    }
}
