package de.uniba.rz.entities.rabbitmq;

import com.rabbitmq.client.*;
import de.uniba.rz.entities.Ticket;
import de.uniba.rz.entities.TicketException;
import de.uniba.rz.entities.network.ByteArrayStream;
import de.uniba.rz.entities.network.UdpDatagramPacket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

public class RabbitMqReceive {

    public static void main(String[] argv) throws Exception {
        ConnectionFactory connectionFactory = ConnectionUtil.
                getRabbitMqConnection(RabbitMqEntities.RABBITMQ_HOST).getConnectionFactory();
        Channel channel = connectionFactory.newConnection().createChannel();

        channel.queueDeclare(RabbitMqEntities.QUEUE_NAME, true, false, false, null);
        channel.queuePurge(RabbitMqEntities.QUEUE_NAME);

        channel.basicQos(1);

        System.out.println(" [x] Awaiting RPC requests");


        DeliverCallback deliverCallback = (consumerTag, delivery) -> {

            byte[] serverResponse = new byte[RabbitMqEntities.RABBITMQ_RESPONSE_BYTE_LEN];
            AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(delivery.getProperties().getCorrelationId())
                    .build();

            byte[] data = delivery.getBody();

            try {
                sendTicketToServer(data);
                serverResponse = receiveTicketsFromServer();
            } catch (TicketException e) {
                throw new RuntimeException(e);
            } finally {
                channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, serverResponse);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
            System.out.println("Data Received from queue");
        };
        channel.basicConsume(RabbitMqEntities.QUEUE_NAME, false, deliverCallback, (consumerTag -> {
        }));
        Thread.currentThread().join();
    }

    private static void sendTicketToServer(byte[] data) throws IOException {
        DatagramSocket datagramSocket = de.uniba.rz.entities.network.ConnectionUtil.
                getUdpConnection(RabbitMqEntities.SERVER_ADDRESS,
                        RabbitMqEntities.SERVER_PORT).getDatagramSocket();
        datagramSocket.send(UdpDatagramPacket.getNewUdpDatagramPacket(data));
    }

    private static byte[] receiveTicketsFromServer() throws TicketException {
        byte[] buffer = new byte[65536];
        DatagramPacket receivedPacket = UdpDatagramPacket.getNewUdpDatagramPacket(buffer);
        try {
            DatagramSocket datagramSocket = de.uniba.rz.entities.network.ConnectionUtil.
                    getUdpConnection(RabbitMqEntities.SERVER_ADDRESS,
                            RabbitMqEntities.SERVER_PORT).getDatagramSocket();
            datagramSocket.receive(receivedPacket);
        } catch (Exception exception) {
            throw new TicketException("Can not receive response from server in rabbit mq");
        } finally {
            return receivedPacket.getData();
        }
    }
}
