package org.example;

import org.example.interfaces.Dao;
import org.example.interfaces.Service;
import org.example.model.DataSource;
import org.example.model.dao.UserDao;
import org.example.model.dto.User;
import org.example.service.EntityManagerService;
import org.h2.tools.RunScript;

import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

public class Application {

    public static final String JDBC_H2_DRIVER = "org.h2.Driver";
    public static final String H2_PROPERTIES = "src/main/resources/h2database.properties";
    public static final String HOST = "localhost";
    public static final int PORT = 8080;
    public static String SQL_INIT = "scripts/init.sql";

    public static void main(String[] args) {
        try {
            Class.forName(JDBC_H2_DRIVER);
            DataSource dataSource = new DataSource(H2_PROPERTIES);
            RunScript.execute(dataSource.getConnection(), new FileReader(SQL_INIT));
            Service service = new EntityManagerService(new UserDao(dataSource), HOST, PORT);
            service.start();
            System.out.println("Service started!");
        } catch (IOException | SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
