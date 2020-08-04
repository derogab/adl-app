package com.derogab.adlapp.ui.personal;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.derogab.adlapp.R;
import com.derogab.adlapp.models.FormElement;
import com.derogab.adlapp.models.FormGroup;
import com.derogab.adlapp.models.FormSubElement;
import com.derogab.adlapp.utils.Constants;

import com.derogab.adlapp.utils.CurrentLang;
import com.derogab.adlapp.utils.MyR;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.Nullable;

public class PersonalContainerLayout extends LinearLayout {

    private static final String TAG = "PersonalContainerLayout";

    private JSONArray contents;
    private String lang;

    public PersonalContainerLayout(Context context) {
        super(context);
        // Set current lang
        this.lang = CurrentLang.getInstance().getLang();
    }

    public PersonalContainerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        // Set current lang
        this.lang = CurrentLang.getInstance().getLang();
    }

    public PersonalContainerLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // Set current lang
        this.lang = CurrentLang.getInstance().getLang();
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
    public void generate(List<FormGroup> groups, SharedPreferences sharedPreferences, PersonalViewModel personalViewModel) {

        int viewId = 1;

        for (int g = 0; g < groups.size(); g++) {

            FormGroup group = groups.get(g);
            List<FormElement> elements = group.getElements();

            MaterialCardView groupCard = new MaterialCardView(getContext());
            // Set the CardView layoutParams
            LayoutParams layoutParams = new LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(0,0,0,8);
            groupCard.setLayoutParams(layoutParams);
            // Set cardView compact padding (default true in material)
            groupCard.setUseCompatPadding(true);
            // Set cardView content padding
            groupCard.setContentPadding(8,8,8,8);

            LinearLayout groupCardLayout = new LinearLayout(getContext());
                groupCardLayout.setOrientation(LinearLayout.VERTICAL);
                groupCardLayout.setPadding(8,8,8,8);
                groupCardLayout.setLayoutParams(new LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT
                ));

            for (int i = 0; i < elements.size(); i++) {

                FormElement element = elements.get(i);
                View customView = null;

                switch (element.getType()){

                    case Constants.FORM_ELEMENT_TYPE_TEXT_VIEW:

                        // Create a TextView
                        customView = new TextView(getContext());

                        // Set the id
                        customView.setId(viewId++);

                        // Set label text
                        if (element.getTranslations() != null
                                && element.getTranslations().getLang(lang) != null)
                            ((TextView)customView).setText(element.getTranslations().getLang(lang));
                        else if (element.getText() != null)
                            ((TextView)customView).setText(element.getText());

                        // Set attributes
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

                        // Set the id
                        setElementId(input_text, viewId++, element.getId());

                        // Set padding
                        input_text.setPadding(8,24,8,16);

                        // Set background color
                        TypedValue typedValue = new TypedValue();
                        getContext().getTheme().resolveAttribute(R.attr.colorDirty, typedValue, true);
                        input_text.setBackgroundColor(typedValue.data);

                        // Listener onChange
                        input_text.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                                if (MyR.get(element.getId()) != MyR.NO_ID)
                                    personalViewModel.addFormValue(MyR.get(element.getId()), s.toString());

                            }
                        });

                        // Restore value
                        if (personalViewModel.getFormValue(MyR.get(element.getId())) != null)
                            input_text.setText(personalViewModel.getFormValue(MyR.get(element.getId())));
                        else if (sharedPreferences != null) {
                            String storageValue = sharedPreferences.getString(element.getId(), "");

                            input_text.setText(storageValue);
                            personalViewModel.addFormValue(MyR.get(element.getId()), storageValue);
                        }

                        if (element.getOption("size") != null)
                            input_text.setTextSize(Integer.parseInt(element.getOption("size")));

                        // Set label text
                        if (element.getTranslations() != null
                                && element.getTranslations().getLang(lang) != null)
                            input_text.setHint(element.getTranslations().getLang(lang));
                        else if (element.getText() != null)
                            input_text.setHint(element.getText());

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

                        // Add label text
                        if (element.getTranslations() != null
                                && element.getTranslations().getLang(lang) != null) {

                            TextView label = new TextView(getContext());
                                label.setText(element.getTranslations().getLang(lang));

                            ((LinearLayout)customView).addView(label);

                        }
                        else if (element.getText() != null) {

                            TextView label = new TextView(getContext());
                            label.setText(element.getText());

                            ((LinearLayout)customView).addView(label);

                        }

                        // Create checkboxes
                        for(int j = 0 ; j < checkboxes.size() ; j++){

                            // Get checkbox info
                            FormSubElement checkbox = checkboxes.get(j);

                            // Create CheckBox
                            CheckBox c = new CheckBox(getContext());

                            // Set checkbox attributes
                            setElementId(c, viewId++, checkbox.getId());

                            // Listener onChange
                            c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if (MyR.get(element.getId()) != MyR.NO_ID) {

                                        if (isChecked)
                                            personalViewModel.appendFormValue(MyR.get(element.getId()),
                                                    checkbox.getId());
                                        else
                                            personalViewModel.removeFormValue(MyR.get(element.getId()),
                                                    checkbox.getId());

                                    }
                                }
                            });

                            // Restore value
                            if (personalViewModel.getFormValues(MyR.get(element.getId())) != null) {

                                ArrayList<String> checkboxList =
                                        (ArrayList<String>) personalViewModel.getFormValues(MyR.get(element.getId()));

                                if (checkboxList != null && checkboxList.contains(checkbox.getId())) {

                                    c.setChecked(true);

                                }


                            }
                            else if (sharedPreferences != null) {
                                Set<String> checkboxSet = sharedPreferences.getStringSet(element.getId(), null);

                                if (checkboxSet != null && checkboxSet.contains(checkbox.getId())) {

                                    c.setChecked(true);

                                }

                            }

                            if (checkbox.getTranslations() != null
                                    && checkbox.getTranslations().getLang(lang) != null)
                                c.setText(checkbox.getTranslations().getLang(lang));
                            else if(checkbox.getText() != null)
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

                        // Add label text
                        if (element.getTranslations() != null
                                && element.getTranslations().getLang(lang) != null) {

                            TextView label = new TextView(getContext());
                                label.setText(element.getTranslations().getLang(lang));

                            ((RadioGroup)customView).addView(label);

                        }
                        else if (element.getText() != null) {

                            TextView label = new TextView(getContext());
                                label.setText(element.getText());

                            ((RadioGroup)customView).addView(label);

                        }

                        // Create radios
                        for(int j = 0 ; j < radios.size() ; j++){

                            // Get radio info
                            FormSubElement radio = radios.get(j);

                            // Create Radio
                            RadioButton r = new RadioButton(getContext());

                            // Set radio attributes
                            setElementId(r, viewId++, radio.getId());

                            // Listener onChange
                            r.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                                    if (MyR.get(element.getId()) != MyR.NO_ID && isChecked)
                                        personalViewModel.addFormValue(MyR.get(element.getId()), radio.getId());

                                }
                            });

                            // Restore value
                            if (personalViewModel.getFormValue(MyR.get(element.getId())) != null){
                                String radioJustSelectedId = personalViewModel.getFormValue(MyR.get(element.getId()));

                                if (radioJustSelectedId != null && radio.getId().equals(radioJustSelectedId) ) {

                                    r.setChecked(true);

                                }

                            }
                            else if (sharedPreferences != null) {
                                String radioSelectedId = sharedPreferences.getString(element.getId(), null);

                                if (radioSelectedId != null && radio.getId().equals(radioSelectedId) ) {

                                    r.setChecked(true);

                                }

                            }

                            if(radio.getTranslations() != null
                                    && radio.getTranslations().getLang(lang) != null) {
                                r.setText(radio.getTranslations().getLang(lang));
                            }
                            else if(radio.getText() != null)
                                r.setText(radio.getText());

                            // Add radio to the container
                            ((RadioGroup)customView).addView(r);

                        }
                        break;


                }

                if (customView != null) groupCardLayout.addView(customView);

            }

            groupCard.addView(groupCardLayout);
            this.addView(groupCard);

        }

    }

    /**
     * save()
     *
     * Save the data
     * */
    public void save(List<FormGroup> groups, SharedPreferences sharedPreferences) {

        Log.d(TAG, "Saving Personal information...");
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (int g = 0; g < groups.size(); g++) {

            FormGroup group = groups.get(g);
            List<FormElement> elements = group.getElements();

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
                                    editor.putString(element.getId(), radioInfo.getId());
                                }

                            }

                            break;
                    }

                }

            }

        }

        editor.apply();

    }



}
