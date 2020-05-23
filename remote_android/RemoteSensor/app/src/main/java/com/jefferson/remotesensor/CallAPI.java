package com.jefferson.remotesensor;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CallAPI implements Runnable {
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

    private static final String BYTES_THAT_WILL_BE_DISCARDED = "                                 ";

    public void run() {
        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();

            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestMethod("POST");
            con.setDoOutput(true);

            con.connect();

            String jsonData = data.toString();
            jsonData = jsonData + BYTES_THAT_WILL_BE_DISCARDED;


            OutputStream outStream = con.getOutputStream();
            outStream.write(jsonData.getBytes(), 0, jsonData.length());
            outStream.close();

            int status = con.getResponseCode();
            System.out.println("#### SUCCESS SEND INFORMATION SENDING REQUEST: " + jsonData);
            //con.disconnect();
        } catch (Exception ex) {
            System.out.println("!!!! ERROR INSIDE CallAPI " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
