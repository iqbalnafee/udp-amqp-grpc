package de.uniba.rz.entities.rabbitmq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class ConnectionUtil {
    private static ConnectionUtil rabbitMqConnection;
    ConnectionFactory connectionFactory;
    Connection connection;

    private ConnectionUtil(String host) {
        try {
            connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(host);
            connection = connectionFactory.newConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized ConnectionUtil getRabbitMqConnection(String host) {
        if (rabbitMqConnection == null) rabbitMqConnection = new ConnectionUtil(host);
        return rabbitMqConnection;
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }
    public Connection getNewConnection() {
        return connection;
    }
}
