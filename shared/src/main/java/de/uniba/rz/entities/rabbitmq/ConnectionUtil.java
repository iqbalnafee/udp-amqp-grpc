package de.uniba.rz.entities.rabbitmq;

import com.rabbitmq.client.ConnectionFactory;

public class ConnectionUtil {
    private static ConnectionUtil rabbitMqConnection;
    ConnectionFactory connectionFactory;

    private ConnectionUtil(String host) {
        try {
            connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(host);
            connectionFactory.setAutomaticRecoveryEnabled(true);
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
}
