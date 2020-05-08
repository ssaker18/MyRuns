package com.example.sunshine.myruns3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.sunshine.myruns3.fragments.HistoryFragment;
import com.example.sunshine.myruns3.fragments.StartFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String ACTIVITY_NAME = "MainActivity";
    private static final String TAG = MainActivity.class.getName();
    private ViewPager mViewPager;
    private BottomNavigationView mBottomNavigationView;
    private MenuItem mStartItem;
    private MenuItem mHistoryItem;
    public String SOURCE = "Source";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = findViewById(R.id.view_pager);
        mBottomNavigationView = findViewById(R.id.bottom_navigation);

        // create fragments
        ArrayList<Fragment> mFragments = new ArrayList<>();
        mFragments.add(new StartFragment()); // pos 0
        mFragments.add(new HistoryFragment()); // pos 1

        // Initialise ViewPageAdapter
        ViewPageAdapter mViewPageAdapter = new ViewPageAdapter(getSupportFragmentManager(), mFragments);

        // Bind ViewPageAdapter to ViewPager
        mViewPager.setAdapter(mViewPageAdapter);


        // start and history buttons in bottom navigation
        mStartItem = mBottomNavigationView.getMenu().getItem(0);
        mHistoryItem = mBottomNavigationView.getMenu().getItem(1);

        // onclick listener for start button
        mStartItem.setOnMenuItemClickListener(item -> {
            // if it's not the current view switch;
            if (mViewPager.getCurrentItem() != 0){
                mViewPager.setCurrentItem(0);
            }
            return false;
        });

        // onclick listener for history button
        mHistoryItem.setOnMenuItemClickListener(item -> {
            // if it's not the current view switch;
            if (mViewPager.getCurrentItem() != 1){
                mViewPager.setCurrentItem(1);
            }
            return false;
        });


        // Handles when the user changes between start and history fragments either by
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0){
                    mBottomNavigationView.setSelectedItemId(mStartItem.getItemId());
                }else if (position == 1){
                    mBottomNavigationView.setSelectedItemId(mHistoryItem.getItemId());
                }
                Log.d(TAG, "fragment - scroll" +  position);
            }

            // Required Methods: No need to implement: Just useful Debug statements
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d(TAG, "fragment - scroll" + position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d(TAG, "fragment - scroll" +  state);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.main_edit_profile:
                // launch register activity and pass intent
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                intent.putExtra(SOURCE, ACTIVITY_NAME);
                startActivity(intent);
                return true;
            case R.id.main_settings:
                startActivity(new Intent(getApplicationContext(), Settings.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "OnPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "OnResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "OnDestroy");
    }
}
