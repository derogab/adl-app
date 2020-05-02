package com.derogab.adlanalyzer.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

import com.derogab.adlanalyzer.HttpGetRequest;
import com.derogab.adlanalyzer.JsonSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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

    public static JSONObject getTemplate(String url) { return new JsonSource(url).getJSON(); }

    public abstract void setSource(String url) throws JSONException;
    public abstract void setSource(JSONObject jsonObject) throws JSONException;
    public abstract void setSource(JSONArray jsonArray);

    public abstract void generate() throws JSONException;

    public abstract ArrayList getData() throws JSONException;


}
