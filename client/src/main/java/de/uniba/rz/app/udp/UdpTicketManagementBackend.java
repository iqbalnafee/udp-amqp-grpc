package de.uniba.rz.app.udp;

import de.uniba.rz.app.TicketManagementBackend;
import de.uniba.rz.entities.Priority;
import de.uniba.rz.entities.Ticket;
import de.uniba.rz.entities.TicketException;
import de.uniba.rz.entities.Type;
import de.uniba.rz.entities.network.ByteArrayStream;
import de.uniba.rz.entities.network.ConnectionUtil;
import de.uniba.rz.entities.network.UdpDatagramPacket;
import de.uniba.rz.entities.rabbitmq.RabbitMqSend;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class UdpTicketManagementBackend implements TicketManagementBackend {

    private String host;
    private int port;

    AtomicInteger nextId;

    public UdpTicketManagementBackend() {
        nextId = new AtomicInteger(1);
    }

    public UdpTicketManagementBackend(String host, int port) {
        this.host = host;
        this.port = port;
        this.nextId = new AtomicInteger(1);
    }

    @Override
    public void triggerShutdown() {

    }

    @Override
    public Ticket createNewTicket(String reporter, String topic, String description, Type type, Priority priority)
            throws TicketException {
        Ticket newTicket = new Ticket(nextId.getAndIncrement(), reporter, topic, description, type, priority);
        try {
            //sendTicketToServer(newTicket);
            sendTicketToQueue(newTicket);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return newTicket;
    }

    private void sendTicketToQueue(Ticket ticket) throws Exception {
        byte[] data = ByteArrayStream.getByteDataFromObject(ticket);
        RabbitMqSend.sendPacketToQueue(data);
    }

    private void sendTicketToServer(Ticket ticket) throws IOException {
        DatagramSocket datagramSocket = ConnectionUtil.
                getUdpConnection(host, port).getDatagramSocket();
        byte[] data = ByteArrayStream.getByteDataFromObject(ticket);
        datagramSocket.send(UdpDatagramPacket.getNewUdpDatagramPacket(data));
    }

    @Override
    public List<Ticket> getAllTickets() throws TicketException {
        return null;
    }

    @Override
    public Ticket getTicketById(int id) throws TicketException {
        return null;
    }

    @Override
    public Ticket acceptTicket(int id) throws TicketException {
        return null;
    }

    @Override
    public Ticket rejectTicket(int id) throws TicketException {
        return null;
    }

    @Override
    public Ticket closeTicket(int id) throws TicketException {
        return null;
    }
}
