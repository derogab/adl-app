package com.derogab.adlanalyzer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class JsonSource {

    JSONObject jsonObject;

    public JsonSource(String url) {

        String json_str = null;
        try {
            json_str = new HttpGetRequest().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        try {
            jsonObject = new JSONObject(json_str);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public JSONObject getJSON(){
        return jsonObject;
    }

}
