package com.derogab.adlanalyzer.ui.personal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.derogab.adlanalyzer.HttpGetRequest;
import com.derogab.adlanalyzer.R;
import com.derogab.adlanalyzer.utils.Constants;
import com.google.android.material.snackbar.Snackbar;
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
    private FragmentActivity mContext;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mContext = this.getActivity();

        View root = inflater.inflate(R.layout.fragment_personal, container, false);

        personalFormContent = root.findViewById(R.id.personalFormContent);

        SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.PERSONAL_DATA_INFORMATION_FILE_NAME, Context.MODE_PRIVATE);
            personalFormContent.setStorage(sharedPreferences);

        try {
            personalFormContent.setSource("https://pastebin.com/raw/xGFK8TvB");
            personalFormContent.generate();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        Button saveButton = root.findViewById(R.id.personalSaveButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    personalFormContent.saveData();
                    Snackbar.make(v, R.string.fragment_personal_saved, Snackbar.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Snackbar.make(v, R.string.fragment_personal_not_saved, Snackbar.LENGTH_SHORT).show();
                }

            }
        });



        return root;

    }

}
