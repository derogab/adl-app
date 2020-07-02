package com.derogab.adlanalyzer.ui.learning;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.derogab.adlanalyzer.databinding.FragmentLearningBinding;
import com.derogab.adlanalyzer.services.LearningService;
import com.derogab.adlanalyzer.utils.CountDown;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.preference.PreferenceManager;
import android.widget.TextView;

import com.derogab.adlanalyzer.R;

import com.derogab.adlanalyzer.models.Activity;
import com.derogab.adlanalyzer.utils.Constants;
import com.derogab.adlanalyzer.models.PhonePosition;
import com.google.android.material.snackbar.Snackbar;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.List;
import java.util.UUID;


public class LearningFragment extends Fragment {

    private static final String TAG = "LearningFragment";

    private FragmentActivity mContext;
    private FragmentLearningBinding binding;
    private LearningViewModel learningViewModel;
    private BroadcastReceiver learningServiceReceiver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get main context
        mContext = this.getActivity();

        // Create ViewModel
        learningViewModel = new ViewModelProvider(requireActivity()).get(LearningViewModel.class);

        // Check if service is running
        if (isMyServiceRunning(LearningService.class))
            learningViewModel.setLearningInProgress(true);

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // View Binding
        binding = FragmentLearningBinding.inflate(getLayoutInflater());
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Insert phone positions
        learningViewModel.getPhonePositions(getActivity()).observe(getViewLifecycleOwner(), new Observer<List<PhonePosition>>() {
            @Override
            public void onChanged(List<PhonePosition> phonePositions) {

                binding.phonePositionSelector.setItems(phonePositions);

                if (binding.phonePositionSelector.getItems().size() > learningViewModel.getPhonePositionSelectedIndex())
                    binding.phonePositionSelector.setSelectedIndex(learningViewModel.getPhonePositionSelectedIndex());

            }
        });

        // Insert activities
        learningViewModel.getActivities().observe(getViewLifecycleOwner(), new Observer<List<Activity>>() {
            @Override
            public void onChanged(List<Activity> activities) {

                binding.activitySelector.setItems(activities);

                if (binding.activitySelector.getItems().size() > learningViewModel.getActivitySelectedIndex())
                    binding.activitySelector.setSelectedIndex(learningViewModel.getActivitySelectedIndex());

                updateInfo();

                if(!learningViewModel.isLearningInProgress())
                    binding.fragmentLearningStartButton.show();
                else
                    binding.fragmentLearningCancelButton.show();

            }

        });

        // Set listener on Activity Spinner select change
        binding.activitySelector.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<Activity>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, Activity item) {

                learningViewModel.setActivitySelectedIndex(position);
                updateInfo();

            }

        });

        // Set listener on PhonePosition Spinner select change
        binding.phonePositionSelector.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<PhonePosition>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, PhonePosition item) {

                learningViewModel.setPhonePositionSelectedIndex(position);

            }

        });

        // Set button start listener
        binding.fragmentLearningStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "fab clicked. ");

                binding.fragmentLearningStartButton.hide();

                learningViewModel.setLearningInProgress(true);

                // Get SharedPreferences file for settings preferences
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);

                // Get service
                Intent learningIntent = learningViewModel.getService(getContext());

                // Activity info
                learningIntent.putExtra(Constants.LEARNING_SERVICE_ARCHIVE, UUID.randomUUID().toString());
                learningIntent.putExtra(Constants.LEARNING_SERVICE_ACTIVITY, getSelectedActivity().getActivity());
                learningIntent.putExtra(Constants.LEARNING_SERVICE_PHONE_POSITION, getSelectedPosition().getPosition());
                learningIntent.putExtra(Constants.LEARNING_SERVICE_ACTIVITY_TIMER, getSelectedActivity().getTime());
                // Settings info
                learningIntent.putExtra(Constants.LEARNING_SERVICE_PREPARATION_TIMER,
                        Integer.parseInt(preferences.getString(Constants.PREFERENCE_PREPARATION_TIME,
                                ""+Constants.LEARNING_COUNTDOWN_PREPARATION_SECONDS_DEFAULT)));
                // Destination server info
                learningIntent.putExtra(Constants.PREFERENCE_SERVER_DESTINATION,
                        preferences.getString(Constants.PREFERENCE_SERVER_DESTINATION,
                                Constants.SERVER_HOST_DEFAULT));
                learningIntent.putExtra(Constants.PREFERENCE_SERVER_PORT,
                        Integer.parseInt(preferences.getString(Constants.PREFERENCE_SERVER_PORT,
                                ""+Constants.SERVER_HOST_PORT)));
                // Sensors status
                learningIntent.putExtra(Constants.LEARNING_SERVICE_SENSOR_STATUS_ACCELEROMETER,
                        getSelectedActivity().isSensorActive(Constants.SENSOR_ACCELEROMETER));
                learningIntent.putExtra(Constants.LEARNING_SERVICE_SENSOR_STATUS_GYROSCOPE,
                        getSelectedActivity().isSensorActive(Constants.SENSOR_GYROSCOPE));

                // Start the service
                mContext.startService(learningIntent);

            }
        });

        binding.fragmentLearningCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.fragmentLearningCancelButton.hide();

                mContext.stopService(learningViewModel.getService(getContext()));

                // Show alert
                Snackbar.make(requireView(), getString(R.string.alert_stop), Snackbar.LENGTH_SHORT).show();

                binding.fragmentLearningStartButton.show();
            }
        });

        // Set information receiver from service
        learningServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                binding.fragmentLearningStartButton.hide();

                if (intent.getAction() != null) switch (intent.getAction()) {

                    case "LEARNING_SERVICE_START":

                        long preparationTime =
                                intent.getLongExtra("PREPARATION_TIME",
                                        Constants.LEARNING_COUNTDOWN_PREPARATION_SECONDS_DEFAULT);

                        // Show alert
                        Snackbar.make(requireView(), getString(R.string.analyzer_service_preparation_countdown, preparationTime), Snackbar.LENGTH_SHORT).show();

                        binding.fragmentLearningCancelButton.show();

                        break;

                    case "LEARNING_ACTIVITY_START":

                        // Show alert
                        Snackbar.make(requireView(), getString(R.string.alert_start), Snackbar.LENGTH_SHORT).show();

                        break;

                    case "LEARNING_ACTIVITY_COUNTDOWN":

                        long activityCountdown = intent.getLongExtra("ACTIVITY_COUNTDOWN", 0);

                        binding.fragmentLearningCountdownTimer.setText(CountDown.get(activityCountdown));

                        break;

                    case "LEARNING_PREPARATION_COUNTDOWN":

                        long preparationCountdown = intent.getLongExtra("PREPARATION_COUNTDOWN", 0);

                        binding.fragmentLearningCountdownTimer.setText(CountDown.get(preparationCountdown));

                        break;

                    case "LEARNING_ACTIVITY_END":

                        // Show alert
                        Snackbar.make(requireView(), getString(R.string.alert_done), Snackbar.LENGTH_SHORT).show();

                        binding.fragmentLearningCountdownTimer.setText(CountDown.get(getSelectedActivity().getTime()));
                        binding.fragmentLearningCancelButton.hide();
                        binding.fragmentLearningStartButton.show();

                        learningViewModel.setLearningInProgress(false);

                        break;

                    case "LEARNING_CONNECTION_ERROR":

                        String errorMessage = intent.getStringExtra("CONNECTION_ERROR");

                        // Show alert
                        if (errorMessage != null)
                            Snackbar.make(requireView(), errorMessage, Snackbar.LENGTH_SHORT).show();


                        binding.fragmentLearningCountdownTimer.setText(CountDown.get(getSelectedActivity().getTime()));
                        binding.fragmentLearningCancelButton.hide();
                        binding.fragmentLearningStartButton.show();

                        learningViewModel.setLearningInProgress(false);

                        break;
                }


            }
        };

        // Init receiver
        mContext.registerReceiver(learningServiceReceiver, new IntentFilter("LEARNING_SERVICE_START"));
        mContext.registerReceiver(learningServiceReceiver, new IntentFilter("LEARNING_ACTIVITY_COUNTDOWN"));
        mContext.registerReceiver(learningServiceReceiver, new IntentFilter("LEARNING_PREPARATION_COUNTDOWN"));
        mContext.registerReceiver(learningServiceReceiver, new IntentFilter("LEARNING_ACTIVITY_START"));
        mContext.registerReceiver(learningServiceReceiver, new IntentFilter("LEARNING_ACTIVITY_END"));
        mContext.registerReceiver(learningServiceReceiver, new IntentFilter("LEARNING_CONNECTION_ERROR"));

    }

    private Activity getSelectedActivity() {

        // Get the selected activity index
        int index = binding.activitySelector.getSelectedIndex();

        // Get the selected activity
        return (Activity) binding.activitySelector.getItems().get(index);

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

    private PhonePosition getSelectedPosition() {

        // Get the selected activity index
        int index = binding.phonePositionSelector.getSelectedIndex();

        // Get the selected activity
        return (PhonePosition) binding.phonePositionSelector.getItems().get(index);

    }

    private void checkSensor(TextView v, String featureSensor, boolean isEnabled) {
        if (mContext != null){

            if (!mContext.getPackageManager().hasSystemFeature(featureSensor)) {
                v.setText(R.string.sensor_status_not_present);
                v.setTextColor(getResources().getColor(R.color.colorDisabled));
            }
            else if (!isEnabled) {
                v.setText(R.string.sensor_status_disabled);
                v.setTextColor(getResources().getColor(R.color.colorDisabled));
            }
            else {
                v.setText(R.string.sensor_status_enabled);
                v.setTextColor(getResources().getColor(R.color.colorEnabled));
            }

        }
    }

    private void updateInfo() {
        Log.d(TAG, "Updating info...");

        Activity as = getSelectedActivity();

        if (as != null) {

            // Write time in the countdown
            binding.fragmentLearningCountdownTimer.setText(CountDown.get(as.getTime()));

            // Write sensors status
            checkSensor(binding.fragmentLearningSensorsAccelerometerValue,
                    PackageManager.FEATURE_SENSOR_ACCELEROMETER,
                    as.isSensorActive(Constants.SENSOR_ACCELEROMETER));
            checkSensor(binding.fragmentLearningSensorsGyroscopeValue,
                    PackageManager.FEATURE_SENSOR_GYROSCOPE,
                    as.isSensorActive(Constants.SENSOR_GYROSCOPE));

        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "Destroying View...");

        // Destroy the broadcast receiver
        mContext.unregisterReceiver(learningServiceReceiver);
    }

}
