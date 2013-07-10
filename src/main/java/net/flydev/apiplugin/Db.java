package net.flydev.apiplugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

class Db {
    private Connection connection = null;        
    private Statement statement = null;
    
    Db(String host, String database, String user, String password) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            
            /* jdbc:mysql:// +  office.kd-eurotrans.com + / + test */
            String url = "jdbc:mysql://" + host + "/" + database;
            
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected.");
            connection.close();
            System.out.println("Disconnected.");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
}
