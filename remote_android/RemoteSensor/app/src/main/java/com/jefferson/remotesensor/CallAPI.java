package com.jefferson.remotesensor;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class CallAPI extends AsyncTask<String, String, String> {
    private String urlString;
    private JSONObject data;
    public CallAPI(String urlString) {
        // context vars
        this.urlString = urlString;
        this.data = new JSONObject();
    }

    public void setParameters(JSONObject data) {
        this.data = data;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();

            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestMethod("POST");
            con.setDoOutput(true);

            con.connect();

            String jsonData = data.toString();

            OutputStream outStream = con.getOutputStream();
            outStream.write(jsonData.getBytes(), 0, jsonData.length());
            outStream.close();

            int status = con.getResponseCode();
            con.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    protected String doInBackground2(String... params) {
        HttpURLConnection urlConnection = null;

        final String USER_AGENT = "Mozilla/5.0 (Linux; U; Android 1.6; en-us; GenericAndroidDevice) AppleWebKit/528.5+ (KHTML, like Gecko) Version/3.1.2 Mobile Safari/525.20.1";
        try {
            System.out.println("Starting request.");

            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection)url.openConnection();
            //urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("User-Agent", USER_AGENT);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestMethod("POST");

            //urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            OutputStream outStream = urlConnection.getOutputStream();
            BufferedOutputStream out = new BufferedOutputStream(outStream);

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.write(data.toString());
            writer.flush();
            writer.close();
            out.close();

            System.out.println("output stream closed");

            urlConnection.connect();

            System.out.println("Connection to url ok");
        } catch(Exception ex) {
            System.out.println("AN ERROR OCCURRED:");
            ex.printStackTrace();
        } finally {
            if(urlConnection != null) urlConnection.disconnect();
        }
        return null;
    }
}
