package org.example;

import java.sql.Connection;

public class Main {

    public static final String JDBC_H2_DRIVER = "org.h2.Driver";
    public static final String H2_PROPERTIES = "src/main/resources/h2database.properties";

    public static void main(String[] args) throws Exception {
        Class.forName(JDBC_H2_DRIVER);
        DataSource dataSource = new DataSource(H2_PROPERTIES);
        Connection connection = dataSource.getConnection();

        dataSource.closeConnection(connection);
    }
}
