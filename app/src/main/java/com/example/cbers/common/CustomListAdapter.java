package com.example.cbers.common;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.cbers.R;

import java.util.List;

public class CustomListAdapter<S> extends ArrayAdapter<String> {

    private Context mContext;
    private int id;
    private List<String> items;

    public CustomListAdapter(Context context, int textViewResourceId, List<String> list) {
        super(context, textViewResourceId, list);
        mContext = context;
        id = textViewResourceId;
        items = list;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        View mView = v;
        if (mView == null) {
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mView = vi.inflate(id, null);
        }

        TextView text = (TextView) mView.findViewById(R.id.itemListText);

        try {
            if (items.get(position) != null) {
                String item = items.get(position);
                int color = Color.BLACK;
                String[] itemArr = item.split(":");
                String itemName = itemArr[0];
                String itemValue = itemArr[1];

                if (item.contains(StatusListItem.TEMPERATURE.toString())) {
                    int temperature = Integer.parseInt(itemValue.trim());
                    if (temperature > 102) {
                        color = Color.argb( 175, 255, 0, 0);
                    } else if (temperature > 100) {
                        color = Color.argb( 75, 255, 0, 0);
                    } else {
                        color = Color.argb( 100, 96, 182, 63);
                    }

                } else if (item.contains(StatusListItem.HEART_RATE.toString())) {
                    int heartRate = Integer.parseInt(itemValue.trim());
                    if (heartRate > 140) {
                        color = Color.argb( 175, 255, 0, 0);
                    } else if (heartRate > 100) {
                        color = Color.argb( 75, 255, 0, 0);
                    } else {
                        color = Color.argb( 100, 96, 182, 63);
                    }

                } else if (item.contains(StatusListItem.BP.toString())) {
                    String[] bp = itemValue.trim().split("/");
                    int bpLow = Integer.parseInt(bp[0]);
                    int bpHigh = Integer.parseInt(bp[1]);
                    if (bpLow < 60 || bpHigh > 200) {
                        color = Color.argb( 175, 255, 0, 0);
                    } else if (bpLow < 80 || bpHigh > 150) {
                        color = Color.argb( 75, 255, 0, 0);
                    } else {
                        color = Color.argb( 100, 96, 182, 63);
                    }

                } else if (item.contains(StatusListItem.SUGAR.toString())) {
                    int bloodSugar = Integer.parseInt(itemValue.trim());
                    if (bloodSugar > 350) {
                        color = Color.argb( 175, 255, 0, 0);
                    } else if (bloodSugar > 200) {
                        color = Color.argb( 75, 255, 0, 0);
                    } else {
                        color = Color.argb( 100, 96, 182, 63);
                    }
                }


                text.setText(item);
                text.setBackgroundColor(color);
                text.animate();

            }
        } catch (Exception e) {
            e.printStackTrace();
            text.setText(items.get(position));
        }

        return mView;
    }

}