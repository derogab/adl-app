package com.derogab.adlanalyzer.ui.learning;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.derogab.adlanalyzer.databinding.FragmentLearningBinding;
import com.derogab.adlanalyzer.services.LearningService;
import com.derogab.adlanalyzer.utils.CountDown;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.preference.PreferenceManager;

import com.derogab.adlanalyzer.R;

import com.derogab.adlanalyzer.models.Activity;
import com.derogab.adlanalyzer.utils.Constants;
import com.derogab.adlanalyzer.models.PhonePosition;
import com.google.android.material.snackbar.Snackbar;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.List;
import java.util.Locale;
import java.util.UUID;


public class LearningFragment extends Fragment {

    private static final String TAG = "LearningFragment";

    private FragmentLearningBinding binding;
    private LearningViewModel learningViewModel;

    private TextToSpeech textToSpeech;

    private BroadcastReceiver learningServiceReceiver;

    private FragmentActivity mContext;

    private Intent learningIntent;




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
        learningIntent = new Intent(mContext, LearningService.class);

        // Create ViewModel
        learningViewModel = new ViewModelProvider(requireActivity()).get(LearningViewModel.class);

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

                // Activity info
                learningIntent.putExtra(Constants.LEARNING_SERVICE_ARCHIVE, UUID.randomUUID().toString());
                learningIntent.putExtra(Constants.LEARNING_SERVICE_ACTIVITY, getSelectedActivity().getActivity());
                learningIntent.putExtra(Constants.LEARNING_SERVICE_PHONE_POSITION, getSelectedPosition().getPosition());
                learningIntent.putExtra(Constants.LEARNING_SERVICE_ACTIVITY_TIMER, getSelectedActivity().getTime());
                // Destination server info
                learningIntent.putExtra(Constants.PREFERENCE_SERVER_DESTINATION,
                        preferences.getString(Constants.PREFERENCE_SERVER_DESTINATION, "localhost"));
                learningIntent.putExtra(Constants.PREFERENCE_SERVER_PORT,
                        Integer.parseInt(preferences.getString(Constants.PREFERENCE_SERVER_PORT, "8080")));
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

                mContext.stopService(learningIntent);
                alert(v, "Activity stopped.");

                binding.fragmentLearningStartButton.show();
            }
        });

    }

    private Activity getSelectedActivity() {

        // Get the selected activity index
        int index = binding.activitySelector.getSelectedIndex();

        // Get the selected activity
        return (Activity) binding.activitySelector.getItems().get(index);

    }

    private PhonePosition getSelectedPosition() {

        // Get the selected activity index
        int index = binding.phonePositionSelector.getSelectedIndex();

        // Get the selected activity
        return (PhonePosition) binding.phonePositionSelector.getItems().get(index);

    }

    private int getSensorStatusText(boolean isActive) {

        if (isActive) return R.string.sensor_status_enabled;
        else return R.string.sensor_status_disabled;

    }

    private void updateInfo() {
        Log.d(TAG, "Updating info...");

        Activity as = getSelectedActivity();

        if (as != null) {

            // Write time in the countdown
            binding.fragmentLearningCountdownTimer.setText(CountDown.get(as.getTime()));

            // Write sensors status
            binding.fragmentLearningSensorsAccelerometerValue.setText(getSensorStatusText(as.isSensorActive(Constants.SENSOR_ACCELEROMETER)));
            binding.fragmentLearningSensorsGyroscopeValue.setText(getSensorStatusText(as.isSensorActive(Constants.SENSOR_GYROSCOPE)));

        }

    }

    @Override
    public void onStart() {
        super.onStart();

        // Set information receiver from service
        learningServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                binding.fragmentLearningStartButton.hide();

                if (intent.getAction() != null) switch (intent.getAction()) {

                    case "GET_SERVICE_START":

                        alert(getView(), "Starting in 10 seconds");

                        binding.fragmentLearningCancelButton.show();

                        break;

                    case "GET_ACTIVITY_START":

                        speak("Activity started.");
                        alert(getView(), "Activity started.");
                        break;

                    case "GET_ACTIVITY_COUNTDOWN":

                        long activityCountdown = intent.getLongExtra("ACTIVITY_COUNTDOWN", 0);

                        if (activityCountdown % 5 == 0)
                            speak("" + activityCountdown);

                        binding.fragmentLearningCountdownTimer.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                        binding.fragmentLearningCountdownTimer.setText(CountDown.get(activityCountdown));

                        break;

                    case "GET_PREPARATION_COUNTDOWN":

                        long preparationCountdown = intent.getLongExtra("PREPARATION_COUNTDOWN", 0);

                        if (preparationCountdown == 3)
                            speak("The activity is about to begin");

                        binding.fragmentLearningCountdownTimer.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                        binding.fragmentLearningCountdownTimer.setText(CountDown.get(preparationCountdown));

                        break;

                    case "GET_ACTIVITY_END":

                        speak("done!");
                        alert(getView(), "Done.");

                        binding.fragmentLearningCountdownTimer.setText(CountDown.get(getSelectedActivity().getTime()));
                        binding.fragmentLearningCancelButton.hide();
                        binding.fragmentLearningStartButton.show();

                        learningViewModel.setLearningInProgress(false);

                        break;

                    case "GET_CONNECTION_ERROR":

                        String errorMessage = intent.getStringExtra("CONNECTION_ERROR");

                        if (errorMessage != null)
                            alert(getView(), errorMessage);

                        binding.fragmentLearningCountdownTimer.setText(CountDown.get(getSelectedActivity().getTime()));
                        binding.fragmentLearningCancelButton.hide();
                        binding.fragmentLearningStartButton.show();

                        learningViewModel.setLearningInProgress(false);

                        break;
                }


            }
        };


        // Init receiver
        mContext.registerReceiver(learningServiceReceiver, new IntentFilter("GET_SERVICE_START"));
        mContext.registerReceiver(learningServiceReceiver, new IntentFilter("GET_ACTIVITY_COUNTDOWN"));
        mContext.registerReceiver(learningServiceReceiver, new IntentFilter("GET_PREPARATION_COUNTDOWN"));
        mContext.registerReceiver(learningServiceReceiver, new IntentFilter("GET_ACTIVITY_START"));
        mContext.registerReceiver(learningServiceReceiver, new IntentFilter("GET_ACTIVITY_END"));
        mContext.registerReceiver(learningServiceReceiver, new IntentFilter("GET_CONNECTION_ERROR"));

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "Destroying View...");

        // Destroy the broadcast receiver
        mContext.unregisterReceiver(learningServiceReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroying...");

        // Destroy TTS
        textToSpeech.stop();
        textToSpeech.shutdown();
        textToSpeech = null;
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


}
