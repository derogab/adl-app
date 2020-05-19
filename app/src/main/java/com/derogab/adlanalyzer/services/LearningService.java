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

import com.derogab.adlanalyzer.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

public class LearningService extends Service implements SensorEventListener {

    private static final String TAG = "LearningService";

    private CountDownTimer preparationTimer;
    private CountDownTimer activityTimer;

    private SensorManager sensorManager;

    private Context mContext;

    private String sendingArchive;
    private String activityToAnalyze;
    private String phonePosition;
    private long activityTime;

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
            sendData(sendingArchive, activityToAnalyze, Constants.SENSOR_GYROSCOPE, phonePosition, x, y, z);

        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            Log.d(TAG, "[ACCELEROMETER] ID: "+sendingArchive+", ACTIVITY: "+activityToAnalyze+", POS: "+phonePosition+", X: "+x+", Y: "+y+", Z: "+z);

            // @TODO: send accelerometer data to server
            sendData(sendingArchive, activityToAnalyze, Constants.SENSOR_ACCELEROMETER, phonePosition, x, y, z);

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate(): start sensors...");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand(): start sensors...");

        mContext = this;

        Log.d(TAG, "onStartJob");
        Log.d(TAG, "intent: " + intent);

        // Get input data from fragment
        sendingArchive = intent.getStringExtra(Constants.LEARNING_SERVICE_ARCHIVE);
        activityToAnalyze = intent.getStringExtra(Constants.LEARNING_SERVICE_ACTIVITY);
        phonePosition = intent.getStringExtra(Constants.LEARNING_SERVICE_PHONE_POSITION);
        activityTime = (long) intent.getIntExtra(Constants.LEARNING_SERVICE_ACTIVITY_TIMER, -1);

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
                Log.d(TAG, "seconds remaining before start: " + millisUntilFinished / 1000);

                Intent sendTime = new Intent();
                    sendTime.setAction("GET_PREPARATION_COUNTDOWN");
                    sendTime.putExtra( "PREPARATION_COUNTDOWN",millisUntilFinished / 1000);
                sendBroadcast(sendTime);

            }

            @Override
            public void onFinish() {
                Log.d(TAG, "preparation timer done!");

                // Start sensors
                if (packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER))
                    sensorManager.registerListener((SensorEventListener) mContext, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
                if (packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE))
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
                Log.d(TAG, "seconds remaining: " + millisUntilFinished / 1000);

                Intent sendTime = new Intent();
                    sendTime.setAction("GET_ACTIVITY_COUNTDOWN");
                    sendTime.putExtra( "ACTIVITY_COUNTDOWN", millisUntilFinished / 1000);
                sendBroadcast(sendTime);

            }

            @Override
            public void onFinish() {
                Log.d(TAG, "activity timer done!");

                // Stop Sensors listener
                sensorManager.unregisterListener((SensorEventListener) mContext);

                Intent sendTime = new Intent();
                    sendTime.setAction("GET_ACTIVITY_END");
                    sendTime.putExtra( "ACTIVITY_END", true);
                sendBroadcast(sendTime);
            }
        };

        // Start service task
        preparationTimer.start();

        return super.onStartCommand(intent, flags, startId);
    }



    @Override
    public void onDestroy() {

        Log.d(TAG, "onDestroy(): stop sensors...");

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    private void sendData(String id,
                          String activity,
                          String sensor,
                          String phonePosition,
                          float x,
                          float y,
                          float z){

        // Create data
        String jsonData = "{'error': true, 'cause': 'no data'}";
        try {

            jsonData = new JSONObject()
                    .put("id", id)
                    .put("activity", activity)
                    .put("sensor", sensor)
                    .put("position", phonePosition)
                    .put("coords", new JSONObject()
                            .put("x", x)
                            .put("y", y)
                            .put("z", z))
                    .toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "host: " + host);
        Log.d(TAG, "port: " + port);

        // Send data
        String dataToSend = jsonData;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                Socket socket = null;
                try {
                    socket = new Socket(host, port);
                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                    printWriter.write(dataToSend);
                    printWriter.flush();
                    printWriter.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };
        new Thread(runnable).start();


    }


}
