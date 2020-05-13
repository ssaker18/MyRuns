package com.example.sunshine.myruns4;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sunshine.myruns4.adapters.ManualEntryAdapter;
import com.example.sunshine.myruns4.constants.MyConstants;
import com.example.sunshine.myruns4.database.DeleteExerciseTask;
import com.example.sunshine.myruns4.database.ExerciseDataSource;
import com.example.sunshine.myruns4.database.ExerciseInsertTask;
import com.example.sunshine.myruns4.database.ExerciseListLoader;
import com.example.sunshine.myruns4.fragments.HistoryFragment;
import com.example.sunshine.myruns4.fragments.StartFragment;
import com.example.sunshine.myruns4.models.ExerciseEntry;
import com.example.sunshine.myruns4.models.ManualEntryModel;

import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.stream.Collectors;

import static android.widget.AdapterView.*;

public class ManualEntryActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<ArrayList<ExerciseEntry>> {
    private static final String ENTRY_VALUES = "Entries";
    private static final String ENTRY_TITLES = "Titles";
    private static final String EXERCISE_ENTRY_ID = "id";
    private static final String TAG = "ManualEntryActivity";
    private static final int FETCH_SINGLE_EXERCISE_ID = 1;
    public static final double MILE_CONVERSION_RATE = 1.609;
    public static final String IMPERIAL_MILES = "Imperial (Miles)";

    public String SOURCE = "Source";

    private TextView mDataView;
    private EditText mInputLayout;
    private Calendar mCalendar;

    private ArrayList<ManualEntryModel> mItems;
    private ManualEntryAdapter mManualEntryAdapter;

    private ExerciseDataSource mDataSource;
    private DeleteExerciseTask mDeleteTask;
    private String mDistanceUnitPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_entry);

        // New calendar with current year and time
        mCalendar = Calendar.getInstance();

        // Set up action bar with back button
        setUpActionbar();

        // Initialise DeleteTask
        mDeleteTask = new DeleteExerciseTask(this);

        // Grab ListView by Id
        ListView mListView = findViewById(R.id.list_view);

        // set up adapter with items array, and bind to ListView
        mItems = new ArrayList<>();
        mManualEntryAdapter = new ManualEntryAdapter(this, mItems);
        mListView.setAdapter(mManualEntryAdapter);

        // Set up UI, depending on where we came from
        Intent source = getIntent();
        if (source != null) {
            switch (source.getStringExtra(SOURCE)) {
                case StartFragment.FRAGMENT_NAME:
                    // wire up ListView adapter items with ManualEntry Models
                    if (savedInstanceState != null) {
                        restoreAdapterArray(savedInstanceState);
                    } else {
                        fillUpAdapterArray(null);
                    }
                    // Define the listener interface for ListView
                    OnItemClickListener mListener = (parent, view, position, id) -> displayDialog(view, position);

                    // set OnItemClickListener to ListVew
                    mListView.setOnItemClickListener(mListener);
                    break;

                case HistoryFragment.FRAGMENT_NAME:
                    // disable Exercise edits
                    mListView.setOnItemClickListener(null);
                    // start AsyncTaskLoader to fetch the data from the DB
                    LoaderManager mLoader = LoaderManager.getInstance(this);
                    mLoader.initLoader(FETCH_SINGLE_EXERCISE_ID, null, this).forceLoad();
                    break;
            }
        }

        // wire up database
        mDataSource = new ExerciseDataSource(this);
        mDataSource.open();
    }

    /*
     * Enables Back button and set action bar title
     */
    private void setUpActionbar() {
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Manual Entry Activity");
    }


    /*
     * Opens DB before Activity becomes active
     */
    @Override
    protected void onResume() {
        mDataSource.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mDataSource.close();
        super.onPause();
    }


    /*
     * Handles Adapter array data across different screen rotations
     */
    private void restoreAdapterArray(Bundle savedInstanceState) {
        ArrayList<String> values = savedInstanceState.getStringArrayList(ENTRY_VALUES);
        ArrayList<String> titles = savedInstanceState.getStringArrayList(ENTRY_TITLES);
        if (values == null || titles == null) {
            return;
        }
        for (int index = 0; index < values.size(); index++) {
            mItems.add(new ManualEntryModel(titles.get(index), values.get(index)));
        }
    }

    /*
     * Saves the Activity Titles and their respective values in an Array
     * placed in the outState
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<String> items = (ArrayList<String>) mItems.stream()
                .map(ManualEntryModel::getData)
                .collect(Collectors.toList());
        ArrayList<String> titles = (ArrayList<String>) mItems.stream()
                .map(ManualEntryModel::getTitle)
                .collect(Collectors.toList());
        outState.putStringArrayList(ENTRY_VALUES, items);
        outState.putStringArrayList(ENTRY_TITLES, titles);

    }

    /*
     * Displays a custom dialog box specific to the item selected
     */
    private void displayDialog(View view, int position) {
        if (view != null) {
            TextView titleView = view.findViewById(R.id.title);
            mDataView = view.findViewById(R.id.data);
            String title = titleView.getText().toString();

            switch (title) {
                case "Activity":
                    // do nothing, disabled: user can't edit
                    break;
                case "Date":
                    // launch date dialog and set data to date if chosen
                    showDatePickerDialog();
                    break;
                case "Time":
                    // launch time dialog and set data to time if chosen
                    showTimePickerDialog();
                    break;
                case "Duration":
                    showEditDialog(title, position, "mins");
                    break;
                case "Distance":

                    showEditDialog(title, position,
                            mDistanceUnitPrefs.equals(IMPERIAL_MILES) ? "miles" : "kms");
                    break;
                case "Calorie":
                    showEditDialog(title, position, "cals");
                    break;
                case "Heartbeat":
                    showEditDialog(title, position, "bpm");
                    break;
                case "Comment":
                    showEditDialog(title, position, "");
                    break;
            }
        }
    }

    /*
     * Displays a dialog when an ListView item is clicked and gives
     * user option to edit the particular item
     */
    private void showEditDialog(final String title, final int position, final String units) {
        final AlertDialog.Builder EditDialog = new AlertDialog.Builder(this);

        // Create input Layout for Dialog and InputType
        View customLayout = getLayoutInflater().inflate(R.layout.edit_input_dialog, null);
        EditDialog.setView(customLayout);

        mInputLayout = customLayout.findViewById(R.id.edit_entry_new_data);

        // Set Dialog Title
        EditDialog.setTitle(title);

        // Set Input Type for dialog
        if (title.equalsIgnoreCase("Comment")) {
            mInputLayout.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        } else {
            mInputLayout.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        // Action button OK : grab data and set to data view
        EditDialog.setPositiveButton("OK", (dialog, id) -> {
            String newData = mInputLayout.getText().toString();
            if (!newData.equalsIgnoreCase("")) {
                mItems.get(position).setData(newData + " " + units);
                mManualEntryAdapter.notifyDataSetChanged();
            }
            Log.d("data", newData + " " + units);
        });
        //Action button CANCEL : use default
        EditDialog.setNegativeButton("CANCEL", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = EditDialog.create();
        dialog.show();
    }

    /*
     * Creates Time PickerDialog for user selection
     * Wires up onTimeSet Listener which sets the data TextView to selected Time
     */
    private void showTimePickerDialog() {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = (view, hourOfDay, minute) -> {
            String time;
            if (minute < 10) {
                time = hourOfDay + ":0" + minute;
                mDataView.setText(time);
            } else {
                time = hourOfDay + ":" + minute;
                mDataView.setText(time);
            }

        };

        new TimePickerDialog(ManualEntryActivity.this, onTimeSetListener,
                mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), true).show();
    }

    /*
     * Creates Date PickerDialog for user selection
     * Wires up onDateSet Listener which sets the data TextView to selected Date
     */
    private void showDatePickerDialog() {


        DatePickerDialog.OnDateSetListener onDateSetListener = (view, year, month, dayOfMonth) -> {
            month += 1; // January is 0 in this format
            String date = year + "-" + month + "-" + dayOfMonth;
            mDataView.setText(date);
        };

        new DatePickerDialog(ManualEntryActivity.this, onDateSetListener, mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    /*
     * Initialises each array item as a model with title and data
     * Note we grab the current date, and time for their specific models
     * Data in activity model is set to what was sent from the startFragment
     *
     * Uses default values on null ExerciseEntry
     */
    private void fillUpAdapterArray(ExerciseEntry entry) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(ManualEntryActivity.this);
        mDistanceUnitPrefs = sharedPreferences.getString("unit_preference", "");

        if (entry == null) {
            mItems.add(new ManualEntryModel("Activity",
                    getIntent().getStringExtra(MyConstants.ACTIVITY_TYPE)));
            mItems.add(new ManualEntryModel("Date",
                    java.time.LocalDate.now().toString()));
            mItems.add(new ManualEntryModel("Time",
                    LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))));
            mItems.add(new ManualEntryModel("Duration",
                    "0 mins"));
            mItems.add(new ManualEntryModel("Distance",
                    mDistanceUnitPrefs.equals(IMPERIAL_MILES) ? "0 miles" : "0 kms"));
            mItems.add(new ManualEntryModel("Calorie",
                    "0 cals"));
            mItems.add(new ManualEntryModel("Heartbeat", "0 bpm"));
            mItems.add(new ManualEntryModel("Comment", ""));
        } else {
            mItems.add(new ManualEntryModel("Activity", entry.getActivityType()));
            mItems.add(new ManualEntryModel("Date", entry.getDate()));
            mItems.add(new ManualEntryModel("Time", entry.getTime()));
            mItems.add(new ManualEntryModel("Duration", entry.getDuration()));

            String distance = entry.getDistance();
            if (mDistanceUnitPrefs.equals(IMPERIAL_MILES)) {
                if (distance.contains("kms")) {
                    distance = distance.replace(" kms", "");
                    DecimalFormat df = new DecimalFormat("####0.00");

                    distance = df.format(Double.parseDouble(distance) / MILE_CONVERSION_RATE);
                    distance = distance + " miles";
                    entry.setDistance(distance);
                }
            } else {
                if (distance.contains("miles")) {
                    distance = distance.replace(" miles", "");
                    DecimalFormat df = new DecimalFormat("####0.00");

                    distance = df.format(Double.parseDouble(distance) * MILE_CONVERSION_RATE);
                    distance = distance + " kms";
                    entry.setDistance(distance);
                }
            }
            mItems.add(new ManualEntryModel("Distance", distance));
            mItems.add(new ManualEntryModel("Calorie", entry.getCalorie()));
            mItems.add(new ManualEntryModel("Heartbeat", entry.getHeartRate()));
            mItems.add(new ManualEntryModel("Comment", entry.getComment()));
        }
    }


    /*
     * Set up actionBar depending on the source of the intent
     * Sets Save option if source is StartFragment
     * Sets Delete option if source is HistoryFragment
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Intent entryPoint = getIntent();

        if (entryPoint != null) {
            switch (entryPoint.getStringExtra(SOURCE)) {
                case StartFragment.FRAGMENT_NAME:
                    getMenuInflater().inflate(R.menu.save_activity, menu);
                    break;
                case HistoryFragment.FRAGMENT_NAME:
                    getMenuInflater().inflate(R.menu.delete_activity, menu);
                    break;
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    /*
     * Handles the case when back button is clicked
     * Handles the case when save option is selected
     * Handles the case when delete option is selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.d(TAG, "home button");
                startActivity(new Intent(ManualEntryActivity.this, MainActivity.class));
                finish();
                return true;
            case R.id.save_activity_entry:
                // calls the save method on the database helper
                ExerciseEntry newEntry = captureNewEntry();
                ExerciseInsertTask task = new ExerciseInsertTask(this, mDataSource);
                task.execute(newEntry);
                finish();
                startActivity(new Intent(ManualEntryActivity.this, MainActivity.class));
                return true;
            case R.id.delete_activity_entry:
                // calls the delete method on the database helper
                Intent intent = getIntent();
                if (intent != null) {
                    Long id = intent.getLongExtra(EXERCISE_ENTRY_ID, -1);
                    if (id > -1) {
                        mDeleteTask.execute(id);
                        startActivity(new Intent(ManualEntryActivity.this, MainActivity.class));
                    } else {
                        Log.d(TAG, "Invalid Exercise ID - Non-null index");
                        Toast.makeText(this, "Invalid Exercise ID", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "Invalid Exercise ID");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
     * Called when user selects save option for an actiity
     * captures the data entered and bundles it into an ExerciseEntry
     * Object.
     */
    private ExerciseEntry captureNewEntry() {
        ExerciseEntry nExercise = new ExerciseEntry();
        nExercise.setInputType(getIntent().getStringExtra("InputType"));

        for (int position = 0; position < mManualEntryAdapter.getCount(); position++) {
            String title = mManualEntryAdapter.getItem(position).getTitle();
            String data = mManualEntryAdapter.getItem(position).getData();

            switch (title) {
                case "Activity":
                    nExercise.setActivityType(data);
                    break;
                case "Date":
                    nExercise.setDate(data);
                    break;
                case "Time":
                    nExercise.setTime(data);
                    break;
                case "Duration":
                    nExercise.setDuration(data);
                    break;
                case "Distance":
                    nExercise.setDistance(data);
                    break;
                case "Calorie":
                    nExercise.setCalorie(data);
                    break;
                case "Heartbeat":
                    nExercise.setHeartRate(data);
                    break;
                case "Comment":
                    nExercise.setComment(data);
                    break;
            }
        }
        return nExercise;
    }

    /*
     * If Intent source is History Fragment we create an AsyncTaskLoader
     * used for Fetching single a Exercise Entry
     */
    @NonNull
    @Override
    public Loader<ArrayList<ExerciseEntry>> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == MyConstants.FETCH_SINGLE_EXERCISE_ID) {
            Intent intent = getIntent();
            if (intent != null) {
                Long exerciseID = intent.getLongExtra(MyConstants.EXERCISE_ENTRY_ID, -1);
                if (exerciseID > -1) {
                    return new ExerciseListLoader(ManualEntryActivity.this, exerciseID);
                } else {
                    Log.d(TAG, "Invalid Exercise ID - Non-null intent");
                }
            }
            Log.d(TAG, "Null Intent");
        }
        return null;
    }

    /*
     * Called when AsyncTaskLoader for Fetching Single Entry is finished
     * We update the adapter with contents of the fetched Entry
     */
    @Override
    public void onLoadFinished(@NonNull Loader<ArrayList<ExerciseEntry>> loader, ArrayList<ExerciseEntry> data) {
        Log.d(TAG, "onLoadFinished(): Thread ID " + Thread.currentThread().getId());
        if (loader.getId() == MyConstants.FETCH_SINGLE_EXERCISE_ID) {
            if (data.size() > 0) {
                fillUpAdapterArray(data.get(0));
                mManualEntryAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<ArrayList<ExerciseEntry>> loader) {
    }

    /*
     * Called when user hits back button
     */
    @Override
    public void onBackPressed() {
        startActivity(new Intent(ManualEntryActivity.this, MainActivity.class));
        finish();
        super.onBackPressed();
    }
}
