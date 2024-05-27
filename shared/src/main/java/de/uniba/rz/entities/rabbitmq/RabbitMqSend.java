package de.uniba.rz.entities.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class RabbitMqSend {
    private static final String QUEUE_NAME = "ticketQueue";
    private static final String RABBITMQ_HOST = "localhost";

    public static void sendPacketToQueue(byte[] data) throws Exception {
        try (Connection connection = ConnectionUtil.
                getRabbitMqConnection(RABBITMQ_HOST).getNewConnection();
             Channel channel = connection.createChannel()) {

            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, data);
            System.out.println("data sent to queue");
        }
    }
}
