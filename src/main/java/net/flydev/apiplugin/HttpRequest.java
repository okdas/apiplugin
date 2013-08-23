package net.flydev.apiplugin;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;



class HttpRequest {
    /**
     * Главный логер
     */
    private static Logger logger = Logger.getLogger("Minecraft");
    
    
    
    /**
     * Выполняем GET запрос.
     * 
     * @param path URL запроса.
     * @return Возвращаем строку ответ
     */
    public static String get(String path) {
        HttpURLConnection connection = requestGet(path);
        return response(connection);
    }
    
    
    /**
     * Выполняем POST запрос.
     * 
     * @param path URL запроса.
     * @param params Параметры передаваемые вместе с POST.
     * @return Возвращаем строку ответ
     */
    public static String post(String path, String params) {
        HttpURLConnection connection = requestPost(path, params);
        return response(connection);        
    }
    
    
    

    /**
     * GET запрос на заданный URL.
     * 
     * @param path URL для запроса
     * @return Возвращаем {@link HttpURLConnection}
     */
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
    
        
    /**
     * POST запрос на заданный URL.
     * 
     * @param path URL для запроса
     * @param params Параметры передаваемые с POST
     * @return Возвращаем {@link HttpURLConnection}
     */
    private static HttpURLConnection requestPost(String path, String params) {
        try {
            URL url = new URL(ApiPlugin.host + path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
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
    
    
    /**
     * Обработка ответов с любого метода.
     * 
     * @param connection {@link HttpURLConnection} возвращаемое либо {@link HttpRequest#requestGet(String)},
     * либо {@link HttpRequest#requestPost(String, String)}
     * @return Возвращаем строку присланную в ответ на запрос 
     */
    private static String response(HttpURLConnection connection) {
        try {
            int responseCode = connection.getResponseCode();
            if (responseCode == 200 || responseCode == 201) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();

                String input;
                while ((input = in.readLine()) != null) {
                    response.append(input);
                }
                in.close();

                return response.toString();
            } else {
                logger.warning(responseCode + " " + connection.toString());
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
