package com.derogab.adlanalyzer.ui.personal;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.derogab.adlanalyzer.HttpGetRequest;
import com.derogab.adlanalyzer.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class PersonalFragment extends Fragment {

    private static final String TAG = "MainActivity";

    PersonalContainerLayout personalFormContent;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_personal, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        personalFormContent = view.findViewById(R.id.personalFormContent);
        try {
            personalFormContent.setSource("https://pastebin.com/raw/xGFK8TvB");
            personalFormContent.generate();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        try {
            ArrayList l = personalFormContent.getData();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        ArrayList backup = new ArrayList();
        try {
            backup = personalFormContent.getData();
        } catch (JSONException e) {
            e.printStackTrace();
        }



    }
}
