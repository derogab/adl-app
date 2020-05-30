package com.derogab.adlanalyzer.services;

import android.app.Service;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import com.derogab.adlanalyzer.connections.Connection;
import com.derogab.adlanalyzer.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.Nullable;

public class LearningService extends Service implements SensorEventListener {

    private static final String TAG = "LearningService";

    private int index;

    private CountDownTimer preparationTimer;
    private CountDownTimer activityTimer;

    private SensorManager sensorManager;
    private Context mContext;

    private Connection conn;

    // Activity info
    private String sendingArchive;
    private String activityToAnalyze;
    private String phonePosition;
    private long activityTime;
    // Sensors status
    private boolean isSensorAccelerometerActive;
    private boolean isSensorGyroscopeActive;
    // Destination server
    private String host;
    private int port;

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            Log.d(TAG, "[GYROSCOPE] ID: "+sendingArchive+", ACTIVITY: "+activityToAnalyze+", POS: "+phonePosition+", X: "+x+", Y: "+y+", Z: "+z);

            // @TODO: send gyroscope data to server
            sendCollectedData(sendingArchive, activityToAnalyze, Constants.SENSOR_GYROSCOPE, phonePosition, x, y, z);

        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            Log.d(TAG, "[ACCELEROMETER] ID: "+sendingArchive+", ACTIVITY: "+activityToAnalyze+", POS: "+phonePosition+", X: "+x+", Y: "+y+", Z: "+z);

            // @TODO: send accelerometer data to server
            sendCollectedData(sendingArchive, activityToAnalyze, Constants.SENSOR_ACCELEROMETER, phonePosition, x, y, z);

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Creating...");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (conn != null) {
            conn.close();
            Log.d(TAG, "Connection closed.");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand(): start sensors...");

        mContext = this;

        index = 0; // init counter

        // Get input data from fragment
        sendingArchive = intent.getStringExtra(Constants.LEARNING_SERVICE_ARCHIVE);
        activityToAnalyze = intent.getStringExtra(Constants.LEARNING_SERVICE_ACTIVITY);
        phonePosition = intent.getStringExtra(Constants.LEARNING_SERVICE_PHONE_POSITION);
        activityTime = intent.getIntExtra(Constants.LEARNING_SERVICE_ACTIVITY_TIMER, -1);

        isSensorAccelerometerActive = intent.getBooleanExtra(Constants.LEARNING_SERVICE_SENSOR_STATUS_ACCELEROMETER, true);
        isSensorGyroscopeActive = intent.getBooleanExtra(Constants.LEARNING_SERVICE_SENSOR_STATUS_GYROSCOPE, true);

        host = intent.getStringExtra(Constants.PREFERENCE_SERVER_DESTINATION);
        port = intent.getIntExtra(Constants.PREFERENCE_SERVER_PORT, 8080);


        Log.d(TAG, "activityTime: " + activityTime);

        // Init sensor manager
        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        // Init PackageManager
        PackageManager packageManager = getPackageManager();

        // Set preparation timer
        preparationTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsUntilFinished = millisUntilFinished / 1000;

                Log.d(TAG, "seconds remaining before start: " + secondsUntilFinished);

                Intent sendTime = new Intent();
                    sendTime.setAction("GET_PREPARATION_COUNTDOWN");
                    sendTime.putExtra( "PREPARATION_COUNTDOWN", secondsUntilFinished);
                sendBroadcast(sendTime);

            }

            @Override
            public void onFinish() {
                Log.d(TAG, "preparation timer done!");

                // Start sensors
                if (packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER) && isSensorAccelerometerActive)
                    sensorManager.registerListener((SensorEventListener) mContext, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
                if (packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE) && isSensorGyroscopeActive)
                    sensorManager.registerListener((SensorEventListener) mContext, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);

                // Send data
                Intent sendTime = new Intent();
                    sendTime.setAction("GET_ACTIVITY_START");
                    sendTime.putExtra( "ACTIVITY_START", true);
                sendBroadcast(sendTime);

                // Start activity timer
                activityTimer.start();
            }
        };

        // Set activity timer
        activityTimer = new CountDownTimer(activityTime * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsUntilFinished = millisUntilFinished / 1000;

                Log.d(TAG, "seconds remaining: " + secondsUntilFinished);

                Intent sendTime = new Intent();
                    sendTime.setAction("GET_ACTIVITY_COUNTDOWN");
                    sendTime.putExtra( "ACTIVITY_COUNTDOWN", secondsUntilFinished);
                sendBroadcast(sendTime);

            }

            @Override
            public void onFinish() {
                Log.d(TAG, "activity timer done!");

                // Stop Sensors listener
                sensorManager.unregisterListener((SensorEventListener) mContext);

                // Send end to UI
                Intent sendTime = new Intent();
                    sendTime.setAction("GET_ACTIVITY_END");
                    sendTime.putExtra( "ACTIVITY_END", true);
                sendBroadcast(sendTime);

                // Send close to server
                sendClose(sendingArchive);

            }
        };

        // Connect to the server
        conn = new Connection(host, port, new Connection.OnMessageReceived() {
            @Override
            public void messageReceived(String message) {

                //Log.d("LearningServiceResponse", "Response: " + message);

                try {
                    readReceivedData(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        conn.run();

        // Start service task
        preparationTimer.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Receive data from Server
     *
     * @param dataReceived data received from the server
     * */
    private void readReceivedData(String dataReceived) throws JSONException {

        JSONObject response = new JSONObject(dataReceived);

        if (response.getString("status").equals("OK")) {

            if (response.getString("type").equals("close")) {

                Log.d(TAG, "All data sent. Stop the service...");
                stopSelf();

            }

            else if (response.getString("type").equals("ack")) {

                Log.d(TAG, "Ack received.");

            }

        }

    }

    /**
     * Send data to Server
     *
     * @param data data to send to the server
     * */
    private void sendData(String data) {

        //sends the message to the server
        if (conn != null && data != null) conn.sendMessage(data);

    }

    private void sendCollectedData(String id,
                          String activity,
                          String sensor,
                          String phonePosition,
                          float x,
                          float y,
                          float z){

        index++;

        // Create data
        String jsonData = null;
        try {

            jsonData = new JSONObject()
                    .put("status", "OK")
                    .put("response", new JSONObject()
                            .put("archive", id)
                            .put("type", "data")
                            .put("info", new JSONObject()
                                .put("index", index)
                                .put("activity", activity)
                                .put("sensor", sensor)
                                .put("position", phonePosition))
                            .put("data", new JSONObject()
                                .put("x", x)
                                .put("y", y)
                                .put("z", z))).toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Send data
        sendData(jsonData);

    }



    private void sendClose(String id) {

        String jsonData = null;
        try {

            jsonData = new JSONObject()
                    .put("status", "OK")
                    .put("error", false)
                    .put("response", new JSONObject()
                            .put("archive", id)
                            .put("type", "close")).toString();

        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        sendData(jsonData);

    }


}
