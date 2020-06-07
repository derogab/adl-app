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

import com.derogab.adlanalyzer.R;
import com.derogab.adlanalyzer.connections.Connection;
import com.derogab.adlanalyzer.ui.learning.LearningFragment;
import com.derogab.adlanalyzer.utils.Constants;
import com.derogab.adlanalyzer.utils.CountDown;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class LearningService extends Service implements SensorEventListener {

    private static final String TAG = "LearningService";

    // Countdown timers
    private CountDownTimer preparationTimer;
    private CountDownTimer activityTimer;
    // Sensor utility
    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener;
    // Connection to a socket
    private Connection conn;
    // Current activity information
    private String sendingArchive;
    private String activityToAnalyze;
    private String phonePosition;
    private long activityTime;
    // Sensors status
    private boolean isSensorAccelerometerActive;
    private boolean isSensorGyroscopeActive;
    // Data counter
    private int index;

    /**
     * Notification channel creation
     * */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.learning_service_channel_name);
            String description = getString(R.string.learning_service_channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(Constants.LEARNING_SERVICE_NOTIFICATION_CHANNEL_ID, name, importance);
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

    /**
     * Create the closing message json
     *
     * @param archive archive to close
     *
     * @return the closing message json
     * */
    private String getClosingMessage(String archive) {

        String jsonData = null;
        try {

            jsonData = new JSONObject()
                    .put("status", "OK")
                    .put("error", false)
                    .put("response", new JSONObject()
                            .put("archive", archive)
                            .put("type", "close")).toString();

        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonData;

    }

    /**
     * Create the data message json
     *
     * @param archive archive to close
     * @param activity the activity to learn
     * @param sensor the sensor data
     * @param phonePosition the phone position
     * @param x the x axis value
     * @param y the y axis value
     * @param z the z axis value
     *
     * @return the data message json
     * */
    private String getCollectionDataMessage(String archive,
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
                            .put("archive", archive)
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

        return jsonData;

    }

    /**
     * Sensor callback
     * Triggered on sensor change
     *
     * @param event the sensor data
     * */
    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            Log.d(TAG, "[GYROSCOPE] ID: "+sendingArchive+", ACTIVITY: "+activityToAnalyze+", POS: "+phonePosition+", X: "+x+", Y: "+y+", Z: "+z);

            sendData(getCollectionDataMessage(sendingArchive, activityToAnalyze, Constants.SENSOR_GYROSCOPE, phonePosition, x, y, z));

        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            Log.d(TAG, "[ACCELEROMETER] ID: "+sendingArchive+", ACTIVITY: "+activityToAnalyze+", POS: "+phonePosition+", X: "+x+", Y: "+y+", Z: "+z);

            sendData(getCollectionDataMessage(sendingArchive, activityToAnalyze, Constants.SENSOR_ACCELEROMETER, phonePosition, x, y, z));

        }

    }

    /**
     * Accuracy callback
     * Triggered on accuracy change
     *
     * @param sensor sensor data
     * @param accuracy accuracy value
     * */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    /**
     * Destroy callback
     * Triggered on service destroy
     * */
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (conn != null) conn.close();

        stopForeground(true);
    }

    /**
     * Sensor callback
     * Triggered on accuracy change
     *
     * @param intent the intent
     * @param flags additional data about this start request
     * @param startId a unique integer representing this specific request to start
     * */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Init
        sensorEventListener = this;

        // Create notification intent
        Intent notificationIntent = new Intent(getApplicationContext(), LearningFragment.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

        createNotificationChannel();

        // Create notification
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), Constants.LEARNING_SERVICE_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Learning Service")
            .setContentText("Learning in progress...")
            .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
            .setContentIntent(pendingIntent)
            .build();

        // Start foreground notification
        startForeground(1, notification);

        // Get input data from fragment: task data
        sendingArchive = intent.getStringExtra(Constants.LEARNING_SERVICE_ARCHIVE);
        activityToAnalyze = intent.getStringExtra(Constants.LEARNING_SERVICE_ACTIVITY);
        phonePosition = intent.getStringExtra(Constants.LEARNING_SERVICE_PHONE_POSITION);
        activityTime = intent.getIntExtra(Constants.LEARNING_SERVICE_ACTIVITY_TIMER, -1);
        // Get input data from fragment: sensor activation status
        isSensorAccelerometerActive = intent.getBooleanExtra(Constants.LEARNING_SERVICE_SENSOR_STATUS_ACCELEROMETER, true);
        isSensorGyroscopeActive = intent.getBooleanExtra(Constants.LEARNING_SERVICE_SENSOR_STATUS_GYROSCOPE, true);
        // Get input data from fragment: server information
        String host = intent.getStringExtra(Constants.PREFERENCE_SERVER_DESTINATION);
        int port = intent.getIntExtra(Constants.PREFERENCE_SERVER_PORT, 8080);

        // Init sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Init PackageManager
        PackageManager packageManager = getPackageManager();

        // Init counter value
        index = 0;

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
                    sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
                if (packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE) && isSensorGyroscopeActive)
                    sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);

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

                // Send countdown data to UI
                Intent sendTime = new Intent();
                    sendTime.setAction("GET_ACTIVITY_COUNTDOWN");
                    sendTime.putExtra( "ACTIVITY_COUNTDOWN", secondsUntilFinished);
                sendBroadcast(sendTime);

                // Update notification countdown
                Notification notification = new NotificationCompat.Builder(getApplicationContext(), Constants.LEARNING_SERVICE_NOTIFICATION_CHANNEL_ID)
                        .setContentTitle("Learning Service")
                        .setContentText("Learning in progress... " + CountDown.get(secondsUntilFinished))
                        .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
                        .setContentIntent(pendingIntent)
                        .build();
                // Get notification manager
                NotificationManager notificationManager = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                    notificationManager = getSystemService(NotificationManager.class);
                else
                    notificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                // Push notification updated
                if (notificationManager != null)
                    notificationManager.notify(1, notification);

            }

            @Override
            public void onFinish() {

                // Stop Sensors listener
                sensorManager.unregisterListener(sensorEventListener);

                // Send end to UI
                Intent sendTime = new Intent();
                    sendTime.setAction("GET_ACTIVITY_END");
                    sendTime.putExtra( "ACTIVITY_END", true);
                sendBroadcast(sendTime);

                // Send close to server
                sendData(getClosingMessage(sendingArchive));

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
                    sendTime.putExtra( "CONNECTION_ERROR", "Error to connect to database.");
                sendBroadcast(sendTime);

                // Just close the service
                stopSelf();

            }
        }, new Connection.OnConnectionSuccessListener() {
            @Override
            public void onConnectionSuccess() {

                // Start service task
                preparationTimer.start();
                // and communicate to UI that it is started
                Intent sendTime = new Intent();
                    sendTime.setAction("GET_SERVICE_START");
                    sendTime.putExtra( "SERVICE_START", true);
                sendBroadcast(sendTime);

            }
        });
        conn.run();

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
