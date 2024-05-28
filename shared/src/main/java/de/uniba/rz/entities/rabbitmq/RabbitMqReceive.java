package de.uniba.rz.entities.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import de.uniba.rz.entities.Ticket;
import de.uniba.rz.entities.network.ByteArrayStream;
import de.uniba.rz.entities.network.UdpDatagramPacket;

import java.io.IOException;
import java.net.DatagramSocket;

public class RabbitMqReceive {

    public static void main(String[] argv) throws Exception {
        Connection connection = ConnectionUtil.
                getRabbitMqConnection(RabbitMqEntities.RABBITMQ_HOST).getNewConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(RabbitMqEntities.QUEUE_NAME, true, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            byte[] data = delivery.getBody();
            sendTicketToServer(data);
            System.out.println("Data Received from queue");
        };
        channel.basicConsume(RabbitMqEntities.QUEUE_NAME, true, deliverCallback, consumerTag -> {
        });
        Thread.currentThread().join();
    }

    private static void sendTicketToServer(byte[] data) throws IOException {
        DatagramSocket datagramSocket = de.uniba.rz.entities.network.ConnectionUtil.
                getUdpConnection(RabbitMqEntities.SERVER_ADDRESS,
                        RabbitMqEntities.SERVER_PORT).getDatagramSocket();
        datagramSocket.send(UdpDatagramPacket.getNewUdpDatagramPacket(data));
    }
}
