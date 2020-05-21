import java.net.*;
import java.io.*;

public class Test {
    private static boolean finished = false;
    public static void main(String[] args){
        new Test();
    }

    public Test(){
        CallAPI callapi = new CallAPI();
        Thread thread = new Thread(callapi);
        thread.start();

        try {
            while(!finished) {
                Thread.sleep(750);
                System.out.print(".");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public class CallAPI implements Runnable {
        public void run() {
            try {
                System.out.println("Starting...");
                URL url = new URL("http://192.168.0.13:3000/SetVal");
                HttpURLConnection con = (HttpURLConnection)url.openConnection();

                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestMethod("POST");
                con.setDoOutput(true);

                con.connect();
                String json = "{\"val\": 59 }";

                System.out.println("getOutputStream...");
                OutputStream outStream = con.getOutputStream();
                System.out.println("write....");
                outStream.write(json.getBytes("utf-8"), 0, json.length());
                outStream.close();
                int status = con.getResponseCode();
                System.out.println("RESPONSE CODE: " + status);
                con.disconnect();
                System.out.println("Finished");
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            finished = true;
        }
    }
}