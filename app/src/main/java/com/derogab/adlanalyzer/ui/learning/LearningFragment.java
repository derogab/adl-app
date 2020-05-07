package com.derogab.adlanalyzer.ui.learning;

import android.os.Bundle;
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

public class LearningFragment extends Fragment {

    public static final String TAG = "LearningFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_learning, container, false);
        MaterialSpinner activity_selector = (MaterialSpinner) root.findViewById(R.id.activity_selector);

        final Button startLearning = root.findViewById(R.id.fragment_learning_start_button);



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

        activity_selector.setItems(act_list);


        Activity first_selected = (Activity) activity_selector.getItems().get(activity_selector.getSelectedIndex());
        init_config(root, first_selected);


        activity_selector.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<Activity>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, Activity item) {

            init_config(root, item);

            }

        });



        return root;
    }

    private void init_config(View v, Activity item){

        final TextView timeValue = v.findViewById(R.id.fragment_learning_config_seconds_value);
        final TextView frequencyValue = v.findViewById(R.id.fragment_learning_config_frequency_value);
        final TextView sensorsValue = v.findViewById(R.id.fragment_learning_config_sensors_value);

        timeValue.setText(item.getSeconds() + "sec");
        frequencyValue.setText(item.getFrequency() + "bitrate");

        String[] sensors = item.getSensors();
        String sensors_str = "";

        if(sensors != null){

            for (int i = 0; i < sensors.length; i++) {
                sensors_str += sensors[i];

                if (i != sensors.length-1) sensors_str += ", ";

            }



            sensorsValue.setText(sensors_str);
        }

    }


}
