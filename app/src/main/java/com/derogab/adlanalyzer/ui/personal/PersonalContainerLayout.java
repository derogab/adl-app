package com.derogab.adlanalyzer.ui.personal;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.derogab.adlanalyzer.JsonSource;
import com.derogab.adlanalyzer.utils.Constants;

import com.derogab.adlanalyzer.utils.MyR;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.Nullable;

public class PersonalContainerLayout extends LinearLayout {

    private static final String TAG = "PersonalContainerLayout";

    private JSONArray contents;
    private SharedPreferences storage;

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
     * getTemplate()
     *
     * Get JSON of contents
     * */
    public static JSONObject getTemplate(String url) { return new JsonSource(url).getJSON(); }

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
     * setStorage()
     *
     * Set the SharedPreferences file
     * */
    public void setStorage(SharedPreferences storage) {
        this.storage = storage;
    }

    /**
     * setElementId()
     *
     * Set an Element ID
     * and save to MyR
     * */
    public void setElementId(View v, int x, String id) {

        // Save correspondence
        MyR.set(x, id);
        // Set ID
        v.setId(x);

    }

    /**
     * generate()
     *
     * Generate the layout contents
     * */
    public void generate() throws JSONException {

        if (contents != null) {

            int viewId = 1;

            for(int i = 0 ; i < contents.length() ; i++) {

                // Get params
                JSONObject params = contents.getJSONObject(i);

                // Create custom view
                View customView = null;

                // Generate the view
                switch (params.getString("class")){

                    case Constants.FORM_ELEMENT_TYPE_TEXT_VIEW:

                        // Create a TextView
                        customView = new TextView(getContext());

                        // Set the id
                        customView.setId(viewId++);

                        // Set attributes
                        if (params.has("text"))
                            ((TextView)customView).setText(params.getString("text"));

                        if (params.has("size"))
                            ((TextView)customView).setTextSize(TypedValue.COMPLEX_UNIT_SP, params.getInt("size"));

                        break;

                    case Constants.FORM_ELEMENT_TYPE_INPUT_TEXT:

                        // Create a TextInputLayout
                        customView = new TextInputLayout(getContext());

                        // Set attributes
                        ((TextInputLayout)customView).setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);

                        // Create TextInputEditText
                        TextInputEditText input_text = new TextInputEditText(getContext());

                        // Restore value
                        if (storage != null)
                            input_text.setText(storage.getString(params.getString("id"), ""));

                        if (params.has("size"))
                            input_text.setTextSize(params.getInt("size"));

                        // Set the id
                        setElementId(input_text, viewId++, params.getString("id"));

                        // Set label
                        if (params.has("label"))
                            input_text.setHint(params.getString("label"));

                        // Add TextInputEditText in TextInputLayout
                        ((TextInputLayout)customView).addView(input_text);

                        break;

                    case Constants.FORM_ELEMENT_TYPE_CHECK_GROUP:

                        // Get checkboxes
                        JSONArray checkboxes = params.getJSONArray("checkboxes");

                        // Create a container
                        customView = new LinearLayout(getContext());

                        // Set the id
                        setElementId(customView, viewId++, params.getString("id"));

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

                            // Restore value
                            if (storage != null) {
                                Set<String> checkboxSet = storage.getStringSet(params.getString("id"), null);

                                if (checkboxSet != null && checkboxSet.contains(checkbox.getString("id"))) {

                                    c.setChecked(true);

                                }

                            }

                            // Set checkbox attributes
                            setElementId(c, viewId++, checkbox.getString("id"));

                            if(checkbox.has("text"))
                                c.setText(checkbox.getString("text"));

                            // Add checkbox to the container
                            ((LinearLayout)customView).addView(c);

                        }
                        break;


                    case Constants.FORM_ELEMENT_TYPE_RADIO_GROUP:

                        // Get radios
                        JSONArray radios = params.getJSONArray("radios");

                        // Create a container
                        customView = new RadioGroup(getContext());

                        // Set the id
                        setElementId(customView, viewId++, params.getString("id"));

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

                            // Restore value
                            if (storage != null) {
                                String radioSelectedId = storage.getString(params.getString("id"), null);

                                if (radioSelectedId != null && radio.getString("id").equals(radioSelectedId) ) {

                                    r.setChecked(true);

                                }

                            }

                            // Set radio attributes
                            setElementId(r, viewId++, radio.getString("id"));

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

                    case Constants.FORM_ELEMENT_TYPE_INPUT_TEXT:


                        break;

                    case Constants.FORM_ELEMENT_TYPE_CHECK_GROUP:




                        break;


                    case Constants.FORM_ELEMENT_TYPE_RADIO_GROUP:


                        break;
                }

            }

        }



        return list;

    }


    public void saveData() throws JSONException {

        Log.d(TAG, "Saving Personal information...");

        if (contents != null) {

            SharedPreferences.Editor editor = storage.edit();

            for(int i = 0 ; i < contents.length() ; i++){

                // Get params
                JSONObject params = contents.getJSONObject(i);

                // Save only data with an ID
                if (params.has("id")) {

                    switch (params.getString("class")){

                        case Constants.FORM_ELEMENT_TYPE_INPUT_TEXT:

                            editor.putString(params.getString("id"),
                                    ((TextView)findViewById(MyR.get(params.getString("id")))).getText().toString() );

                            break;

                        case Constants.FORM_ELEMENT_TYPE_CHECK_GROUP:

                            // Get checkboxes
                            JSONArray checkboxes = params.getJSONArray("checkboxes");

                            // Create Set
                            Set<String> checkGroup = new HashSet<>();

                            // Save every checkboxes
                            for(int j = 0 ; j < checkboxes.length() ; j++) {

                                // Get checkbox info
                                JSONObject checkboxObject = checkboxes.getJSONObject(j);

                                // Get CheckBox by id
                                CheckBox checkBox = ((CheckBox) findViewById(MyR.get(checkboxObject.getString("id"))));

                                // Save checked
                                if(checkBox.isChecked()) {
                                    checkGroup.add(checkboxObject.getString("id"));
                                }

                            }

                            // Save all checked checkbox
                            editor.putStringSet(params.getString("id"), checkGroup);

                            break;


                        case Constants.FORM_ELEMENT_TYPE_RADIO_GROUP:

                            // Get radios
                            JSONArray radios = params.getJSONArray("radios");

                            // Get radiogroup by id
                            RadioGroup radioGroupView = ((RadioGroup) findViewById(MyR.get(params.getString("id"))));

                            // Get checked radio button id
                            int checkedRadioButton = radioGroupView.getCheckedRadioButtonId();

                            // Create radios
                            for(int j = 0 ; j < radios.length() ; j++){

                                // Get radio info
                                JSONObject radioInfo = radios.getJSONObject(j);

                                // Insert id if checked
                                if (MyR.get(radioInfo.getString("id")) == checkedRadioButton){
                                    // Italy
                                    editor.putString(params.getString("id"), radioInfo.getString("id"));
                                }

                            }

                            break;
                    }

                }

            }

            editor.apply();

        }

    }




}
