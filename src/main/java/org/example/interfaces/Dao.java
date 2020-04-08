package org.example.interfaces;

import java.util.List;

public interface Dao<K, E> {

    void insert(E entity);

    E selectById(K id);

    List<E> selectAll();

    void update(E entity);

    void delete(K id);

}
