package com.derogab.adlanalyzer.ui.analyzer;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.derogab.adlanalyzer.R;
import com.derogab.adlanalyzer.databinding.FragmentAnalyzerBinding;
import com.derogab.adlanalyzer.models.Activity;
import com.derogab.adlanalyzer.models.PhonePosition;
import com.derogab.adlanalyzer.services.AnalyzerService;
import com.derogab.adlanalyzer.utils.Constants;
import com.derogab.adlanalyzer.utils.CountDown;
import com.google.android.material.snackbar.Snackbar;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AnalyzerFragment extends Fragment {

    private static final String TAG = "AnalyzerFragment";

    private FragmentActivity mContext;
    private FragmentAnalyzerBinding binding;
    private AnalyzerViewModel analyzerViewModel;
    private Intent analyzerIntent;
    private BroadcastReceiver analyzerServiceReceiver;

    private TextToSpeech textToSpeech;

    private void checkSensor(TextView v, String featureSensor) {
        if (mContext != null){

            if (mContext.getPackageManager().hasSystemFeature(featureSensor)) {
                v.setText(R.string.sensor_status_enabled);
                v.setTextColor(getResources().getColor(R.color.colorEnabled));
            }
            else {
                v.setText(R.string.sensor_status_not_present);
                v.setTextColor(getResources().getColor(R.color.colorDisabled));
            }

        }
    }

    private PhonePosition getSelectedPosition() {

        // Get the selected activity index
        int index = binding.phonePositionSelector.getSelectedIndex();

        // Get the selected activity
        return (PhonePosition) binding.phonePositionSelector.getItems().get(index);

    }

    private boolean speak(String tts) {

        if (textToSpeech == null) return false;

        int speechStatus = textToSpeech.speak(tts, TextToSpeech.QUEUE_FLUSH, null);

        if (speechStatus == TextToSpeech.ERROR) {
            Log.e(TAG, "[TTS] Error in converting Text to Speech!");
            return false;
        }

        return true;
    }

    private boolean alert(View view, String text) {
        return alert(view, text, Snackbar.LENGTH_SHORT);
    }

    private boolean alert(View view, String text, int duration) {
        if (view == null) return false;

        try {
            Snackbar.make(view, text, duration).show();
        }
        catch (Exception e) {
            Log.d(TAG, "[Error] No alert showed.");
            return false;
        }

        return true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get main context
        mContext = this.getActivity();

        // Set TTS object
        textToSpeech = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    Log.d(TAG, "Current Locale: "+getString(R.string.current_lang));

                    int ttsLang = textToSpeech.setLanguage(new Locale(getString(R.string.current_lang)));

                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA
                            || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e(TAG, "[TTS] The Language is not supported!");
                    } else {
                        Log.i(TAG, "[TTS] Language Supported.");
                    }
                    Log.i(TAG, "[TTS] Initialization success.");
                } else {

                    Log.e(TAG, "[TTS] Initialization failed!");
                }
            }
        });

        // Create service
        analyzerIntent = new Intent(mContext, AnalyzerService.class);

        // Create ViewModel
        analyzerViewModel = new ViewModelProvider(requireActivity()).get(AnalyzerViewModel.class);

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // View Binding
        binding = FragmentAnalyzerBinding.inflate(getLayoutInflater());
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Insert phone positions
        analyzerViewModel.getPhonePositions(getActivity()).observe(getViewLifecycleOwner(), new Observer<List<PhonePosition>>() {
            @Override
            public void onChanged(List<PhonePosition> phonePositions) {

                // Add all phone position to the selector
                binding.phonePositionSelector.setItems(phonePositions);

                // Select the previously selected phone position
                if (binding.phonePositionSelector.getItems().size() > analyzerViewModel.getPhonePositionSelectedIndex())
                    binding.phonePositionSelector.setSelectedIndex(analyzerViewModel.getPhonePositionSelectedIndex());

                // Display the correct fab
                if(!analyzerViewModel.isAnalyzingInProgress())
                    binding.fragmentAnalyzerStartButton.show();
                else
                    binding.fragmentAnalyzerCancelButton.show();

            }
        });

        // Set listener on PhonePosition Spinner select change
        binding.phonePositionSelector.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<PhonePosition>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, PhonePosition item) {


                analyzerViewModel.setPhonePositionSelectedIndex(position);

            }

        });

        // Set start click listener
        binding.fragmentAnalyzerStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Starting...");

                // Update progress status
                analyzerViewModel.setAnalyzingInProgress(true);

                // Hide default button and show cancel
                binding.fragmentAnalyzerStartButton.hide();
                binding.fragmentAnalyzerCancelButton.show();

                // Get SharedPreferences file for settings preferences
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);

                // Set current information
                analyzerIntent.putExtra(Constants.LEARNING_SERVICE_ARCHIVE, UUID.randomUUID().toString());
                analyzerIntent.putExtra(Constants.LEARNING_SERVICE_PHONE_POSITION, getSelectedPosition().getPosition());
                // Set settings info
                analyzerIntent.putExtra(Constants.LEARNING_SERVICE_PREPARATION_TIMER,
                        Integer.parseInt(preferences.getString(Constants.PREFERENCE_PREPARATION_TIME,
                                ""+Constants.LEARNING_COUNTDOWN_PREPARATION_SECONDS_DEFAULT)));
                // Set server information
                analyzerIntent.putExtra(Constants.PREFERENCE_SERVER_DESTINATION,
                        preferences.getString(Constants.PREFERENCE_SERVER_DESTINATION, "localhost"));
                analyzerIntent.putExtra(Constants.PREFERENCE_SERVER_PORT,
                        Integer.parseInt(preferences.getString(Constants.PREFERENCE_SERVER_PORT, "8080")));

                // Start the service
                mContext.startService(analyzerIntent);

            }
        });

        // Set cancel click listener
        binding.fragmentAnalyzerCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Cancelling...");

                // Stop the service
                mContext.stopService(analyzerIntent);

                // Update progress status
                analyzerViewModel.setAnalyzingInProgress(false);

                // Hide cancel button and show default
                binding.fragmentAnalyzerCancelButton.hide();
                binding.fragmentAnalyzerStartButton.show();

            }
        });

        // Set information receiver from service
        analyzerServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            if (intent.getAction() != null) switch (intent.getAction()) {

                case "GET_SERVICE_START":
                    long preparationTime =
                            intent.getLongExtra("PREPARATION_TIME",
                                    Constants.LEARNING_COUNTDOWN_PREPARATION_SECONDS_DEFAULT);

                    alert(getView(), "Starting in " + preparationTime +" seconds");

                    break;

                case "GET_CONNECTION_ERROR":
                    String errorMessage = intent.getStringExtra("CONNECTION_ERROR");

                    if (errorMessage != null)
                        alert(getView(), errorMessage);

                    binding.fragmentAnalyzerCancelButton.hide();
                    binding.fragmentAnalyzerStartButton.show();

                    break;

            }

            }
        };

        // Init receiver
        mContext.registerReceiver(analyzerServiceReceiver, new IntentFilter("GET_SERVICE_START"));
        mContext.registerReceiver(analyzerServiceReceiver, new IntentFilter("GET_CONNECTION_ERROR"));

        // Set sensors status on UI
        checkSensor(binding.fragmentAnalyzerSensorsAccelerometerValue, PackageManager.FEATURE_SENSOR_ACCELEROMETER);
        checkSensor(binding.fragmentAnalyzerSensorsGyroscopeValue, PackageManager.FEATURE_SENSOR_GYROSCOPE);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Destroy the broadcast receiver
        mContext.unregisterReceiver(analyzerServiceReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Destroy TTS
        textToSpeech.stop();
        textToSpeech.shutdown();
        textToSpeech = null;
    }
}
