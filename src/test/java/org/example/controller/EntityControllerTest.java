package org.example.controller;

import org.example.interfaces.Service;
import org.example.model.DataSource;
import org.example.model.dao.UserDao;
import org.example.model.dto.User;
import org.example.service.EntityManagerService;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.RunScript;
import org.junit.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class EntityControllerTest {

    private static final String SQL_INIT_TEST = "src/test/java/testscripts/init_test.sql";
    private static final String URL = "http://localhost:8080/entity/user";
    private static final String DATA_SOURCE_URL = "jdbc:h2:~/test;AUTO_SERVER=TRUE;Mode=Oracle";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "";

    private static Service service = null;
    private static DataSource dataSource = null;

    static {
        try {
            dataSource = new DataSource(DATA_SOURCE_URL, USERNAME, PASSWORD);
            service = new EntityManagerService(new UserDao(dataSource), "localhost", 8080);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @BeforeClass
    public static void start() throws ClassNotFoundException, FileNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        service.start();
    }

    @AfterClass
    public static void stop() {
        DeleteDbFiles.execute("~", "test", true);
        service.stop();
    }

    @Before
    public void createTable() throws SQLException, FileNotFoundException {
        RunScript.execute(dataSource.getConnection(), new FileReader(SQL_INIT_TEST));
    }

    @After
    public void dropTable() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP TABLE users;");
        }
    }

    @Test
    public void handleGetAll() throws IOException {
        URL url = new URL(URL + "s");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        Assert.assertEquals(responseCode, HttpURLConnection.HTTP_OK);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream()) {
                byte[] actual = new byte[inputStream.available()];
                inputStream.read(actual);
                String string = new User(1, "coolLogin", "1234", "coolEmail@email.ru").toString() + "\n" +
                        new User(2, "anotherLogin", "qwerty", "anotherEmail@email.ru").toString() + "\n" +
                        new User(3, "thirdLogin", "zxcvb", "thirdEmail@email.ru").toString() + "\n";
                byte[] expected = string.getBytes();
                Assert.assertArrayEquals(expected, actual);
            }
        }
    }

    @Test
    public void handleGetResponse() throws IOException {
        int id = 1;
        URL url = new URL(URL + "?id=" + id);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        Assert.assertEquals(responseCode, HttpURLConnection.HTTP_OK);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream()) {
                byte[] actual = new byte[inputStream.available()];
                inputStream.read(actual);
                User expectedUser = new User(id, "coolLogin", "1234", "coolEmail@email.ru");
                byte[] expected = expectedUser.toString().getBytes();
                Assert.assertArrayEquals(expected, actual);
            }
        }
    }

    @Test
    public void getEntityNotFound() throws IOException {
        int id = 10;
        URL url = new URL(URL + "?id=" + id);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        Assert.assertEquals(responseCode, HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void handleDeleteResponse() throws IOException, SQLException {
        int id = 3;
        URL url = new URL(URL + "?id=" + id);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");

        int responseCode = connection.getResponseCode();
        Assert.assertEquals(responseCode, HttpURLConnection.HTTP_ACCEPTED);

        if (responseCode == HttpURLConnection.HTTP_ACCEPTED) {
            try (Connection conn = dataSource.getConnection();
                 Statement statement = conn.createStatement()) {
                ResultSet resultSet = statement.executeQuery("SELECT * FROM users WHERE user_id = " + id + ";");
                Assert.assertFalse(resultSet.next());
            }
        }
    }

    @Test
    public void handlePostResponse() throws IOException, SQLException {
        int id = 4;
        URL url = new URL(URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        User expected = new User("addeduser", "111", "email");
        try (ObjectOutputStream outputStream = new ObjectOutputStream(connection.getOutputStream())) {
            outputStream.writeObject(expected);
        }
        expected.setId(id);

        int responseCode = connection.getResponseCode();
        Assert.assertEquals(responseCode, HttpURLConnection.HTTP_CREATED);

        if (responseCode == HttpURLConnection.HTTP_CREATED) {
            try (Connection conn = dataSource.getConnection();
                 Statement statement = conn.createStatement()) {
                ResultSet resultSet = statement.executeQuery("SELECT * FROM users WHERE user_id = " + id +";");
                resultSet.next();
                User actual = new User();
                actual.setId(resultSet.getLong("user_id"));
                actual.setLogin(resultSet.getString("login"));
                actual.setPassword(resultSet.getString("password"));
                actual.setEmail(resultSet.getString("email"));
                Assert.assertEquals(expected, actual);
            }
        }
    }

    @Test
    public void handlePutResponse() throws IOException, SQLException {
        int id = 2;
        URL url = new URL(URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setDoOutput(true);

        User expected = new User(id,"updateLogin", "qwerty", "anotherEmail@email.ru");
        try (ObjectOutputStream outputStream = new ObjectOutputStream(connection.getOutputStream())) {
            outputStream.writeObject(expected);
        }

        int responseCode = connection.getResponseCode();
        Assert.assertEquals(responseCode, HttpURLConnection.HTTP_ACCEPTED);

        if (responseCode == HttpURLConnection.HTTP_ACCEPTED) {
            try (Connection conn = dataSource.getConnection();
                 Statement statement = conn.createStatement()) {
                ResultSet resultSet = statement.executeQuery("SELECT * FROM users WHERE user_id = " + id + ";");
                resultSet.next();
                User actual = new User();
                actual.setId(resultSet.getLong("user_id"));
                actual.setLogin(resultSet.getString("login"));
                actual.setPassword(resultSet.getString("password"));
                actual.setEmail(resultSet.getString("email"));
                Assert.assertEquals(expected, actual);
            }
        }
    }
}