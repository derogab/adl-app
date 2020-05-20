package com.derogab.adlanalyzer.ui.learning;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
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

import com.derogab.adlanalyzer.services.LearningService;
import com.derogab.adlanalyzer.utils.CountDown;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceFragmentCompat;

import android.preference.PreferenceManager;

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

    BroadcastReceiver learningServiceReceiver;

    private TextView accelerometerValue, gyroscopeValue, countdownValue;

    private FragmentActivity mContext;


    private LearningViewModel learningViewModel;

    private MaterialSpinner activitySelector, phonePositionSelector;
    private FloatingActionButton startLearning;

    private View root;

    private Intent learningIntent;




    private String learningArchive;





    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Creating...");

        // Get main context
        mContext = this.getActivity();

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

        // Create service
        learningIntent = new Intent(mContext, LearningService.class);

        // Create ViewModel
        learningViewModel = new ViewModelProvider(this).get(LearningViewModel.class);

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if (root == null) {
            Log.d(TAG, "Creating View...");
            root = inflater.inflate(R.layout.fragment_learning, container, false);
        }
        else {
            Log.d(TAG, "Restoring View...");
        }


        // Layout elements
        activitySelector = (MaterialSpinner) root.findViewById(R.id.activity_selector);
        phonePositionSelector = (MaterialSpinner) root.findViewById(R.id.phone_position_selector);
        startLearning = root.findViewById(R.id.fragment_learning_start_button);

        // Dynamic values
        countdownValue = root.findViewById(R.id.fragment_learning_countdown_timer);
        accelerometerValue = root.findViewById(R.id.fragment_learning_sensors_accelerometer_value);
        gyroscopeValue = root.findViewById(R.id.fragment_learning_sensors_gyroscope_value);

        // Insert phone positions
        phonePositionSelector.setItems(PhonePosition.getAll(mContext));

        // Insert activities
        learningViewModel.getActivities().observe(getViewLifecycleOwner(), new Observer<List<Activity>>() {
            @Override
            public void onChanged(List<Activity> activities) {

                activitySelector.setItems(activities);
                updateInfo();

            }

        });

        // Set listener on Spinner select change
        activitySelector.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<Activity>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, Activity item) {

                updateInfo();

            }

        });

        // Set button start listener
        startLearning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "fab clicked. ");
                alert(view, "Starting in 10 seconds");

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

        // Return view
        return root;
    }

    private Activity getSelectedActivity() {

        // Get the selected activity index
        int index = activitySelector.getSelectedIndex();

        // Get the selected activity
        return (Activity) activitySelector.getItems().get(index);

    }

    private PhonePosition getSelectedPosition() {

        // Get the selected activity index
        int index = phonePositionSelector.getSelectedIndex();

        // Get the selected activity
        return (PhonePosition) phonePositionSelector.getItems().get(index);

    }

    private int getSensorStatusText(boolean isActive) {

        if (isActive) return R.string.fragment_learning_sensor_status_active;
        else return R.string.fragment_learning_sensor_status_not_active;

    }

    private void updateInfo() {
        Log.d(TAG, "Updating info...");

        Activity as = getSelectedActivity();

        if (as != null) {

            // Write time in the countdown
            countdownValue.setText(CountDown.get(as.getTime()));

            // Write sensors status
            accelerometerValue.setText(getSensorStatusText(as.isSensorActive(Constants.SENSOR_ACCELEROMETER)));
            gyroscopeValue.setText(getSensorStatusText(as.isSensorActive(Constants.SENSOR_GYROSCOPE)));

        }

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "Starting...");

        // Set information receiver from service
        learningServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if(intent.getAction().equals("GET_ACTIVITY_COUNTDOWN")) {

                    long activityCountdown = intent.getLongExtra("ACTIVITY_COUNTDOWN",0);
                    Log.d(TAG, "Activity countdown received: " + activityCountdown);

                    if (activityCountdown % 5 == 0) {

                        speak(""+activityCountdown);

                    }

                    countdownValue.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    countdownValue.setText(CountDown.get(activityCountdown));

                }
                else if(intent.getAction().equals("GET_PREPARATION_COUNTDOWN")) {

                    long preparationCountdown = intent.getLongExtra("PREPARATION_COUNTDOWN",0);
                    Log.d(TAG, "Preparation countdown received: " + preparationCountdown);

                    if (preparationCountdown == 3) {

                        speak("The activity is about to begin");

                    }

                    countdownValue.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                    countdownValue.setText(CountDown.get(preparationCountdown));

                }
                else if(intent.getAction().equals("GET_ACTIVITY_START")) {
                    Log.d(TAG, "Activity started");

                    speak("Activity started.");

                    alert(root, "Activity started.");
                }
                else if(intent.getAction().equals("GET_ACTIVITY_END")) {
                    Log.d(TAG, "Activity stop");

                    speak("done!");

                    countdownValue.setText(CountDown.get(getSelectedActivity().getTime()));

                    alert(root, "Done.");
                }


            }
        };


        // Init receiver
        mContext.registerReceiver(learningServiceReceiver, new IntentFilter("GET_ACTIVITY_COUNTDOWN"));
        mContext.registerReceiver(learningServiceReceiver, new IntentFilter("GET_PREPARATION_COUNTDOWN"));
        mContext.registerReceiver(learningServiceReceiver, new IntentFilter("GET_ACTIVITY_START"));
        mContext.registerReceiver(learningServiceReceiver, new IntentFilter("GET_ACTIVITY_END"));



    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Resuming...");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "Pausing...");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "Stopping...");
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
        Snackbar.make(view, text, duration).show();
        return true;
    }


}
