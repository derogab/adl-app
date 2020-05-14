package com.derogab.adlanalyzer.ui.learning;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.derogab.adlanalyzer.utils.CountDown;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.derogab.adlanalyzer.JsonSource;
import com.derogab.adlanalyzer.MainActivity;
import com.derogab.adlanalyzer.R;

import com.derogab.adlanalyzer.models.Activity;
import com.derogab.adlanalyzer.utils.Constants;
import com.derogab.adlanalyzer.utils.PhonePosition;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class LearningFragment extends Fragment {

    private static final String TAG = "LearningFragment";

    private static final String ACTIVITY_SELECTED_INDEX = "activity_selected_index";

    private TextToSpeech textToSpeech;
    private CountDownTimer preparationTimer, activityTimer;


    private TextView accelerometerValue, gyroscopeValue, countdownValue;

    private int activitySelectedIndex = 0;

    private SensorManager sensorManager;

    private FragmentActivity mContext;


    private LearningViewModel learningViewModel;

    private MaterialSpinner activitySelector, phonePositionSelector;
    private FloatingActionButton startLearning;

    private View root;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mContext = this.getActivity();

        // Layout elements
        root = inflater.inflate(R.layout.fragment_learning, container, false);
        activitySelector = (MaterialSpinner) root.findViewById(R.id.activity_selector);
        phonePositionSelector = (MaterialSpinner) root.findViewById(R.id.phone_position_selector);
        startLearning = root.findViewById(R.id.fragment_learning_start_button);

        // Dynamic values
        countdownValue = root.findViewById(R.id.fragment_learning_countdown_timer);
        accelerometerValue = root.findViewById(R.id.fragment_learning_sensors_accelerometer_value);
        gyroscopeValue = root.findViewById(R.id.fragment_learning_sensors_gyroscope_value);

        // SensorManager init
        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        // Phone position init
        phonePositionSelector.setItems(PhonePosition.getAll(mContext));
        phonePositionSelector.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<PhonePosition>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, PhonePosition item) {

                ((MainActivity) mContext).setPhonePosition(item);

            }
        });

        learningViewModel = new ViewModelProvider(this).get(LearningViewModel.class);

        learningViewModel.getActivities().observe(getViewLifecycleOwner(), new Observer<List<Activity>>() {
            @Override
            public void onChanged(List<Activity> activities) {

                activitySelector.setItems(activities);
                updateInfo();

            }

        });


        // Set all saved data
        if(savedInstanceState != null){
            Log.d(TAG, "Restore InstanceState...");
            activitySelectedIndex = savedInstanceState.getInt(ACTIVITY_SELECTED_INDEX);


        }




        // Set listener on Spinner select change
        activitySelector.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<Activity>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, Activity item) {

                activitySelectedIndex = position;

                ((MainActivity) mContext).setActivityToAnalyze(item);

                updateInfo();

            }

        });

        // Set TTS object
        textToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
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


        // Set listener
        startLearning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "fab clicked. ");
                Snackbar.make(view, "Starting in 10 seconds", Snackbar.LENGTH_SHORT).show();
                preparationTimer.start();
            }
        });

        return root;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.d(TAG, "Saving InstanceState...");
        outState.putInt(ACTIVITY_SELECTED_INDEX, activitySelectedIndex);
    }

    private void infoSensor(TextView sensorValue, boolean isActive) {

        if (isActive) {
            sensorValue.setText("ON");
        }
        else {
            sensorValue.setText("OFF");
        }

    }

    private void updateInfo() {

        Log.d(TAG, "Updating info...");

        // Select previously
        if (activitySelectedIndex != 0
                && activitySelectedIndex < activitySelector.getItems().size()) {

            activitySelector.setSelectedIndex(activitySelectedIndex);

        }

        // Get the selected activity index
        activitySelectedIndex = activitySelector.getSelectedIndex();

        // Get the selected activity
        Activity activitySelected = (Activity) activitySelector.getItems().get(activitySelectedIndex);

        // Set header in the MainActivity
        ((MainActivity) mContext).setActivityToAnalyze(activitySelected);

        // Write time in the countdown
        countdownValue.setText(CountDown.get(activitySelected.getTime()));

        // Write sensors status
        infoSensor(accelerometerValue, activitySelected.isSensorActive(Constants.SENSOR_ACCELEROMETER));
        infoSensor(gyroscopeValue, activitySelected.isSensorActive(Constants.SENSOR_GYROSCOPE));

        // Set preparation timer
        preparationTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(TAG, "seconds remaining before start: " + millisUntilFinished / 1000);

                if ((millisUntilFinished / 1000) == 3) {

                    int speechStatus = textToSpeech.speak("The activity is about to begin", TextToSpeech.QUEUE_FLUSH, null);

                    if (speechStatus == TextToSpeech.ERROR) {
                        Log.e(TAG, "[TTS] Error in converting Text to Speech!");
                    }

                }

                countdownValue.setText("-" + CountDown.get(millisUntilFinished / 1000));

            }

            @Override
            public void onFinish() {
                Log.d(TAG, "preparation timer done!");

                sensorManager.registerListener((SensorEventListener) mContext, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener((SensorEventListener) mContext, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);

                ((MainActivity) mContext).setSendingArchive(UUID.randomUUID().toString());
                ((MainActivity) mContext).setSendingMode(Constants.SENDING_MODE_LEARN);

                int speechStatus = textToSpeech.speak("Activity started.", TextToSpeech.QUEUE_FLUSH, null);

                if (speechStatus == TextToSpeech.ERROR) {
                    Log.e(TAG, "[TTS] Error in converting Text to Speech!");
                }

                Snackbar.make(root, "Activity started.", Snackbar.LENGTH_SHORT).show();

                activityTimer.start();
            }
        };

        // Set activity timer
        activityTimer = new CountDownTimer(activitySelected.getTime() * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(TAG, "seconds remaining: " + millisUntilFinished / 1000);

                if (millisUntilFinished / 1000 % 5 == 0) {

                    int speechStatus = textToSpeech.speak(""+(millisUntilFinished / 1000), TextToSpeech.QUEUE_FLUSH, null);

                    if (speechStatus == TextToSpeech.ERROR) {
                        Log.e(TAG, "[TTS] Error in converting Text to Speech!");
                    }

                }

                countdownValue.setText(CountDown.get((millisUntilFinished / 1000)));

            }

            @Override
            public void onFinish() {
                Log.d(TAG, "activity timer done!");

                // Stop Sensors listener
                sensorManager.unregisterListener((SensorEventListener) mContext);

                int speechStatus = textToSpeech.speak("done!", TextToSpeech.QUEUE_FLUSH, null);

                if (speechStatus == TextToSpeech.ERROR) {
                    Log.e(TAG, "[TTS] Error in converting Text to Speech!");
                }
            }
        };



    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        /*textToSpeech.stop();
        textToSpeech.shutdown();
        textToSpeech = null;*/
    }
}
