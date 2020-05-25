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
import com.derogab.adlanalyzer.models.FormElement;
import com.derogab.adlanalyzer.models.FormSubElement;
import com.derogab.adlanalyzer.utils.Constants;

import com.derogab.adlanalyzer.utils.MyR;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.Nullable;

public class PersonalContainerLayout extends LinearLayout {

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
    public void generate(List<FormElement> elements, SharedPreferences sharedPreferences) {

        int viewId = 1;

        for (int i = 0; i < elements.size(); i++) {

            FormElement element = elements.get(i);
            View customView = null;

            switch (element.getType()){

                case Constants.FORM_ELEMENT_TYPE_TEXT_VIEW:

                    // Create a TextView
                    customView = new TextView(getContext());

                    // Set the id
                    customView.setId(viewId++);

                    // Set attributes
                    if (element.getOption("text") != null)
                        ((TextView)customView).setText(element.getOption("text"));

                    if (element.getOption("size") != null)
                        ((TextView)customView)
                                .setTextSize(TypedValue.COMPLEX_UNIT_SP,
                                        Integer.parseInt(element.getOption("size")));

                    break;

                case Constants.FORM_ELEMENT_TYPE_INPUT_TEXT:

                    // Create a TextInputLayout
                    customView = new TextInputLayout(getContext());

                    // Set attributes
                    ((TextInputLayout)customView).setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);

                    // Create TextInputEditText
                    TextInputEditText input_text = new TextInputEditText(getContext());

                    // Restore value
                    if (sharedPreferences != null)
                        input_text.setText(sharedPreferences.getString(element.getId(), ""));

                    if (element.getOption("size") != null)
                        input_text.setTextSize(Integer.parseInt(element.getOption("size")));

                    // Set the id
                    setElementId(input_text, viewId++, element.getId());

                    // Set label
                    if (element.getOption("label") != null)
                        input_text.setHint(element.getOption("label"));

                    // Add TextInputEditText in TextInputLayout
                    ((TextInputLayout)customView).addView(input_text);

                    break;

                case Constants.FORM_ELEMENT_TYPE_CHECK_GROUP:

                    // Get checkboxes
                    List<FormSubElement> checkboxes = element.getContents();

                    // Create a container
                    customView = new LinearLayout(getContext());

                    // Set the id
                    setElementId(customView, viewId++, element.getId());

                    // Change orientation if necessary
                    if (checkboxes.size() > 2)
                        ((LinearLayout)customView).setOrientation(LinearLayout.VERTICAL);

                    // Add label
                    if (element.getOption("label") != null) {

                        TextView label = new TextView(getContext());

                        label.setText(element.getOption("label"));

                        ((LinearLayout)customView).addView(label);

                    }

                    // Create checkboxes
                    for(int j = 0 ; j < checkboxes.size() ; j++){

                        // Get checkbox info
                        FormSubElement checkbox = checkboxes.get(j);

                        // Create CheckBox
                        CheckBox c = new CheckBox(getContext());

                        // Restore value
                        if (sharedPreferences != null) {
                            Set<String> checkboxSet = sharedPreferences.getStringSet(element.getId(), null);

                            if (checkboxSet != null && checkboxSet.contains(checkbox.getId())) {

                                c.setChecked(true);

                            }

                        }

                        // Set checkbox attributes
                        setElementId(c, viewId++, checkbox.getId());

                        if(checkbox.getText() != null)
                            c.setText(checkbox.getText());

                        // Add checkbox to the container
                        ((LinearLayout)customView).addView(c);

                    }
                    break;


                case Constants.FORM_ELEMENT_TYPE_RADIO_GROUP:

                    // Get radios
                    List<FormSubElement> radios = element.getContents();

                    // Create a container
                    customView = new RadioGroup(getContext());

                    // Set the id
                    setElementId(customView, viewId++, element.getId());

                    // Change orientation if necessary
                    if (radios.size() < 3)
                        ((RadioGroup)customView).setOrientation(RadioGroup.HORIZONTAL);

                    // Add label
                    if (element.getOption("label") != null) {

                        TextView label = new TextView(getContext());

                        label.setText(element.getOption("label"));

                        ((RadioGroup)customView).addView(label);

                    }

                    // Create radios
                    for(int j = 0 ; j < radios.size() ; j++){

                        // Get radio info
                        FormSubElement radio = radios.get(j);

                        // Create Radio
                        RadioButton r = new RadioButton(getContext());

                        // Restore value
                        if (sharedPreferences != null) {
                            String radioSelectedId = sharedPreferences.getString(element.getId(), null);

                            if (radioSelectedId != null && radio.getId().equals(radioSelectedId) ) {

                                r.setChecked(true);

                            }

                        }

                        // Set radio attributes
                        setElementId(r, viewId++, radio.getId());

                        if(radio.getText() != null)
                            r.setText(radio.getText());

                        // Add radio to the container
                        ((RadioGroup)customView).addView(r);

                    }
                    break;


            }

            if (customView != null) this.addView(customView);

        }

    }

    /**
     * save()
     *
     * Save the data
     * */
    public void save(List<FormElement> elements, SharedPreferences sharedPreferences) {

        Log.d(TAG, "Saving Personal information...");
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (int i = 0; i < elements.size(); i++) {

            // Get params
            FormElement element = elements.get(i);

            // Save only data with an ID
            if (element.getId() != null) {

                switch (element.getType()){

                    case Constants.FORM_ELEMENT_TYPE_INPUT_TEXT:

                        editor.putString(element.getId(),
                                ((TextView)findViewById(MyR.get(element.getId()))).getText().toString() );

                        break;

                    case Constants.FORM_ELEMENT_TYPE_CHECK_GROUP:

                        // Get checkboxes
                        List<FormSubElement> checkboxes = element.getContents();

                        // Create Set
                        Set<String> checkGroup = new HashSet<>();

                        // Save every checkboxes
                        for(int j = 0 ; j < checkboxes.size() ; j++) {

                            // Get checkbox info
                            FormSubElement checkboxObject = checkboxes.get(j);

                            // Get CheckBox by id
                            CheckBox checkBox = ((CheckBox) findViewById(MyR.get(checkboxObject.getId())));

                            // Save checked
                            if(checkBox.isChecked()) {
                                checkGroup.add(checkboxObject.getId());
                            }

                        }

                        // Save all checked checkbox
                        editor.putStringSet(element.getId(), checkGroup);

                        break;


                    case Constants.FORM_ELEMENT_TYPE_RADIO_GROUP:

                        // Get radios
                        List<FormSubElement> radios = element.getContents();

                        // Get radiogroup by id
                        RadioGroup radioGroupView = ((RadioGroup) findViewById(MyR.get(element.getId())));

                        // Get checked radio button id
                        int checkedRadioButton = radioGroupView.getCheckedRadioButtonId();

                        // Create radios
                        for(int j = 0 ; j < radios.size() ; j++){

                            // Get radio info
                            FormSubElement radioInfo = radios.get(j);

                            // Insert id if checked
                            if (MyR.get(radioInfo.getId()) == checkedRadioButton){
                                // Italy
                                editor.putString(element.getId(), radioInfo.getId());
                            }

                        }

                        break;
                }

            }

        }

        editor.apply();

    }



}
