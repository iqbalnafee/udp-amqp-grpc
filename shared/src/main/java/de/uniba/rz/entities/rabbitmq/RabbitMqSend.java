package de.uniba.rz.entities.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class RabbitMqSend {

    public static void sendPacketToQueue(byte[] data) throws Exception {
        try (Connection connection = ConnectionUtil.
                getRabbitMqConnection(RabbitMqEntities.RABBITMQ_HOST).getNewConnection();
             Channel channel = connection.createChannel()) {

            channel.queueDeclare(RabbitMqEntities.QUEUE_NAME, true, false, false, null);
            channel.basicPublish("", RabbitMqEntities.QUEUE_NAME, null, data);
            System.out.println("data sent to queue");
        }
    }
}
