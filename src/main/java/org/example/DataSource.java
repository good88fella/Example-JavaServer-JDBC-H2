package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DataSource {

    private String url;
    private String userName;
    private String password;

    public DataSource(String url, String userName, String password) {
        this.url = url;
        this.userName = userName;
        this.password = password;
    }

    public DataSource(String pathToPropertiesFile) {
        Properties prop = new Properties();
        try (InputStream inputStream = Files.newInputStream(Paths.get(pathToPropertiesFile))) {
            prop.load(inputStream);
            url = prop.getProperty("url");
            userName = prop.getProperty("username");
            password = prop.getProperty("password");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, userName, password);
    }

    public void closeConnection(Connection connection) throws SQLException {
        if (connection == null) return;
        connection.close();
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}
