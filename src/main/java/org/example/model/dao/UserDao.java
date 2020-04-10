package org.example.model.dao;

import org.example.model.DataSource;
import org.example.model.dto.User;
import org.example.interfaces.Dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao implements Dao<User> {

    public static final String SQL_SELECT_ALL = "SELECT * FROM " + User.TABLE_NAME;

    public static final String SQL_SELECT_BY_ID = SQL_SELECT_ALL + " WHERE " + User.ID_COLUMN + " = ?;";

    public static final String SQL_INSERT = "INSERT INTO " + User.TABLE_NAME + "(" +
            User.LOGIN_COLUMN + ", " + User.PASSWORD_COLUMN + ", " + User.EMAIL_COLUMN + ") VALUES(?, ?, ?);";

    public static final String SQL_UPDATE = "UPDATE " + User.TABLE_NAME + " SET " + User.LOGIN_COLUMN +
            " = ?, " + User.PASSWORD_COLUMN + " = ?, " + User.EMAIL_COLUMN + " = ? WHERE " + User.ID_COLUMN + " = ?;";

    public static final String SQL_DELETE = "DELETE FROM " + User.TABLE_NAME + " WHERE " +
            User.ID_COLUMN + " = ?;";

    private DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(User user) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT)) {
            preparedStatement.setString(1, user.getLogin());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getEmail());
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public User selectById(long id) throws SQLException {
        User user;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_SELECT_BY_ID)) {
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            user = createUser(resultSet);
        }
        return user;
    }

    @Override
    public List<User> selectAll() throws SQLException {
        List<User> users = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_SELECT_ALL)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                User user = createUser(resultSet);
                users.add(user);
            }
        }
        return users;
    }

    @Override
    public void update(User user) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_UPDATE)) {
            preparedStatement.setString(1, user.getLogin());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getEmail());
            preparedStatement.setLong(4, user.getId());
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void delete(long id) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_DELETE)) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        }
    }

    private User createUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong(User.ID_COLUMN));
        user.setLogin(resultSet.getString(User.LOGIN_COLUMN));
        user.setPassword(resultSet.getString(User.PASSWORD_COLUMN));
        user.setEmail(resultSet.getString(User.EMAIL_COLUMN));
        return user;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
