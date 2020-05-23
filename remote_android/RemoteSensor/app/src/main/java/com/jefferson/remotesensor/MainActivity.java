package com.jefferson.remotesensor;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener {
    private TextView txtSample;
    private Thread sendToApiThread;
    protected static boolean SensorSendToApiThread_ENABLED = false;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLocale();

        setContentView(R.layout.activity_main);

        doLanguageSpinnerSetup();

        doSensorsSetup();
        doLocationSetup();

        sendToApiThread = new Thread(new SensorSendToApiThread(this));
        sendToApiThread.start();

        final Button btnHabilitarEnvio = findViewById(R.id.btn_request);
        btnHabilitarEnvio.setTextColor(Color.GREEN);
        btnHabilitarEnvio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /// not used anymore.
                SensorSendToApiThread_ENABLED = !SensorSendToApiThread_ENABLED;
                if (SensorSendToApiThread_ENABLED) {
                    if(sendToApiThread.isAlive() == false || sendToApiThread.isInterrupted() == true) {
                        sendToApiThread.start();
                    }
                    btnHabilitarEnvio.setText(getResources().getText(R.string.txt_desabilitar));
                    btnHabilitarEnvio.setTextColor(Color.RED);
                } else {
                    sendToApiThread.interrupt();
                    btnHabilitarEnvio.setText(getResources().getText(R.string.txt_habilitar));
                    btnHabilitarEnvio.setTextColor(Color.GREEN);
                }
            }
        });

        final Button btnSample = findViewById(R.id.btn_sample);
        btnSample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSensorsInformation();
            }
        });
    }

    private static final String[] LANGUAGES =  new String[] {"Português", "English", "日本語"};

    private static String selectedLanguage = "pt";
    protected Spinner spinnerLanguage;

    public void doLanguageSpinnerSetup() {
        spinnerLanguage = (Spinner)findViewById(R.id.spinner_language);

        final ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, LANGUAGES);

        spinnerLanguage.setAdapter(adapter);
        int lang = -1;
        switch(selectedLanguage) {
            case "pt":
                lang = 0;
                break;
            case "en":
                lang = 1;
                break;
            case "jp":
                lang = 2;
                break;

        }
        spinnerLanguage.setSelection(lang);

        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                int lang = -1;
                switch(selectedLanguage) {
                    case "pt":
                        lang = 0;
                        break;
                    case "en":
                        lang = 1;
                        break;
                    case "jp":
                        lang = 2;
                        break;

                }

                spinnerLanguage.setSelection(lang);
            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String language = LANGUAGES[position];

                String newLang = "";
                switch(language) {
                    case "Português":
                        newLang = "pt";
                        break;
                    case "English":
                        newLang = "en";
                        break;
                    case "日本語":
                        newLang = "jp";
                        break;
                }

                if(newLang.equals(selectedLanguage) == false) {
                    selectedLanguage = newLang;
                    Intent intent = getIntent();
                    overridePendingTransition(0,0);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    finish();
                    overridePendingTransition(0,0);
                    startActivity(intent);
//                    spinnerLanguage.setAdapter(adapter);
//                    setLocale();
//                    setContentView(R.layout.activity_main);
//                    MainActivity.this.doLanguageSpinnerSetup();
                }
            }
        });
    }

    public static boolean LOCATION_SERVICE_ENABLED = false;

    class SensorSendToApiThread implements Runnable {
        private MainActivity main;

        public SensorSendToApiThread(MainActivity main) {
            SensorSendToApiThread.this.main = main;
        }

        public void run() {
            try {
                while (true) {
                    try {
                        Thread.sleep(500);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    if (MainActivity.this.locationAccessGranted && MainActivity.LOCATION_SERVICE_ENABLED) {
                        System.out.println("Updating location...");
                        updateLocation();
                    } else {
                        System.out.println("Unable to update location due to>" +
                                "location access: " + (MainActivity.this.locationAccessGranted ? "true" : "false") +
                                "service enabled: " + (MainActivity.LOCATION_SERVICE_ENABLED ? "true" : "false"));
                    }
                    if (MainActivity.SensorSendToApiThread_ENABLED)
                        SensorSendToApiThread.this.main.sendSensorsInformation();
                }
            } catch (Exception ex) {
                System.out.println("ERROR IN UPDATE THREAD");
                ex.printStackTrace();
            }
        }
    }

    protected void updateLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.ACCESS_FINE_LOCATION  },
                        MY_PERMISSION_ACCCESS_FINE_LOCATION );

                return;
            }

            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            locationSensor.SensorValues = new ArrayList<>();

            updateLocation((float)location.getLatitude(), (float)location.getLongitude());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private LocationManager locationManager;
    SensorValue locationSensor;
    public static final int MY_PERMISSION_ACCCESS_FINE_LOCATION = 500;
    protected void doLocationSetup() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.ACCESS_FINE_LOCATION  },
                    MY_PERMISSION_ACCCESS_FINE_LOCATION );

            return;
        }

        locationAccessGranted = true;

        LOCATION_SERVICE_ENABLED = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);;

        if(LOCATION_SERVICE_ENABLED == false) {
            Toast.makeText(this, getResources().getText(R.string.txt_habilitar_localizacao), Toast.LENGTH_LONG)
                    .show();
        }

        for(int i=0;i<sensorsValues.size();i++) {
            if(sensorsValues.get(i).SensorType == -1) {
                sensorsValues.remove(i);
            }
        }

        locationSensor = new SensorValue();
        locationSensor.SensorStringType = "GPS_PROVIDER";
        locationSensor.SensorType = -1;
        locationSensor.SensorVendor = "DEFAULT";
        locationSensor.SensorVersion = -1;
        locationSensor.SensorValues = new ArrayList<String>();
        sensorsValues.add(locationSensor);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
    }

    protected boolean locationAccessGranted = false;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case MY_PERMISSION_ACCCESS_FINE_LOCATION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationAccessGranted = true;
                    LOCATION_SERVICE_ENABLED = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    doLocationSetup();
                } else {
                    locationAccessGranted = false;
                    Toast.makeText(this, getResources().getText(R.string.txt_permitir_localizacao), Toast.LENGTH_LONG)
                            .show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) { LOCATION_SERVICE_ENABLED = true; }

    @Override
    public void onProviderDisabled(String provider) { LOCATION_SERVICE_ENABLED = false; }

    public void updateLocation(float latitude, float longitude){

        if(locationSensor != null) {
            locationSensor.SensorValues = new ArrayList<String>();

            Geocoder geocoder;
            List<Address> addresses = null;
            geocoder = new Geocoder(this, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                System.out.println("!!!! ERROR ON getFromLocation");
                e.printStackTrace();
            }

            if(addresses != null) {
                String address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();


                locationSensor.SensorValues.add(address == null ? "" : address);
                locationSensor.SensorValues.add(city == null ? "" : city);
                locationSensor.SensorValues.add(state == null ? "" : state);
                locationSensor.SensorValues.add(country == null ? "" : country);
                locationSensor.SensorValues.add(postalCode == null ? "" : postalCode);
                locationSensor.SensorValues.add(knownName == null ? "" : knownName);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        float latitude = (float)location.getLatitude();
        float longitude = (float)location.getLongitude();

        updateLocation(latitude, longitude);
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
                sensorValue.SensorValues = new ArrayList<String>();
                sensorValue.SensorVendor = sensor.getVendor();
                sensorValue.SensorVersion = sensor.getVersion();
                sensorValue.SensorChanged = true;

                sensorsValues.add(sensorValue);
//                txtSample.getEditableText().clear();
//                String newStr = "Sensors: " + sensorsValues.size();
            } // else sensor não existe.
        }

        resumeSensors();

        updateQtdSensor();
    }

    public void updateQtdSensor() {
        txtSample = (TextView)findViewById(R.id.txt_sample_vw);
        String qtdSensorsStr = getResources().getText(R.string.qtd_sensors_str).toString();
        txtSample.setText(qtdSensorsStr + " " + sensorsValues.size());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do something
    }

    public class SensorValue {
        public int SensorType;
        public String SensorStringType;
        public List<String> SensorValues;
        public String SensorVendor;
        public int SensorVersion;
        public boolean SensorChanged;
    }

    private List<SensorValue> sensorsValues;
    private boolean sensorChanged = false;
    @Override
    public void onSensorChanged(SensorEvent event) {
        for(SensorValue sensorValue : sensorsValues) {
            if(0==0
                && sensorValue.SensorType == event.sensor.getType()
                //&& sensorValue.SensorVendor.equals(event.sensor.getVendor())
                //&& sensorValue.SensorVersion == event.sensor.getVersion()
            ) {
                sensorValue.SensorValues.clear();
                for(float value : event.values) sensorValue.SensorValues.add(value+"");
                sensorValue.SensorChanged = true;
                JSONArray arr = new JSONArray(sensorValue.SensorValues);
                String strValues = arr.toString();
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


    private static final String SERVER_URL = "https://remotesensorjeffraf.azurewebsites.net";

    private String getSetSensorsValuesURL() {
        return SERVER_URL + "/SetSensorsValues";
    }

    protected void sendSensorsInformation() {
        //sensorManager.unregisterListener(this);
        try {
            List<JSONObject> listSensorJSON = new ArrayList<>();

            for(SensorValue sensor : sensorsValues) {
                JSONObject sensorJsonObject = new JSONObject();

                sensorJsonObject.put("Type", sensor.SensorType);
                sensorJsonObject.put("StringType", sensor.SensorStringType);
                sensorJsonObject.put("Vendor", sensor.SensorVendor);
                sensorJsonObject.put("Version", sensor.SensorVersion);

                JSONArray values = new JSONArray(sensor.SensorValues);
                sensorJsonObject.put("Values", values);
                listSensorJSON.add(sensorJsonObject);
            }

            JSONObject data = new JSONObject();
            data.put("Sensors", new JSONArray(listSensorJSON));

            String url = getSetSensorsValuesURL();
            CallAPI callAPI = new CallAPI(url);
            callAPI.setParameters(data);

            new Thread(callAPI).start();

            System.out.println("Call Api passed execute");
        } catch(Exception ex) {
            System.out.println("AN ERROR OCCURRED TRYING TO SEND INFORMATION TO THE SERVER:");
            ex.printStackTrace();
        }
    }

    public void setLocale() {
        Locale locale;
        //Sessions session = new Sessions(this);
        //Log.e("Lan",session.getLanguage());
        locale = new Locale(selectedLanguage);
        Configuration config = new Configuration(getResources().getConfiguration());
        Locale.setDefault(locale);
        config.setLocale(locale);

        this.getBaseContext().getResources().updateConfiguration(config,
                this.getBaseContext().getResources().getDisplayMetrics());
    }
}
