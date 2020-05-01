package com.derogab.adlanalyzer.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

import com.derogab.adlanalyzer.HttpGetRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import androidx.annotation.Nullable;

public abstract class CustomLayout extends LinearLayout {

    public CustomLayout(Context context) {
        super(context);
    }

    public CustomLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected JSONObject getTemplate(String url) {

        String json_str = null;
        try {
            json_str = new HttpGetRequest().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        JSONObject template = null;
        try {
            template = new JSONObject(json_str);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return template;

    }

    public abstract void generate(String url);
    public abstract void generate(JSONObject json);

}
