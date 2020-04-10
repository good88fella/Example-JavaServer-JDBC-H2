package org.example.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.interfaces.Dao;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.sql.SQLException;

public class EntityController<E> implements HttpHandler {

    private Dao<E> dao;

    public EntityController(Dao<E> dao) {
        this.dao = dao;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        if ("GET".equalsIgnoreCase(httpExchange.getRequestMethod())) {
            handleGetResponse(httpExchange);
        } else if ("DELETE".equalsIgnoreCase(httpExchange.getRequestMethod())) {
            handleDeleteResponse(httpExchange);
        } else if ("POST".equalsIgnoreCase(httpExchange.getRequestMethod())) {
            handlePostResponse(httpExchange);
        } else if ("PUT".equalsIgnoreCase(httpExchange.getRequestMethod())) {
            handlePutResponse(httpExchange);
        }
    }

    private void handleGetResponse(HttpExchange httpExchange) throws IOException {
        try (OutputStream outputStream = httpExchange.getResponseBody()) {
            E entity = dao.selectById(Long.parseLong(handleGetRequest(httpExchange)));
            byte[] response = entity.toString().getBytes();
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
            outputStream.write(response);
        } catch (SQLException e) {
            notFoundResponse(httpExchange);
        }
    }

    private void handleDeleteResponse(HttpExchange httpExchange) throws IOException {
        try {
            dao.delete(Long.parseLong(handleGetRequest(httpExchange)));
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_ACCEPTED, 0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handlePostResponse(HttpExchange httpExchange) throws IOException {
        try (InputStream inputStream = httpExchange.getRequestBody()) {
            E entity = getEntityFromRequest(inputStream);
            dao.insert(entity);
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_CREATED, entity.toString().length());
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void handlePutResponse(HttpExchange httpExchange) throws IOException {
        try (InputStream inputStream = httpExchange.getRequestBody()) {
            E entity = getEntityFromRequest(inputStream);
            dao.update(entity);
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_ACCEPTED, entity.toString().length());
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public E getEntityFromRequest(InputStream inputStream) throws IOException, ClassNotFoundException {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            return  (E) objectInputStream.readObject();
        }
    }

    private void notFoundResponse(HttpExchange httpExchange) throws IOException {
        String response = "<h1>Entity not found</h1>";
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, response.length());
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private String handleGetRequest(HttpExchange httpExchange) {
        return httpExchange.
                getRequestURI()
                .toString()
                .split("\\?")[1]
                .split("=")[1];
    }

    public Dao<E> getDao() {
        return dao;
    }
}
