package com.derogab.adlanalyzer.ui.personal;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.derogab.adlanalyzer.models.FormElement;
import com.derogab.adlanalyzer.models.FormGroup;
import com.derogab.adlanalyzer.models.FormValue;
import com.derogab.adlanalyzer.repositories.FormTemplateRepository;

import java.util.ArrayList;
import java.util.List;

public class PersonalViewModel extends ViewModel {

    private final static String TAG = "PersonalViewModel";

    private MutableLiveData<List<FormGroup>> groups;
    private ArrayList<FormValue> values;

    public LiveData<List<FormGroup>> getFormTemplate() {

        if (groups == null) {
            groups = new MutableLiveData<List<FormGroup>>();

            Log.d(TAG, "Download template...");

            FormTemplateRepository.getInstance().getFormTemplate(groups);

        }

        Log.d(TAG, "Get template...");
        return groups;
    }

    public void addFormValue(int key, String value) {

        if (this.values == null)
            this.values = new ArrayList<FormValue>();

        if (getFormValue(key) != null)
            for (int i = 0; i < this.values.size(); i++)
                if (this.values.get(i).getKey() == key)
                    this.values.remove(i);

        this.values.add(new FormValue(key, value));

    }

    public void addFormValue(int key, List<String> values) {

        if (this.values == null)
            this.values = new ArrayList<FormValue>();

        if (getFormValues(key) != null)
            for (int i = 0; i < this.values.size(); i++)
                if (this.values.get(i).getKey() == key)
                    this.values.remove(i);

        this.values.add(new FormValue(key, values));

    }

    private boolean check(List<String> l, String s) {
        if (l != null && s != null)
            for (int i = 0; i < l.size(); i++)
                if (l.get(i).equals(s))
                    return true;
        return false;
    }

    private List<String> union(List<String> l1, List<String> l2) {

        if (l1 == null || l1.size() == 0)
            return l2;

        if (l2 == null || l2.size() == 0)
            return l1;

        for (int i = 0; i < l2.size(); i++)
            if (!check(l1, l2.get(i)))
                l1.add(l2.get(i));

        return l1;
    }

    public void appendFormValue(int key, List<String> values) {

        if (this.values == null ||
                (getFormValues(key) == null && getFormValue(key) == null))
            addFormValue(key, values);

        if (getFormValues(key) != null)
            addFormValue(key, union(values, getFormValues(key)));

    }

    public void appendFormValue(int key, String value) {

        ArrayList<String> toConcat = new ArrayList<>();
        toConcat.add(value);

        appendFormValue(key, toConcat);

    }

    public String getFormValue(int key) {

        if (this.values != null)
            for (int i = 0; i < this.values.size(); i++)
                if (this.values.get(i).getKey() == key)
                    return this.values.get(i).getValue();

        return null;
    }

    public List<String> getFormValues(int key) {

        if (this.values != null)
            for (int i = 0; i < this.values.size(); i++)
                if (this.values.get(i).getKey() == key)
                    return this.values.get(i).getValues();

        return null;
    }

    public void removeFormValue(int key) {

        if (this.values != null)
            for (int i = 0; i < this.values.size(); i++)
                if (this.values.get(i).getKey() == key)
                    this.values.remove(i);

    }

    public void removeFormValue(int key, String subValue) {

        if (this.values != null)
            for (int i = 0; i < this.values.size(); i++)
                if (this.values.get(i).getKey() == key)
                    for (int j = 0; j < this.values.get(i).getValues().size(); j++)
                        if (this.values.get(i).getValues().get(j).equals(subValue))
                            this.values.get(i).getValues().remove(j);

    }

}
