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
    // Data counter
    private int index;

    /**
     * Notification channel creation
     * */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.service_analyzer_channel_name);
            String description = getString(R.string.service_analyzer_channel_description);
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

        Log.d(TAG, "OKKKKKK");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Stop Sensors listener
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "OKKKKKK2");

        // Init
        sensorEventListener = this;

        // Create notification intent
        Intent notificationIntent = new Intent(this, AnalyzerFragment.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

        createNotificationChannel();

        // Create notification
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), Constants.ANALYZER_SERVICE_NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Analyzer Service")
                .setContentText("Analyzing in progress...")
                .setSmallIcon(R.drawable.ic_search_black_24dp)
                .setContentIntent(pendingIntent)
                .build();

        // Start foreground notification
        startForeground(2, notification);

        // Get input data from fragment: task data
        archive = intent.getStringExtra(Constants.LEARNING_SERVICE_ARCHIVE);
        phonePosition = intent.getStringExtra(Constants.LEARNING_SERVICE_PHONE_POSITION);
        // Get input data from fragment: server information
        String host = intent.getStringExtra(Constants.PREFERENCE_SERVER_DESTINATION);
        int port = intent.getIntExtra(Constants.PREFERENCE_SERVER_PORT, 8080);

        // Init sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Init PackageManager
        PackageManager packageManager = getPackageManager();

        // Init counter value
        index = 0;

        // Start sensors
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER))
            sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE))
            sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);


        return super.onStartCommand(intent, flags, startId);
    }


}
