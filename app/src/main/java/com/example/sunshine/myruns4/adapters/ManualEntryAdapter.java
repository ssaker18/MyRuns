package com.example.sunshine.myruns4.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sunshine.myruns4.R;
import com.example.sunshine.myruns4.models.ManualEntryModel;

import java.util.ArrayList;

public class ManualEntryAdapter extends ArrayAdapter<ManualEntryModel> {
    private ArrayList<ManualEntryModel> mItems;
    private Context context;

    /*
     * Custom Adapter constructor, calls super class constructor
     * Sets items, and context
     */
    public ManualEntryAdapter(@NonNull Context context, ArrayList<ManualEntryModel> items) {
        super(context, 0, items);
        this.mItems = items;
        this.context = context;
    }

    /*
     * Specifies layout for each entry item and sets the title and data fields
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ManualEntryModel item = mItems.get(position);
        convertView = LayoutInflater.from(context).inflate(R.layout.manual_entry_item,parent, false);
        TextView title = convertView.findViewById(R.id.title);
        TextView data =  convertView.findViewById(R.id.data);
        title.setText(item.getTitle());
        data.setText(item.getData());

        // Remove clicking feature for Activity type item
        if (item.getTitle().equals("Activity")) convertView.setOnClickListener(null);
        return convertView;
    }

}
