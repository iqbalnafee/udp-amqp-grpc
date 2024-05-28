package de.uniba.rz.app.udp;

import de.uniba.rz.app.TicketManagementBackend;
import de.uniba.rz.entities.*;
import de.uniba.rz.entities.network.ByteArrayStream;
import de.uniba.rz.entities.network.ConnectionUtil;
import de.uniba.rz.entities.network.UdpDatagramPacket;
import de.uniba.rz.entities.rabbitmq.RabbitMqSend;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class UdpTicketManagementBackend implements TicketManagementBackend {

    private String host;
    private int port;
    boolean isRabbitMqActive;

    AtomicInteger nextId;

    List<Ticket> tickets;

    DatagramSocket datagramSocket;
    RabbitMqSend rabbitMqSend;

    public UdpTicketManagementBackend() {
        nextId = new AtomicInteger(1);
    }

    public UdpTicketManagementBackend(String host, int port, boolean isRabbitMqActive) {
        this.host = host;
        this.port = port;
        this.isRabbitMqActive = isRabbitMqActive;
        this.nextId = new AtomicInteger(1);
        this.tickets = new ArrayList<>();
        datagramSocket = ConnectionUtil.
                getUdpConnection(host, port).getDatagramSocket();
        rabbitMqSend = new RabbitMqSend();
    }

    @Override
    public void triggerShutdown() {

    }

    @Override
    public Ticket createNewTicket(String reporter, String topic, String description, Type type, Priority priority)
            throws TicketException {

        nextId.getAndIncrement();
        Ticket newTicket = new Ticket(tickets.size()+1, reporter, topic, description, type, priority);
        try {
            if (!isRabbitMqActive) {
                sendTicketToServer(newTicket);
                receiveTicketsFromServer();
            } else sendTicketToQueue(newTicket);
        } catch (Exception exception) {
            throw new TicketException("Some error occurred. Reason: " + exception.getMessage());
        }
        return (Ticket) newTicket.clone();
    }

    private void receiveTicketsFromServer() throws TicketException {
        byte[] buffer = new byte[65536];
        try {
            DatagramPacket receivedPacket = UdpDatagramPacket.getNewUdpDatagramPacket(buffer);
            datagramSocket.receive(receivedPacket);
            addDataInListFromServer(receivedPacket.getData());

        } catch (Exception exception) {
            throw new TicketException("Can not receive response from server");
        }
    }

    public void addDataInListFromServer(byte[] data) throws TicketException {
        try {
            ByteArrayInputStream byteStreamIn = new ByteArrayInputStream(data,
                    0, data.length);
            ObjectInputStream objStreamIn = new ObjectInputStream(byteStreamIn);
            tickets = new ArrayList<>();
            tickets.addAll((ArrayList<Ticket>) objStreamIn.readObject());
        } catch (Exception exception) {
            throw new TicketException("Can not receive response from server");
        }
    }



    private void sendTicketToServer(Ticket ticket) throws IOException {
        byte[] data = ByteArrayStream.getByteDataFromObject(ticket);
        datagramSocket.send(UdpDatagramPacket.getNewUdpDatagramPacket(data));
    }
    private void sendTicketToQueue(Ticket ticket) throws Exception {
        byte[] data = ByteArrayStream.getByteDataFromObject(ticket);
        data = rabbitMqSend.sendPacketToQueue(data);
        addDataInListFromServer(data);
    }
    @Override
    public List<Ticket> getAllTickets() throws TicketException {
        try {
            if (!isRabbitMqActive) {
                sendTicketToServer(new Ticket());
                receiveTicketsFromServer();
            } else {
                //sendTicketToQueue(new Ticket());
            }
        } catch (Exception exception) {
        }
        return tickets;
    }

    @Override
    public Ticket getTicketById(int id) {
        return tickets.stream().filter(t -> t.getId() == id).findFirst().get();
    }

    @Override
    public Ticket acceptTicket(int id) throws TicketException {
        Ticket ticketToModify = changeTicketStatus(id, Status.ACCEPTED);
        return (Ticket) ticketToModify.clone();
    }


    @Override
    public Ticket rejectTicket(int id) throws TicketException {
        Ticket ticketToModify = changeTicketStatus(id, Status.REJECTED);
        return (Ticket) ticketToModify.clone();
    }

    @Override
    public Ticket closeTicket(int id) throws TicketException {
        Ticket ticketToModify = changeTicketStatus(id, Status.CLOSED);
        return (Ticket) ticketToModify.clone();
    }

    @Override
    public Ticket changeTicketStatus(int id, Status status) throws TicketException {
        Ticket ticketToModify = getTicketById(id);
        if ((status != Status.CLOSED && ticketToModify.getStatus() != Status.NEW) ||
                (status == Status.CLOSED && ticketToModify.getStatus() != Status.ACCEPTED)) {
            throw new TicketException(
                    "Can not " + status + " Ticket as it is currently in status " + ticketToModify.getStatus());
        }
        ticketToModify.setStatus(status);
        try {
            sendTicketToServer(ticketToModify);
            receiveTicketsFromServer();
        } catch (Exception e) {
            throw new TicketException("can not send modified ticket to server");
        }
        return (Ticket) ticketToModify.clone();
    }
}
