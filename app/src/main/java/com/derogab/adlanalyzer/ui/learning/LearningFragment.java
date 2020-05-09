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
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.derogab.adlanalyzer.JsonSource;
import com.derogab.adlanalyzer.MainActivity;
import com.derogab.adlanalyzer.R;

import com.derogab.adlanalyzer.utils.Activity;
import com.derogab.adlanalyzer.utils.Constants;
import com.derogab.adlanalyzer.utils.PhonePosition;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class LearningFragment extends Fragment {

    private static final String TAG = "LearningFragment";

    private static final String ACTIVITY_SELECTED_INDEX = "activity_selected_index";

    private TextToSpeech textToSpeech;
    private CountDownTimer preparationTimer, activityTimer;

    private TextView timeValue, sensorsValue;

    private int activitySelectedIndex;

    private SensorManager sensorManager;

    private FragmentActivity mContext;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mContext = this.getActivity();

        // Layout elements
        final View root = inflater.inflate(R.layout.fragment_learning, container, false);
        final MaterialSpinner activitySelector = (MaterialSpinner) root.findViewById(R.id.activity_selector);
        final MaterialSpinner phonePositionSelector = (MaterialSpinner) root.findViewById(R.id.phone_position_selector);
        final Button startLearning = root.findViewById(R.id.fragment_learning_start_button);

        // SensorManager init
        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        // Phone position init
        phonePositionSelector.setItems(PhonePosition.getAll(mContext));
        phonePositionSelector.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<PhonePosition>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, PhonePosition item) {

                ((MainActivity) mContext).setPhonePosition(item);

            }
        });

        // Get activities list
        JSONArray activities;
        ArrayList<Activity> activitiesList = new ArrayList<>();
        try {

            activities = new JsonSource("https://pastebin.com/raw/bs6RK1Wv")
                                    .getJSON()
                                    .getJSONArray("activities");

            for(int i = 0 ; i < activities.length(); i++) {

                // Get params
                JSONObject params = activities.getJSONObject(i);

                // Create activity
                Activity tmp = new Activity(params.getString("name"),
                                            params.getInt("time"));

                // Convert data sensors format
                JSONArray sensorsJson = params.getJSONArray("sensors");
                String sensorsArray[] = new String[sensorsJson.length()];
                for (int j = 0; j < sensorsJson.length(); j++) {
                    sensorsArray[j] = sensorsJson.getString(j);
                }
                tmp.setSensors(sensorsArray);

                // Add activities to  Spinner list
                activitiesList.add(tmp);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Add activities list to the spinner list
        activitySelector.setItems(activitiesList);

        // Set all saved data
        if(savedInstanceState != null){
            Log.d(TAG, "Restore InstanceState...");
            activitySelectedIndex = savedInstanceState.getInt(ACTIVITY_SELECTED_INDEX);

            if (activitySelectedIndex < activitySelector.getItems().size()) {
                activitySelector.setSelectedIndex(activitySelectedIndex);
            }

        }

        // Get first activity selected index
        activitySelectedIndex = activitySelector.getSelectedIndex();

        // First selected config
        Activity first_selected = (Activity) activitySelector.getItems().get(activitySelectedIndex);
        initConfig(root, first_selected);
        ((MainActivity) mContext).setActivityToAnalyze(first_selected);

        // Set listener on Spinner select change
        activitySelector.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<Activity>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, Activity item) {

            activitySelectedIndex = position;
            initConfig(root, item);
            ((MainActivity) mContext).setActivityToAnalyze(item);

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
            public void onClick(View v) {
            Log.d(TAG, "button clicked. ");
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

    @Override
    public void onDestroy() {
        super.onDestroy();

        textToSpeech.stop();
        textToSpeech.shutdown();
        textToSpeech = null;
    }

    private void initConfig(View v, Activity item){

        timeValue = v.findViewById(R.id.fragment_learning_config_seconds_value);
        sensorsValue = v.findViewById(R.id.fragment_learning_config_sensors_value);

        timeValue.setText(item.getSeconds() + " sec");


        String[] sensors = item.getSensors();
        String sensorsStr = "";

        if(sensors != null){

            for (int i = 0; i < sensors.length; i++) {
                sensorsStr += sensors[i];

                if (i != sensors.length-1) sensorsStr += ", ";

            }
            sensorsValue.setText(sensorsStr);
        }

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

                timeValue.setText("-" + (millisUntilFinished / 1000) + " sec");

            }

            @Override
            public void onFinish() {
                Log.d(TAG, "preparation timer done!");

                sensorManager.registerListener((SensorEventListener) mContext, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener((SensorEventListener) mContext, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);

                ((MainActivity) mContext).setSendingArchive(UUID.randomUUID().toString());
                ((MainActivity) mContext).setSendingMode(Constants.SENDING_MODE_LEARN);

                activityTimer.start();
            }
        };

        activityTimer = new CountDownTimer(item.getSeconds() * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(TAG, "seconds remaining: " + millisUntilFinished / 1000);

                if (millisUntilFinished / 1000 % 5 == 0) {

                    int speechStatus = textToSpeech.speak(""+(millisUntilFinished / 1000), TextToSpeech.QUEUE_FLUSH, null);

                    if (speechStatus == TextToSpeech.ERROR) {
                        Log.e(TAG, "[TTS] Error in converting Text to Speech!");
                    }

                }

                timeValue.setText("" + (millisUntilFinished / 1000) + " sec");

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


}
