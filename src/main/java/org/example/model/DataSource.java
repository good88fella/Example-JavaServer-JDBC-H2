package org.example.model;

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
    private String login;
    private String password;

    public DataSource(String url, String login, String password) {
        this.url = url;
        this.login = login;
        this.password = password;
    }

    public DataSource(String pathToPropertiesFile) {
        Properties prop = new Properties();
        try (InputStream inputStream = Files.newInputStream(Paths.get(pathToPropertiesFile))) {
            prop.load(inputStream);
            url = prop.getProperty("url");
            login = prop.getProperty("username");
            password = prop.getProperty("password");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, login, password);
    }

    public void closeConnection(Connection connection) throws SQLException {
        if (connection == null) return;
        connection.close();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
