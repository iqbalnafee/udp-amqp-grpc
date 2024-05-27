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

    int bufferLen = 65536;
    byte[] buf = new byte[bufferLen];

    public UdpRemoteAccess(String host, String port) {
        this.host = host;
        this.port = Integer.parseInt(port);
    }

    @Override
    public void prepareStartup(TicketStore ticketStore) {
        while (true) {
            try {

                DatagramSocket datagramSocket = ConnectionUtil.
                        getDatagramSocketForServer(port).getDatagramSocket();
                DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
                datagramSocket.receive(receivedPacket);

                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buf));
                Ticket ticket = (Ticket) ois.readObject();

                ticketStore.storeNewTicket(ticket.getReporter(), ticket.getTopic(),
                        ticket.getDescription(), ticket.getType(), ticket.getPriority());

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ObjectOutputStream outputStream = new ObjectOutputStream(out);
                outputStream.writeObject(ticketStore.getAllTickets());
                outputStream.flush();
                //byte[] allTickets = ByteArrayStream.getByteDataFromObject(ticketStore.getAllTickets());
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
