package org.example.dao;

import org.example.DataSource;
import org.example.dto.User;
import org.h2.jdbc.JdbcSQLNonTransientException;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.RunScript;
import org.h2.tools.Server;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDaoTest {

    private static final String URL = "jdbc:h2:~/test;AUTO_SERVER=TRUE;Mode=Oracle";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "";
    private static final String SQL_SCRIPT_CREATE_TABLE = "src/main/java/org/example/sql/scripts/h2init.sql";

    private static DataSource dataSource;
    private static UserDao userDao;

    @BeforeClass
    public static void createDB() throws SQLException, FileNotFoundException, ClassNotFoundException {
        Class.forName("org.h2.Driver");
        dataSource = new DataSource(URL, USERNAME, PASSWORD);
        userDao = new UserDao(dataSource);
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            RunScript.execute(connection, new FileReader(SQL_SCRIPT_CREATE_TABLE));
            statement.execute("INSERT INTO users(login, password, email) VALUES('coolLogin', '1234', 'coolEmail@email.ru');");
            statement.execute("INSERT INTO users(login, password, email) VALUES('anotherLogin', 'qwerty', 'anotherEmail@email.ru');");
            statement.execute("INSERT INTO users(login, password, email) VALUES('thirdLogin', 'zxcvb', 'thirdEmail@email.ru');");
        }
    }

    @AfterClass
    public static void dropDB() throws SQLException {
        DeleteDbFiles.execute("~", "test", true);
    }

    @Test
    public void insert() throws SQLException {
        User expectedUser = new User("secondLogin", "secretpass", "secondEmail@email.ru");
        userDao.insert(expectedUser);
        expectedUser.setId(4L);
        User actualUser = getActualUser(4L);
        Assert.assertEquals(expectedUser, actualUser);
    }

    @Test
    public void selectById() {
        User expectedUser = userDao.selectById(1L);
        User actualUser = new User(1L,"coolLogin", "1234", "coolEmail@email.ru");
        Assert.assertEquals(expectedUser, actualUser);
    }

    @Test(expected = JdbcSQLNonTransientException.class)
    public void delete() throws SQLException {
        userDao.delete(4L);
        getActualUser(4L);
    }

    @Test
    public void selectAll() {
        User[] actual = {
        new User(1L,"coolLogin", "1234", "coolEmail@email.ru"),
        new User(2L, "anotherLogin", "qwerty", "anotherEmail@email.ru"),
        new User(3L, "thirdLogin", "zxcvb", "thirdEmail@email.ru")
        };
        User[] expected = userDao.selectAll().toArray(new User[0]);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void update() throws SQLException {
        User actualUser = new User(2L, "anotherLogin", "qwerty", "email@email.ru");
        userDao.update(actualUser);
        User expectedUser = getActualUser(2L);
        Assert.assertEquals(expectedUser, actualUser);
    }

    private User getActualUser(Long id) throws SQLException {
        User actualUser = new User();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM users WHERE user_id = " + id + ";");
            resultSet.next();
            actualUser.setId(resultSet.getLong("user_id"));
            actualUser.setLogin(resultSet.getString("login"));
            actualUser.setPassword(resultSet.getString("password"));
            actualUser.setEmail(resultSet.getString("email"));
        }
        return actualUser;
    }
}