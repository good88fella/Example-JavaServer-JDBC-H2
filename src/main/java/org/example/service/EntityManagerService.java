package org.example.service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.example.controller.EntityController;
import org.example.interfaces.Dao;
import org.example.interfaces.Dto;
import org.example.interfaces.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.sql.SQLException;

public class EntityManagerService implements Service {

    private HttpServer server;
    private Dao<? extends Dto> dao;
    private int port;
    private String host;

    public EntityManagerService(Dao<? extends Dto> dao, String host, int port) throws IOException {
        this.dao = dao;
        this.port = port;
        this.host = host;
        server = HttpServer.create(new InetSocketAddress(host, port), 0);
        server.setExecutor(null);
        server.createContext("/entity/user", new EntityController<>(dao));
        server.createContext("/entity/users", new EntitiesHandler());
    }

    @Override
    public void start() {
        server.start();
    }

    @Override
    public void stop() {
        server.stop(0);
    }

    public HttpServer getServer() {
        return server;
    }

    public Dao<? extends Dto> getDao() {
        return dao;
    }

    public void setDao(Dao<Dto> dao) {
        this.dao = dao;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }


    private class EntitiesHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            try (OutputStream outputStream = httpExchange.getResponseBody()) {
                StringBuilder stringBuilder = new StringBuilder();
                for (Dto dto : dao.selectAll())
                    stringBuilder.append(dto.toString()).append("\n");
                byte[] response = stringBuilder.toString().getBytes();
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                outputStream.write(response);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
