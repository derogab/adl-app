package com.derogab.adlanalyzer.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.derogab.adlanalyzer.R;
import com.derogab.adlanalyzer.connections.Connection;
import com.derogab.adlanalyzer.ui.analyzer.AnalyzerFragment;
import com.derogab.adlanalyzer.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class AnalyzerService extends Service implements SensorEventListener {

    private static final String TAG = "AnalyzerService";

    // Sensor utility
    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener;
    // Connection to a socket
    private Connection conn;
    // Current data information
    private String archive;
    private String phonePosition;
    // Preparation time and timer
    private long preparationTime;
    private CountDownTimer preparationTimer;
    // Data counter
    private int index;

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

    /**
     * Receive data from Server
     *
     * @param dataReceived data received from the server
     * */
    private void readReceivedData(String dataReceived) throws JSONException {

        JSONObject response = new JSONObject(dataReceived);

        if (response.getString("status").equals("OK")) {

            if (response.getString("type").equals("close")) {

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

        // if connected
        if (conn != null && data != null)
            // send the data to the server
            conn.sendMessage(data);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            Log.d(TAG, "[GYROSCOPE] ID: "+archive+", POS: "+phonePosition+", X: "+x+", Y: "+y+", Z: "+z);

            // @TODO Send data to server

        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            Log.d(TAG, "[ACCELEROMETER] ID: "+archive+", POS: "+phonePosition+", X: "+x+", Y: "+y+", Z: "+z);

            // @TODO Send data to server

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Cancel the countdown timer
        if (preparationTimer != null) preparationTimer.cancel();

        // Stop Sensors listener
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Init
        sensorEventListener = this;

        // Create notification intent
        Intent notificationIntent = new Intent(this, AnalyzerFragment.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

        createNotificationChannel();

        // Create notification
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), Constants.ANALYZER_SERVICE_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.analyzer_service_channel_name))
                .setContentText(getString(R.string.analyzer_service_channel_description))
                .setSmallIcon(R.drawable.ic_search_black_24dp)
                .setContentIntent(pendingIntent)
                .build();

        // Start foreground notification
        startForeground(2, notification);

        // Get input data from fragment: task data
        archive = intent.getStringExtra(Constants.LEARNING_SERVICE_ARCHIVE);
        phonePosition = intent.getStringExtra(Constants.LEARNING_SERVICE_PHONE_POSITION);
        preparationTime = intent.getIntExtra(Constants.LEARNING_SERVICE_PREPARATION_TIMER,
                Constants.LEARNING_COUNTDOWN_PREPARATION_SECONDS_DEFAULT);
        // Get input data from fragment: server information
        String host = intent.getStringExtra(Constants.PREFERENCE_SERVER_DESTINATION);
        int port = intent.getIntExtra(Constants.PREFERENCE_SERVER_PORT, Constants.SERVER_HOST_PORT);

        // Init sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Init PackageManager
        PackageManager packageManager = getPackageManager();

        // Init counter value
        index = 0;

        // Set the countdown
        preparationTimer = new CountDownTimer(preparationTime * 1000, 1000) {
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
                if (packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER))
                    sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
                if (packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE))
                    sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);

                // Send data
                Intent sendTime = new Intent();
                    sendTime.setAction("GET_ACTIVITY_START");
                    sendTime.putExtra( "ACTIVITY_START", true);
                sendBroadcast(sendTime);

            }
        };

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
                Intent sendTime = new Intent();
                    sendTime.setAction("GET_CONNECTION_ERROR");
                    sendTime.putExtra( "CONNECTION_ERROR", getString(R.string.error_server_connection));
                sendBroadcast(sendTime);

                // Just close the service
                stopSelf();

            }
        }, new Connection.OnConnectionSuccessListener() {
            @Override
            public void onConnectionSuccess() {

                // start the timer before start
                preparationTimer.start();

                // and communicate to UI that it is started
                Intent sendTime = new Intent();
                    sendTime.setAction("GET_SERVICE_START");
                    sendTime.putExtra( "SERVICE_START", true);
                    sendTime.putExtra( "PREPARATION_TIME", preparationTime);
                sendBroadcast(sendTime);

            }
        });
        conn.run();

        return super.onStartCommand(intent, flags, startId);
    }


}
