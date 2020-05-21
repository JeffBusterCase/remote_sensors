package com.jefferson.remotesensor;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorDirectChannel;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private TextView txtSample;
    private Thread sendToApiThread;
    protected static boolean SensorSendToApiThread_ENABLED = false;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        doSensorsSetup();

        sendToApiThread = new Thread(new SensorSendToApiThread(this));
        sendToApiThread.start();

        Button btn = findViewById(R.id.btn_request);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                /// not used anymore.
                SensorSendToApiThread_ENABLED = !SensorSendToApiThread_ENABLED;
            }
        });


    }

    class SensorSendToApiThread implements Runnable {
        private MainActivity main;
        public SensorSendToApiThread(MainActivity main) {
            SensorSendToApiThread.this.main = main;
        }
        public void run(){
            while(true){
                try { Thread.sleep(500); } catch (Exception ex) { ex.printStackTrace(); }
                if(MainActivity.SensorSendToApiThread_ENABLED) SensorSendToApiThread.this.main.sendSensorsInformation();
            }
        }
    }

    private SensorManager sensorManager;
    private List<Sensor> deviceSensors;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    protected void doSensorsSetup() {
        sensorsValues = new ArrayList<SensorValue>();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for(Sensor sensor : deviceSensors) {
            if(sensorManager.getDefaultSensor(sensor.getType()) != null) {
                SensorValue sensorValue = new SensorValue();
                sensorValue.SensorType = sensor.getType();
                sensorValue.SensorStringType = sensor.getStringType();
                sensorValue.SensorValues = new ArrayList<Float>();
                sensorValue.SensorVendor = sensor.getVendor();
                sensorValue.SensorVersion = sensor.getVersion();
                sensorValue.SensorChanged = true;

                sensorsValues.add(sensorValue);
//                txtSample.getEditableText().clear();
//                String newStr = "Sensors: " + sensorsValues.size();
            } // else sensor não existe.
        }


        txtSample = (TextView)findViewById(R.id.txt_sample_vw);
        txtSample.setText("Sensor " + sensorsValues.size());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do something
    }

    public class SensorValue {
        public int SensorType;
        public String SensorStringType;
        public List<Float> SensorValues;
        public String SensorVendor;
        public int SensorVersion;
        public boolean SensorChanged;
    }

    private List<SensorValue> sensorsValues;
    private boolean sensorChanged = false;
    @Override
    public void onSensorChanged(SensorEvent event) {
        for(SensorValue sensorValue : sensorsValues) {
            if(sensorValue.SensorType == event.sensor.getType()
            && sensorValue.SensorVendor.equals(event.sensor.getVendor())
            && sensorValue.SensorVersion == event.sensor.getVersion()) {
                sensorValue.SensorValues = new ArrayList<Float>();
                for(float value : event.values) sensorValue.SensorValues.add(value);
                sensorValue.SensorChanged = true;
            }
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        resumeSensors();
    }

    protected void resumeSensors() {
        for(Sensor sensor : deviceSensors) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    protected void pauseSensors(){
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseSensors();
    }


    protected void sendSensorsInformation() {
        sensorManager.unregisterListener(this);
        try {
            String url = "http://192.168.0.10:3000/SetSensorValues";

            for(SensorValue sensor : sensorsValues) {
                if(sensor.SensorChanged == false) continue;
                sensor.SensorChanged = false;
                JSONObject data = new JSONObject();

                data.put("SensorType", sensor.SensorType);
                data.put("SensorStringType", sensor.SensorStringType);
                data.put("SensorVendor", sensor.SensorVendor);
                data.put("SensorVersion", sensor.SensorVersion);

                JSONArray values = new JSONArray(sensor.SensorValues);
                data.put("SensorValues", values);

                CallAPI callAPI = new CallAPI(url);
                callAPI.setParameters(data);
                callAPI.execute();
            }
        } catch(Exception ex) {
            System.out.println("AN ERROR OCCURRED:");
            ex.printStackTrace();
        }
    }
}
