package de.uniba.rz.entities.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMqSend {

    public static void sendPacketToQueue(byte[] data) throws Exception {
        ConnectionFactory factory =  ConnectionUtil.
                getRabbitMqConnection(RabbitMqEntities.RABBITMQ_HOST).getConnectionFactory();
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.queueDeclare(RabbitMqEntities.QUEUE_NAME, true, false, false, null);
            channel.basicPublish("", RabbitMqEntities.QUEUE_NAME, null, data);
            System.out.println("data sent to queue");
        }
    }
}
