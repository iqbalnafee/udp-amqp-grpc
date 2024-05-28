package de.uniba.rz.entities.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import de.uniba.rz.entities.Ticket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RabbitMqSend {

    public byte[] sendPacketToQueue(byte[] data) throws Exception {
        ConnectionFactory factory = ConnectionUtil.
                getRabbitMqConnection(RabbitMqEntities.RABBITMQ_HOST).getConnectionFactory();
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            System.out.println("data sent to queue");
            return call(data, channel);
        }
    }

    public byte[] call(byte[] data, Channel channel) throws IOException, ExecutionException, InterruptedException {

        final String corrId = UUID.randomUUID().toString();
        String replyQueueName = channel.queueDeclare().getQueue();
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();
        channel.basicPublish("", RabbitMqEntities.QUEUE_NAME, props, data);
        final CompletableFuture<byte[]> response = new CompletableFuture<>();
        String cTag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response.complete(delivery.getBody());
            }
        }, consumerTag -> {
        });

        byte[] bytes1 = response.get();
        channel.basicCancel(cTag);
        return bytes1;

    }
}
