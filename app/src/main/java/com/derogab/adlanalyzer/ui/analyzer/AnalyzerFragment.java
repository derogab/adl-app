package com.derogab.adlanalyzer.ui.analyzer;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.derogab.adlanalyzer.utils.CurrentLang;
import com.google.android.material.snackbar.Snackbar;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.List;
import java.util.UUID;

public class AnalyzerFragment extends Fragment {

    private static final String TAG = "AnalyzerFragment";

    private FragmentActivity mContext;
    private FragmentAnalyzerBinding binding;
    private AnalyzerViewModel analyzerViewModel;
    private BroadcastReceiver analyzerServiceReceiver;

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

    private boolean isMyServiceRunning(Class serviceClass) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true; // Package name matches, our service is running
            }
        }
        return false; // No matching package name found => Our service is not running
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get main context
        mContext = this.getActivity();

        // Create ViewModel
        analyzerViewModel = new ViewModelProvider(requireActivity()).get(AnalyzerViewModel.class);

        // Check if service is running
        if (isMyServiceRunning(AnalyzerService.class))
            analyzerViewModel.setAnalyzingInProgress(true);
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

                // Update progress status
                analyzerViewModel.setAnalyzingInProgress(true);

                // Hide default button and show cancel
                binding.fragmentAnalyzerStartButton.hide();
                binding.fragmentAnalyzerCancelButton.show();

                // Get SharedPreferences file for settings preferences
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);

                // Get service
                Intent analyzerIntent = analyzerViewModel.getService(getContext());

                // Set current information
                analyzerIntent.putExtra(Constants.LEARNING_SERVICE_ARCHIVE, UUID.randomUUID().toString());
                analyzerIntent.putExtra(Constants.LEARNING_SERVICE_PHONE_POSITION, getSelectedPosition().getId());
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

                // Stop the service
                mContext.stopService(analyzerViewModel.getService(getContext()));

                // Update progress status
                analyzerViewModel.setAnalyzingInProgress(false);

                // Hide cancel button and show default
                binding.fragmentAnalyzerCancelButton.hide();
                binding.fragmentAnalyzerStartButton.show();

                // Output re-init
                binding.fragmentAnalyzerOutput.setTextColor(Color.GRAY);
                binding.fragmentAnalyzerOutput.setText("?");

            }
        });

        // Set information receiver from service
        analyzerServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction() != null) switch (intent.getAction()) {

                    case "ANALYZER_PREPARATION_COUNTDOWN":
                        long preparationCountdown =
                                intent.getLongExtra("PREPARATION_COUNTDOWN", -1);

                        if (preparationCountdown != -1) {

                            // Output the countdown
                            binding.fragmentAnalyzerOutput.setTextColor(getResources().getColor(R.color.colorAccent));
                            binding.fragmentAnalyzerOutput.setText(CountDown.get(preparationCountdown));

                        }

                        break;

                    case "ANALYZER_ACTIVITY_START":

                        // Show alert
                        Snackbar.make(requireView(), getString(R.string.analyzer_service_start), Snackbar.LENGTH_SHORT).show();

                        // Output null
                        binding.fragmentAnalyzerOutput.setTextColor(Color.GRAY);
                        binding.fragmentAnalyzerOutput.setText("...");

                        break;

                    case "ANALYZER_SERVICE_START":
                        long preparationTime =
                                intent.getLongExtra("PREPARATION_TIME",
                                        Constants.LEARNING_COUNTDOWN_PREPARATION_SECONDS_DEFAULT);

                        // Show alert
                        Snackbar.make(requireView(), getString(R.string.analyzer_service_preparation_countdown, preparationTime), Snackbar.LENGTH_SHORT).show();

                        // Output the countdown
                        binding.fragmentAnalyzerOutput.setTextColor(getResources().getColor(R.color.colorAccent));
                        binding.fragmentAnalyzerOutput.setText(CountDown.get(preparationTime));

                        break;

                    case "ANALYZER_CONNECTION_ERROR":
                        String errorMessage = intent.getStringExtra("CONNECTION_ERROR");

                        // Show alert
                        if (errorMessage != null)
                            Snackbar.make(requireView(), errorMessage, Snackbar.LENGTH_SHORT).show();


                        // Change button
                        binding.fragmentAnalyzerCancelButton.hide();
                        binding.fragmentAnalyzerStartButton.show();

                        break;

                    case "ANALYZER_PREDICTION":

                        long prediction = intent.getLongExtra("PREDICTION", Constants.NO_INTEGER_DATA);

                        if (prediction != Constants.NO_INTEGER_DATA) {

                            List<Activity> activities = analyzerViewModel.getActivities().getValue();

                            String predictionOutput = null;

                            if (activities != null)
                                for (int i = 0; i < activities.size(); i++)
                                    if (activities.get(i).getId() == prediction)
                                        predictionOutput = activities.get(i).getTranslations().getLang(CurrentLang.getInstance().getLang());

                            if (predictionOutput != null) {

                                // Output prediction
                                binding.fragmentAnalyzerOutput.setTextColor(getResources().getColor(R.color.colorPrimary));
                                binding.fragmentAnalyzerOutput.setText(predictionOutput);

                            }

                        }

                        break;

                }

            }
        };

        // Init receiver
        mContext.registerReceiver(analyzerServiceReceiver, new IntentFilter("ANALYZER_PREPARATION_COUNTDOWN"));
        mContext.registerReceiver(analyzerServiceReceiver, new IntentFilter("ANALYZER_SERVICE_START"));
        mContext.registerReceiver(analyzerServiceReceiver, new IntentFilter("ANALYZER_ACTIVITY_START"));
        mContext.registerReceiver(analyzerServiceReceiver, new IntentFilter("ANALYZER_CONNECTION_ERROR"));
        mContext.registerReceiver(analyzerServiceReceiver, new IntentFilter("ANALYZER_PREDICTION"));

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

}
