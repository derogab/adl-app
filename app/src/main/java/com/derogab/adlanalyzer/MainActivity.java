package com.derogab.adlanalyzer;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.util.Log;

import com.derogab.adlanalyzer.models.Activity;
import com.derogab.adlanalyzer.utils.PhonePosition;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";

    private String phonePosition;
    private String activityToAnalyze;
    private String sendingMode;
    private String sendingArchive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_analyzer, R.id.navigation_learning, R.id.navigation_personal)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        // Set default headers
        phonePosition = PhonePosition.IN_RIGHT_HAND;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            Log.d(TAG, "[GYROSCOPE] ID: "+sendingArchive+", ACTIVITY: "+activityToAnalyze+", POS: "+phonePosition+", MODE: "+sendingMode+", X: "+x+", Y: "+y+", Z: "+z);

            // @TODO: send gyroscope data to server

        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            Log.d(TAG, "[ACCELEROMETER] ID: "+sendingArchive+", ACTIVITY: "+activityToAnalyze+", POS: "+phonePosition+", MODE: "+sendingMode+", X: "+x+", Y: "+y+", Z: "+z);

            // @TODO: send accelerometer data to server

        }

    }

    public void setPhonePosition(PhonePosition phonePosition) {
        this.phonePosition = phonePosition.getPosition();
    }

    public void setActivityToAnalyze(Activity activityToAnalyze) {
        this.activityToAnalyze = activityToAnalyze.toString();
    }

    public void setSendingMode(String sendingMode) {
        this.sendingMode = sendingMode;
    }

    public void setSendingArchive(String sendingArchive) {
        this.sendingArchive = sendingArchive;
    }

    public void setHeaders(String id, Activity activity, PhonePosition positionPhone, String sendingMode) {

        setSendingArchive(id);
        setActivityToAnalyze(activity);
        setPhonePosition(positionPhone);
        setSendingMode(sendingMode);

    }

}
