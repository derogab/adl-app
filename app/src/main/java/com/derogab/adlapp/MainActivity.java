package com.derogab.adlapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.derogab.adlapp.utils.Constants;
import com.derogab.adlapp.utils.CurrentLang;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Get current theme preference
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Set correct theme
        if (preferences.getBoolean(Constants.PREFERENCE_DARK_MODE, false))
            setTheme(R.style.AppTheme_Dark);
        else
            setTheme(R.style.AppTheme_Light);

        // Create all
        super.onCreate(savedInstanceState);

        // Set current language
        CurrentLang.getInstance().setLang(getString(R.string.current_lang));

        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_analyzer, R.id.navigation_learning, R.id.navigation_personal)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_right_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()) {

            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

        }


        return super.onOptionsItemSelected(item);
    }
}
