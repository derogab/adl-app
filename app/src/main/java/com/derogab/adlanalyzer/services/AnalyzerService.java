package com.derogab.adlanalyzer.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.derogab.adlanalyzer.MainActivity;
import com.derogab.adlanalyzer.R;
import com.derogab.adlanalyzer.connections.Connection;
import com.derogab.adlanalyzer.models.Activity;
import com.derogab.adlanalyzer.models.FormElement;
import com.derogab.adlanalyzer.models.FormGroup;
import com.derogab.adlanalyzer.repositories.ActivitiesRepository;
import com.derogab.adlanalyzer.repositories.FormTemplateRepository;
import com.derogab.adlanalyzer.utils.Constants;
import com.derogab.adlanalyzer.utils.CurrentLang;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AnalyzerService extends Service implements SensorEventListener {

    private static final String TAG = "AnalyzerService";

    // Sensor utility
    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener;
    // Connection to a socket
    private Connection conn;
    // Server info
    private String host;
    private int port;
    // Current data information
    private String archive;
    private String phonePosition;
    // Preparation time and timer
    private long preparationTime;
    private CountDownTimer preparationTimer;
    // Data counter
    private int index;
    // TTS
    private TextToSpeech textToSpeech;
    // Activities List
    private MutableLiveData<List<Activity>> activities;
    // Form Template Groups List
    private MutableLiveData<List<FormGroup>> templateGroups;
    // Headers check
    private boolean headersHaveBeenSent = false;
    // Previously predicted activity
    private long previouslyPredictedActivity = Constants.NO_INTEGER_DATA;

    /**
     * Notification channel creation
     * */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.analyzer_service_channel_name);
            String description = getString(R.string.analyzer_service_channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(Constants.ANALYZER_SERVICE_NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

    }

    public LiveData<List<FormGroup>> getFormTemplate() {

        if (templateGroups == null) {
            templateGroups = new MutableLiveData<List<FormGroup>>();

            Log.d(TAG, "Download template...");

            FormTemplateRepository.getInstance().getFormTemplate(templateGroups);

        }

        Log.d(TAG, "Get template...");
        return templateGroups;
    }

    public LiveData<List<Activity>> getActivities() {

        if (activities == null) {
            activities = new MutableLiveData<List<Activity>>();

            Log.d(TAG, "Download activities...");

            ActivitiesRepository.getInstance().getActivities(activities);

        }

        Log.d(TAG, "Get activities...");
        return activities;

    }

    /**
     * Create the data message json
     *
     * @param archive archive to close
     * @param sensor the sensor data
     * @param phonePosition the phone position
     * @param x the x axis value
     * @param y the y axis value
     * @param z the z axis value
     *
     * @return the data message json
     * */
    private String getCollectionDataMessage(String archive,
                                            String phonePosition,
                                            String sensor,
                                            float x,
                                            float y,
                                            float z,
                                            long t){

        index++;

        // Create data
        String jsonData = null;
        try {

            jsonData = new JSONObject()
                    .put("status", "OK")
                    .put("mode", Constants.SERVER_REQUEST_MODE_ANALYZER)
                    .put("data", new JSONObject()
                            .put("archive", archive)
                            .put("type", "data")
                            .put("info", new JSONObject()
                                    .put("index", index)
                                    .put("sensor", sensor)
                                    .put("position", phonePosition))
                            .put("values", new JSONObject()
                                    .put("x", x)
                                    .put("y", y)
                                    .put("z", z)
                                    .put("t", t)
                                )).toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonData;

    }

    /**
     * Create a custom message json
     *
     * @param archive archive to close
     * @param type message type
     *
     * @return the custom message json
     * */
    private String getMessage(String archive, String type) {

        String jsonData = null;
        try {

            jsonData = new JSONObject()
                    .put("status", "OK")
                    .put("mode", Constants.SERVER_REQUEST_MODE_ANALYZER)
                    .put("data", new JSONObject()
                            .put("archive", archive)
                            .put("type", type)).toString();

        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonData;

    }

    /**
     * Get headers
     * Generate headers from saved data
     *
     * @return the headers JSON string generated
     *
     * */
    private String getHeaders() throws JSONException {

        // Get form template
        List<FormGroup> groups = getFormTemplate().getValue();

        if (groups != null) {

            JSONObject additionalData = new JSONObject();
            SharedPreferences sharedPreferences = getSharedPreferences(Constants.PERSONAL_DATA_INFORMATION_FILE_NAME, Context.MODE_PRIVATE);

            for (int g = 0; g < groups.size(); g++) {

                FormGroup group = groups.get(g);
                List<FormElement> elements = group.getElements();

                for (int i = 0; i < elements.size(); i++) {

                    // Get params
                    FormElement element = elements.get(i);

                    // Save only data with an ID and that isUploadable
                    if (element.getId() != null && element.isUploadable()) {

                        String strToSend = null;
                        JSONArray arrayToSend = null;

                        switch (element.getType()){

                            case Constants.FORM_ELEMENT_TYPE_INPUT_TEXT:
                            case Constants.FORM_ELEMENT_TYPE_RADIO_GROUP:
                                strToSend = sharedPreferences.getString(element.getId(), null);
                                break;
                            case Constants.FORM_ELEMENT_TYPE_CHECK_GROUP:
                                Set<String> checkboxSet = sharedPreferences.getStringSet(element.getId(), null);
                                arrayToSend = new JSONArray();

                                if (checkboxSet != null)
                                    for (String checkboxSetItem : checkboxSet)
                                        arrayToSend.put(checkboxSetItem);

                                break;

                        }

                        if (strToSend != null)
                            additionalData.put(element.getId(), strToSend);
                        else if (arrayToSend != null)
                            additionalData.put(element.getId(), arrayToSend);

                    }

                }

            }

            // Headers
            JSONObject additionalDataPackage = new JSONObject()
                    .put("status", "OK")
                    .put("mode", Constants.SERVER_REQUEST_MODE_ANALYZER)
                    .put("data", new JSONObject()
                            .put("archive", archive)
                            .put("type", "headers")
                            .put("values", additionalData));

            return additionalDataPackage.toString();

        }
        else return null; // form template not downloaded

    }

    /**
     * Send headers to Server
     *
     * */
    private void sendHeaders() throws JSONException {

        // Get headers
        String headers = getHeaders();
        // Headers set
        if (headers != null) {
            // Send headers
            sendData(headers);
            // Check headers as sent
            headersHaveBeenSent = true;
        }

    }

    /**
     * Receive data from Server
     *
     * @param dataReceived data received from the server
     * */
    private void readReceivedData(String dataReceived) throws JSONException {

        JSONObject response = new JSONObject(dataReceived);

        if (response.getString("status").equals("OK")) {

            // Close the connection and so the service
            if (response.getString("type").equals("close")
                    || response.getString("type").equals("goodbye")) {

                Log.d(TAG, "Closed received.");
                stopSelf();

            }

            // Receive confirmations
            else if (response.getString("type").equals("ack")) {

                Log.d(TAG, "Ack received.");

                // Trigger send headers if not previously sent
                if (!headersHaveBeenSent) sendHeaders();

            }

            // Receive handshake
            else if (response.getString("type").equals("handshake")) {

                Log.d(TAG, "Handshake received.");

                // Trigger send headers if not previously sent
                if (!headersHaveBeenSent) sendHeaders();

            }

            // Receive predictions
            else if (response.getString("type").equals("prediction")) {

                // Get prediction
                long activity = response.getInt("activity");

                if (activity != previouslyPredictedActivity) {

                    String predictionOutput = null;
                    List<Activity> activities = getActivities().getValue();

                    // Search predicted activity name
                    if (activities != null)
                        for (int i = 0; i < activities.size(); i++)
                            if (activities.get(i).getId() == activity)
                                predictionOutput = activities.get(i).getTranslations().getLang(CurrentLang.getInstance().getLang());

                    // Speak result
                    if (predictionOutput != null) {

                        // TTS Prediction
                        speak(getString(R.string.tts_prediction, predictionOutput));

                        // Send prediction to UI
                        Intent sendPrediction = new Intent();
                            sendPrediction.setAction("ANALYZER_PREDICTION");
                            sendPrediction.putExtra( "PREDICTION", predictionOutput);
                        sendBroadcast(sendPrediction);

                        // Save previously selected
                        previouslyPredictedActivity = activity;

                    }

                }

            }

        }

    }

    /**
     * Send data to Server
     *
     * @param data data to send to the server
     * */
    private void sendData(String data) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                // if connected
                if (conn != null && data != null)
                    // send the data to the server
                    conn.sendMessage(data);

            }

        }).start();

    }

    /**
     * onBind() the service
     * */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * On Sensor changed
     * When receive a sensor data
     *
     * @param event the sensor change event
     * */
    @Override
    public void onSensorChanged(SensorEvent event) {

        // Filter and send
        if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            Log.d(TAG, "[GYROSCOPE] ID: "+archive+", POS: "+phonePosition+", X: "+x+", Y: "+y+", Z: "+z);

            sendData(getCollectionDataMessage(archive, phonePosition, Constants.SENSOR_GYROSCOPE, x, y, z, new Date().getTime()));

        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            Log.d(TAG, "[ACCELEROMETER] ID: "+archive+", POS: "+phonePosition+", X: "+x+", Y: "+y+", Z: "+z);

            sendData(getCollectionDataMessage(archive, phonePosition, Constants.SENSOR_ACCELEROMETER, x, y, z, new Date().getTime()));

        }

    }

    /**
     * On Accuracy changed
     * When change the accuracy
     *
     * @param sensor the sensor
     * @param accuracy the updated value
     * */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    /**
     * On Create
     * When the service is created
     * */
    @Override
    public void onCreate() {
        super.onCreate();

        // Set TTS object
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    Log.d(TAG, "Current Locale: "+getString(R.string.current_lang));

                    int ttsLang = textToSpeech.setLanguage(new Locale(getString(R.string.current_lang)));

                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA
                            || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e(TAG, "[TTS] The Language is not supported!");
                    } else {
                        Log.i(TAG, "[TTS] Language Supported.");
                    }
                    Log.i(TAG, "[TTS] Initialization success.");
                } else {

                    Log.e(TAG, "[TTS] Initialization failed!");
                }
            }
        });
    }

    /**
     * On Destroy
     * When the service is stopped
     * */
    @Override
    public void onDestroy() {

        // Send close & destroy message to server
        sendData(getMessage(archive, "close"));
        sendData(getMessage(archive, "destroy"));

        // Send destroy message to UI
        Intent sendDestroy = new Intent();
            sendDestroy.setAction("ANALYZER_SERVICE_DESTROY");
            sendDestroy.putExtra( "SERVICE_DESTROY", true);
        sendBroadcast(sendDestroy);

        // Cancel the countdown timer
        if (preparationTimer != null) {
            preparationTimer.cancel();
            preparationTimer = null;
        }

        // Stop Sensors listener
        if (sensorManager != null && sensorEventListener != null)
            sensorManager.unregisterListener(sensorEventListener);

        // Destroy TTS
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }

        // Close connection
        if (conn != null) {
            conn.close();
            conn = null;
        }

        // And then
        super.onDestroy();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            // Connect to the server & set callbacks
            conn = new Connection(host, port, new Connection.OnMessageReceivedListener() {
                @Override
                public void messageReceived(String message) {

                    try {
                        readReceivedData(message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Connection.OnConnectionErrorListener() {
                @Override
                public void onConnectionError() {

                    // Send connection error to UI
                    Intent sendError = new Intent();
                        sendError.setAction("ANALYZER_CONNECTION_ERROR");
                        sendError.putExtra( "CONNECTION_ERROR", getString(R.string.error_server_connection));
                    sendBroadcast(sendError);

                    // Just close the service
                    stopSelf();

                }
            }, new Connection.OnConnectionSuccessListener() {
                @Override
                public void onConnectionSuccess() {

                    // start the timer before start
                    preparationTimer.start();

                    // and communicate to UI that it is started
                    Intent sendStart = new Intent();
                        sendStart.setAction("ANALYZER_SERVICE_START");
                        sendStart.putExtra( "SERVICE_START", true);
                        sendStart.putExtra( "PREPARATION_TIME", preparationTime);
                    sendBroadcast(sendStart);

                }
            });
            conn.run();

        }
    };

    /**
     * On Start Command
     * All service task
     *
     * @param intent the current intent w/ extras
     * @param flags other flags
     * @param startId  the started id
     * */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Init
        sensorEventListener = this;

        // Create notification intent
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

        createNotificationChannel();

        // Create notification
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), Constants.ANALYZER_SERVICE_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.analyzer_service_channel_name))
                .setContentText(getString(R.string.analyzer_service_channel_description))
                .setSmallIcon(R.drawable.ic_search_black_24dp)
                .setContentIntent(pendingIntent)
                .setProgress(0,0,true) // indeterminate progress
                .build();

        // Start foreground notification
        startForeground(Constants.ANALYZER_NOTIFICATION_ID, notification);

        // Destroy or continue...
        if (intent == null) {
            // Destroy service if intent is null
            stopSelf();
        }
        else{
            // Get input data from fragment: task data
            archive = intent.getStringExtra(Constants.LEARNING_SERVICE_ARCHIVE);
            phonePosition = intent.getStringExtra(Constants.LEARNING_SERVICE_PHONE_POSITION);
            preparationTime = intent.getIntExtra(Constants.LEARNING_SERVICE_PREPARATION_TIMER,
                    Constants.LEARNING_COUNTDOWN_PREPARATION_SECONDS_DEFAULT);
            // Get input data from fragment: server information
            host = intent.getStringExtra(Constants.PREFERENCE_SERVER_DESTINATION);
            port = intent.getIntExtra(Constants.PREFERENCE_SERVER_PORT, Constants.SERVER_HOST_PORT);

            // Init sensor manager
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

            // Init counter value
            index = 0;

            // Set the countdown
            preparationTimer = new CountDownTimer(preparationTime * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long secondsUntilFinished = millisUntilFinished / 1000;

                    Log.d(TAG, "seconds remaining before start: " + secondsUntilFinished);

                    Intent sendTime = new Intent();
                        sendTime.setAction("ANALYZER_PREPARATION_COUNTDOWN");
                        sendTime.putExtra( "PREPARATION_COUNTDOWN", secondsUntilFinished);
                    sendBroadcast(sendTime);

                    // Voice alert
                    if (secondsUntilFinished == 3)
                        speak(getString(R.string.tts_analysis_almost_started));

                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "preparation timer done!");

                    // Start sensors
                    if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER))
                        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), Constants.SAMPLING_PERIOD);
                    if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE))
                        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), Constants.SAMPLING_PERIOD);

                    // Send data to UI
                    Intent sendStart = new Intent();
                        sendStart.setAction("ANALYZER_ACTIVITY_START");
                        sendStart.putExtra( "ACTIVITY_START", true);
                    sendBroadcast(sendStart);

                    // Voice alert
                    speak(getString(R.string.tts_analysis_started));

                }
            };

            // Start all service tasks
            new Thread(runnable).start();

        }

        return super.onStartCommand(intent, flags, startId);
    }



    private boolean speak(String tts) {

        if (textToSpeech == null) return false;

        int speechStatus = textToSpeech.speak(tts, TextToSpeech.QUEUE_FLUSH, null);

        if (speechStatus == TextToSpeech.ERROR) {
            Log.e(TAG, "[TTS] Error in converting Text to Speech!");
            return false;
        }

        return true;
    }

}
