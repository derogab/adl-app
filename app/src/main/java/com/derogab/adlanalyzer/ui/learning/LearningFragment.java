package com.derogab.adlanalyzer.ui.learning;

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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.derogab.adlanalyzer.JsonSource;
import com.derogab.adlanalyzer.R;
import com.google.android.material.snackbar.Snackbar;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class LearningFragment extends Fragment {

    private static final String TAG = "LearningFragment";

    private static final String ACTIVITY_SELECTED_INDEX = "activity_selected_index";


    private TextToSpeech textToSpeech;
    private CountDownTimer countDownTimer;

    private TextView timeValue, frequencyValue, sensorsValue;

    private int activitySelectedIndex;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        // Layout elements
        final View root = inflater.inflate(R.layout.fragment_learning, container, false);
        final MaterialSpinner activity_selector = (MaterialSpinner) root.findViewById(R.id.activity_selector);
        final Button startLearning = root.findViewById(R.id.fragment_learning_start_button);


        // Get activities list
        JSONArray activities;
        ArrayList<Activity> act_list = new ArrayList<>();
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
                JSONArray sensors_json = params.getJSONArray("sensors");
                String sensors_array[] = new String[sensors_json.length()];
                for (int j = 0; j < sensors_json.length(); j++) {
                    sensors_array[j] = sensors_json.getString(j);
                }
                tmp.setSensors(sensors_array);

                // Add activities to  Spinner list
                act_list.add(tmp);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Add activities list to the spinner list
        activity_selector.setItems(act_list);

        // Set all saved data
        if(savedInstanceState != null){
            Log.d(TAG, "Restore InstanceState...");
            activitySelectedIndex = savedInstanceState.getInt(ACTIVITY_SELECTED_INDEX);

            if (activitySelectedIndex < activity_selector.getItems().size()) {
                activity_selector.setSelectedIndex(activitySelectedIndex);
            }

        }

        // Get first activity selected index
        activitySelectedIndex = activity_selector.getSelectedIndex();

        // First selected config
        Activity first_selected = (Activity) activity_selector.getItems().get(activitySelectedIndex);
        initConfig(root, first_selected);

        // Set listener on Spinner select change
        activity_selector.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<Activity>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, Activity item) {

            activitySelectedIndex = position;
            initConfig(root, item);

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
                countDownTimer.start();
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
        frequencyValue = v.findViewById(R.id.fragment_learning_config_frequency_value);
        sensorsValue = v.findViewById(R.id.fragment_learning_config_sensors_value);

        timeValue.setText(item.getSeconds() + " sec");
        frequencyValue.setText(item.getFrequency() + " bit/s");

        String[] sensors = item.getSensors();
        String sensors_str = "";

        if(sensors != null){

            for (int i = 0; i < sensors.length; i++) {
                sensors_str += sensors[i];

                if (i != sensors.length-1) sensors_str += ", ";

            }
            sensorsValue.setText(sensors_str);
        }

        countDownTimer = new CountDownTimer(item.getSeconds() * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(TAG, "seconds remaining: " + millisUntilFinished / 1000);

                timeValue.setText("" + (millisUntilFinished / 1000) + " sec");

                if (millisUntilFinished / 1000 % 5 == 0) {

                    int speechStatus = textToSpeech.speak(""+(millisUntilFinished / 1000), TextToSpeech.QUEUE_FLUSH, null);

                    if (speechStatus == TextToSpeech.ERROR) {
                        Log.e(TAG, "[TTS] Error in converting Text to Speech!");
                    }

                }

            }

            @Override
            public void onFinish() {
                Log.d(TAG, "timer done!");

                int speechStatus = textToSpeech.speak("done!", TextToSpeech.QUEUE_FLUSH, null);

                if (speechStatus == TextToSpeech.ERROR) {
                    Log.e(TAG, "[TTS] Error in converting Text to Speech!");
                }
            }
        };

    }


}
