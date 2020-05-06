package com.derogab.adlanalyzer.ui.learning;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_learning, container, false);
        MaterialSpinner spinner = (MaterialSpinner) root.findViewById(R.id.spinner);

        JSONArray activities;
        ArrayList<String> act_list = new ArrayList<>();
        try {

            activities = new JsonSource("https://pastebin.com/raw/bs6RK1Wv")
                                    .getJSON()
                                    .getJSONArray("activities");

            if (activities != null) {

                for(int i = 0 ; i < activities.length() ; i++) {

                    // Get params
                    JSONObject params = activities.getJSONObject(i);

                    // Add activities to  Spinner list
                    act_list.add(params.getString("name"));
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        spinner.setItems(act_list);

        return root;
    }
}
