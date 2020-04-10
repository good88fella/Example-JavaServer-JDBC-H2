package org.example.interfaces;

import java.sql.SQLException;
import java.util.List;

public interface Dao<E> {

    void insert(E entity) throws SQLException;

    E selectById(long id) throws SQLException;

    List<E> selectAll() throws SQLException;

    void update(E entity) throws SQLException;

    void delete(long id) throws SQLException;

}
