package de.uniba.rz.backend.udp;

import de.uniba.rz.backend.RemoteAccess;
import de.uniba.rz.backend.TicketStore;
import de.uniba.rz.entities.Ticket;
import de.uniba.rz.entities.TicketException;
import de.uniba.rz.entities.network.ByteArrayStream;
import de.uniba.rz.entities.network.ConnectionUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpRemoteAccess implements RemoteAccess {
    String host;
    int port;

    DatagramSocket datagramSocket;

    int bufferLen = 65536;
    byte[] buffer = new byte[bufferLen];

    public UdpRemoteAccess(String host, String port) {
        this.host = host;
        this.port = Integer.parseInt(port);
        datagramSocket = ConnectionUtil.
                getDatagramSocketForServer(this.port).getDatagramSocket();
    }

    @Override
    public void prepareStartup(TicketStore ticketStore) {
        while (true) {
            try {

                DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(receivedPacket);

                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buffer));
                Ticket ticket = (Ticket) ois.readObject();

                ticketStore.storeNewTicket(ticket.getReporter(), ticket.getTopic(),
                        ticket.getDescription(), ticket.getType(), ticket.getPriority());

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ObjectOutputStream outputStream = new ObjectOutputStream(out);
                outputStream.writeObject(ticketStore.getAllTickets());
                outputStream.flush();
                datagramSocket.send(new DatagramPacket(out.toByteArray(), out.toByteArray().length,
                        receivedPacket.getAddress(), receivedPacket.getPort()));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void run() {

    }
}
