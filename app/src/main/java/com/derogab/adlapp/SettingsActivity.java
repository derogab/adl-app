package com.derogab.adlapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.derogab.adlapp.utils.Constants;
import com.google.android.material.snackbar.Snackbar;

public class SettingsActivity extends AppCompatActivity {

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

        setContentView(R.layout.activity_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            // Set preference on change listeners
            Preference themePreference = findPreference(Constants.PREFERENCE_DARK_MODE);

            if (themePreference != null) themePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {

                        Snackbar.make(requireView(), R.string.settings_theme_changed_alert, Snackbar.LENGTH_LONG).show();

                        return false;
                    }
                });
        }
    }
}