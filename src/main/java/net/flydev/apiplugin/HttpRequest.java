package net.flydev.apiplugin;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

class HttpRequest {
    /**
     * The main logger.
     */
    private static Logger logger = Logger.getLogger("Minecraft");
    
    
    private static HttpURLConnection requestGet(String path) {
        try {
            URL url = new URL(ApiPlugin.host + path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            connection.setRequestMethod("GET");

            return connection;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
    
    private static HttpURLConnection requestPost(String path, String params) {
        try {
            URL url = new URL(ApiPlugin.host + path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(params);
            wr.flush();
            wr.close();
            
            return connection;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
    
    private static String response(HttpURLConnection connection) {
        try {
            int responseCode = connection.getResponseCode();
            if (responseCode == 200 || responseCode == 201) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuffer response = new StringBuffer();

                String input;
                while ((input = in.readLine()) != null) {
                    response.append(input);
                }
                in.close();

                return response.toString();
            } else {
                logger.warning(connection.toString());
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
    
    public static String get(String path) {
        HttpURLConnection connection = requestGet(path);
        return response(connection);
    }
    
    public static String post(String path, String params) {
        HttpURLConnection connection = requestPost(path, params);
        return response(connection);        
    }

}
