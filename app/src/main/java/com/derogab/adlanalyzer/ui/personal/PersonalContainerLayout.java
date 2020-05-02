package com.derogab.adlanalyzer.ui.personal;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.derogab.adlanalyzer.ui.CustomLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.Nullable;

public class PersonalContainerLayout extends CustomLayout {

    private static final String TAG = "PersonalContainerLayout";

    private JSONArray contents;

    public PersonalContainerLayout(Context context) {
        super(context);
    }

    public PersonalContainerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PersonalContainerLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /**
     * setSource()
     *
     * Set the JSON Array of contents
     * */
    public void setSource(String url) throws JSONException {
        setSource(getTemplate(url));
    }
    public void setSource(JSONObject jsonObject) throws JSONException {
        setSource(jsonObject.getJSONArray("contents"));
    }
    public void setSource(JSONArray jsonArray){
        contents = jsonArray;
    }

    /**
     * generate()
     *
     * Generate the layout contents
     * */
    public void generate() throws JSONException {

        if (contents != null) {

            for(int i = 0 ; i < contents.length() ; i++){

                // Get params
                JSONObject params = contents.getJSONObject(i);

                // Create custom view
                View customView = null;

                // Generate the view
                switch (params.getString("class")){

                    case "text":

                        // Create a TextView
                        customView = new TextView(getContext());

                        // Set the id
                        if (params.has("id"))
                            customView.setId(params.getInt("id"));

                        // Set attributes
                        if (params.has("text"))
                            ((TextView)customView).setText(params.getString("text"));

                        if (params.has("size"))
                            ((TextView)customView).setTextSize(TypedValue.COMPLEX_UNIT_SP, params.getInt("size"));

                        break;

                    case "input-text":

                        // Create a TextInputLayout
                        customView = new TextInputLayout(getContext());

                        // Set attributes
                        ((TextInputLayout)customView).setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
                        ((TextInputLayout)customView).setBoxCornerRadii((float)10.0, (float)10.0, (float)10.0, (float)10.0);

                        // Create TextInputEditText
                        TextInputEditText input_text = new TextInputEditText(getContext());

                        if (params.has("size"))
                            input_text.setTextSize(params.getInt("size"));

                        // Set the id
                        if (params.has("id"))
                            input_text.setId(params.getInt("id"));

                        // Set label
                        if (params.has("label"))
                            input_text.setHint(params.getString("label"));

                        // Add TextInputEditText in TextInputLayout
                        ((TextInputLayout)customView).addView(input_text);

                        break;

                    case "check-group":

                        // Get checkboxes
                        JSONArray checkboxes = params.getJSONArray("checkboxes");

                        // Create a container
                        customView = new LinearLayout(getContext());

                        // Set the id
                        if (params.has("id"))
                            customView.setId(params.getInt("id"));

                        // Change orientation if necessary
                        if (checkboxes.length() > 2)
                            ((LinearLayout)customView).setOrientation(LinearLayout.VERTICAL);

                        // Add label
                        if (params.has("label")) {

                            TextView label = new TextView(getContext());

                            label.setText(params.getString("label"));

                            ((LinearLayout)customView).addView(label);

                        }

                        // Create checkboxes
                        for(int j = 0 ; j < checkboxes.length() ; j++){

                            // Get checkbox info
                            JSONObject checkbox = checkboxes.getJSONObject(j);

                            // Create CheckBox
                            CheckBox c = new CheckBox(getContext());

                            // Set checkbox attributes
                            if(checkbox.has("id"))
                                c.setId(checkbox.getInt("id"));

                            if(checkbox.has("text"))
                                c.setText(checkbox.getString("text"));

                            // Add checkbox to the container
                            ((LinearLayout)customView).addView(c);

                        }
                        break;


                    case "radio-group":

                        // Get radios
                        JSONArray radios = params.getJSONArray("radios");

                        // Create a container
                        customView = new RadioGroup(getContext());

                        // Set the id
                        if (params.has("id"))
                            customView.setId(params.getInt("id"));

                        // Change orientation if necessary
                        if (radios.length() < 3)
                            ((RadioGroup)customView).setOrientation(RadioGroup.HORIZONTAL);

                        // Add label
                        if (params.has("label")) {

                            TextView label = new TextView(getContext());

                            label.setText(params.getString("label"));

                            ((RadioGroup)customView).addView(label);

                        }

                        // Create radios
                        for(int j = 0 ; j < radios.length() ; j++){

                            // Get radio info
                            JSONObject radio = radios.getJSONObject(j);

                            // Create Radio
                            RadioButton r = new RadioButton(getContext());

                            // Set radio attributes
                            if(radio.has("id"))
                                r.setId(radio.getInt("id"));

                            if(radio.has("text"))
                                r.setText(radio.getString("text"));

                            // Add radio to the container
                            ((RadioGroup)customView).addView(r);

                        }
                        break;
                }

                if (customView != null) this.addView(customView);

            }

        }

    }

    public ArrayList getData() throws JSONException {

        ArrayList list = new ArrayList();

        if (contents != null) {

            for(int i = 0 ; i < contents.length() ; i++){

                // Get params
                JSONObject params = contents.getJSONObject(i);

                // Create custom view
                View customView = null;

                // Generate the view
                switch (params.getString("class")){

                    case "input-text":


                        break;

                    case "check-group":


                        break;


                    case "radio-group":


                        break;
                }

            }

        }









        return list;

    }




}
