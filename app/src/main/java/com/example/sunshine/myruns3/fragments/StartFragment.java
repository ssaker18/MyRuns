package com.example.sunshine.myruns3.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.sunshine.myruns3.R;
import com.example.sunshine.myruns3.ManualEntryActivity;
import com.example.sunshine.myruns3.MapActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class StartFragment extends Fragment implements View.OnClickListener {
    public static final String FRAGMENT_NAME = "StartFragment";
    private String SOURCE = "Source";
    private Spinner mInputTypeSpinner;
    private Spinner mActivityTypeSpinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mInputTypeSpinner = view.findViewById(R.id.input_type);
        mActivityTypeSpinner = view.findViewById(R.id.activity_type);
        FloatingActionButton mStartActivity = view.findViewById(R.id.begin_exercise);

        // Create an ArrayAdapter using the string array and a custom spinner layout
        ArrayAdapter<CharSequence> input_type_adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.input_type_array, R.layout.custom_spinner);

        ArrayAdapter<CharSequence> activity_type_adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.activities_array, R.layout.custom_spinner);

        input_type_adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        activity_type_adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);

        mInputTypeSpinner.setAdapter(input_type_adapter);
        mActivityTypeSpinner.setAdapter(activity_type_adapter);


        mStartActivity.setOnClickListener(this);
        mInputTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 2){
                    mActivityTypeSpinner.setEnabled(false);
                    mActivityTypeSpinner.setBackgroundColor(Color.TRANSPARENT);
                }else{
                    mActivityTypeSpinner.setEnabled(true);
                    mActivityTypeSpinner.setBackgroundColor(((ColorDrawable) mInputTypeSpinner.getBackground()).getColor());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onClick(View v) {
        Intent intent = null;
        if (v.getId() == R.id.begin_exercise) {
            String input_type = mInputTypeSpinner.getSelectedItem().toString();
            String activity_type = mActivityTypeSpinner.getSelectedItem().toString();
            if (input_type != null && activity_type != null) {
                if (input_type.equals("Manual")) {
                    intent = new Intent(getContext(), ManualEntryActivity.class);
                    intent.putExtra("InputType", input_type);
                    intent.putExtra("Activity", activity_type);
                    intent.putExtra(SOURCE, FRAGMENT_NAME);
                } else {
                    // GPS or Automatic: Launch Map Activity
                    intent = new Intent(getContext(), MapActivity.class);
                    intent.putExtra(SOURCE, FRAGMENT_NAME);
                }
            }
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}
