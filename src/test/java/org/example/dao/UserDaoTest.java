package org.example.dao;

import org.example.model.DataSource;
import org.example.model.dto.User;
import org.example.model.dao.UserDao;
import org.h2.jdbc.JdbcSQLNonTransientException;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.RunScript;
import org.junit.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.*;

public class UserDaoTest {

    private static final String URL = "jdbc:h2:~/test;AUTO_SERVER=TRUE;Mode=Oracle";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "";
    private static final String SQL_INIT_TEST = "src/test/java/testscripts/init_test.sql";

    private static DataSource dataSource;
    private static UserDao userDao;

    @BeforeClass
    public static void createSources() throws ClassNotFoundException {
        Class.forName("org.h2.Driver");
        dataSource = new DataSource(URL, USERNAME, PASSWORD);
        userDao = new UserDao(dataSource);
    }

    @AfterClass
    public static void dropDB() throws SQLException {
        DeleteDbFiles.execute("~", "test", true);
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
    public void insert() throws SQLException {
        User expectedUser = new User("secondLogin", "secretpass", "secondEmail@email.ru");
        userDao.insert(expectedUser);
        expectedUser.setId(4L);
        User actualUser = getActualUser(4L);
        Assert.assertEquals(expectedUser, actualUser);
    }

    @Test
    public void selectById() throws SQLException {
        User expectedUser = userDao.selectById(1L);
        User actualUser = new User(1L,"coolLogin", "1234", "coolEmail@email.ru");
        Assert.assertEquals(expectedUser, actualUser);
    }

    @Test(expected = JdbcSQLNonTransientException.class)
    public void delete() throws SQLException {
        userDao.delete(3L);
        getActualUser(3L);
    }

    @Test
    public void selectAll() throws SQLException {
        User[] actual = {
        new User(1,"coolLogin", "1234", "coolEmail@email.ru"),
        new User(2, "anotherLogin", "qwerty", "anotherEmail@email.ru"),
        new User(3, "thirdLogin", "zxcvb", "thirdEmail@email.ru")
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