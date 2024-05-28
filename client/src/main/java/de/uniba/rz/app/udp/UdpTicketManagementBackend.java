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
    }

    @Override
    public void triggerShutdown() {

    }

    @Override
    public Ticket createNewTicket(String reporter, String topic, String description, Type type, Priority priority)
            throws TicketException {
        Ticket newTicket = new Ticket(nextId.getAndIncrement(), reporter, topic, description, type, priority);
        try {
            if(!isRabbitMqActive){
                sendTicketToServer(newTicket);
                receiveTicketsFromServer();
            }
            else sendTicketToQueue(newTicket);
        } catch (Exception exception) {
            throw new TicketException("Some error occurred. Reason: "+ exception.getMessage());
        }
        return (Ticket) newTicket.clone();
    }
    private void receiveTicketsFromServer() throws TicketException {
        byte[] buffer = new byte[65536];
        try {
            List<byte[]> packetList = new ArrayList<>();
            DatagramPacket receivedPacket = UdpDatagramPacket.getNewUdpDatagramPacket(buffer);
            while (true) {
                datagramSocket.receive(receivedPacket);
                // Check if the packet indicates end of transmission
                ByteArrayInputStream byteStreamIn = new ByteArrayInputStream(receivedPacket.getData(),
                        0, receivedPacket.getLength());
                ObjectInputStream objStreamIn = new ObjectInputStream(byteStreamIn);
                String signal;
                try {
                    signal = (String) objStreamIn.readObject();
                } catch (Exception e) {
                    signal = null;  // Not an end signal
                }
                if ("END".equals(signal)) {
                    break;
                } else {
                    packetList.add(receivedPacket.getData().clone());
                    if (receivedPacket.getLength() < 65536) {
                        break;  // Last packet received
                    }
                }
            }
            // Combine all received packets into a single byte array
            ByteArrayOutputStream byteStreamOut = new ByteArrayOutputStream();
            for (byte[] packet : packetList) {
                byteStreamOut.write(packet);
                System.out.println("packet length"+packet.length);
            }
            // Deserialize the combined data
            ByteArrayInputStream combinedByteStreamIn = new ByteArrayInputStream(byteStreamOut.toByteArray());
            ObjectInputStream combinedObjStreamIn = new ObjectInputStream(combinedByteStreamIn);
            tickets = new ArrayList<>();
            tickets.addAll((ArrayList<Ticket>) combinedObjStreamIn.readObject());
            System.out.println("Ticket list size"+tickets.size());
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new TicketException("Can not receive response from server");
        }
    }

    private void sendTicketToQueue(Ticket ticket) throws Exception {
        byte[] data = ByteArrayStream.getByteDataFromObject(ticket);
        RabbitMqSend.sendPacketToQueue(data);
    }

    private void sendTicketToServer(Ticket ticket) throws IOException {
        byte[] data = ByteArrayStream.getByteDataFromObject(ticket);
        datagramSocket.send(UdpDatagramPacket.getNewUdpDatagramPacket(data));
    }

    @Override
    public List<Ticket> getAllTickets() throws TicketException {
        try{
            sendTicketToServer(new Ticket());
            receiveTicketsFromServer();
        }
        catch(Exception ignored){}
        return tickets;
    }

    @Override
    public Ticket getTicketById(int id) throws TicketException {
        return tickets.stream().filter(ticket -> ticket.getId() == id).findFirst().get();
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
