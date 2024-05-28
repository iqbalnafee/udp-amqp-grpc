package de.uniba.rz.backend.udp;

import de.uniba.rz.backend.RemoteAccess;
import de.uniba.rz.backend.TicketStore;
import de.uniba.rz.entities.Ticket;
import de.uniba.rz.entities.network.ConnectionUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Optional;
import java.util.List;

public class UdpRemoteAccess implements RemoteAccess {
    String host;
    int port;

    DatagramSocket datagramSocket;

    private final int bufferLen = 65536;
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
                // Receive the packet
                DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(receivedPacket);

                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buffer));
                Ticket ticket = (Ticket) ois.readObject();

                if (ticket.getId() > 0) {
                    Optional<Ticket> optionalTicket = ticketStore.getAllTickets().
                            stream().filter(t -> t.getId() == ticket.getId()).findFirst();
                    if(optionalTicket.isPresent()) ticketStore.updateTicketStatus(ticket.getId(), ticket.getStatus());
                    else ticketStore.storeNewTicket(ticket.getReporter(), ticket.getTopic(),
                            ticket.getDescription(), ticket.getType(), ticket.getPriority());
                }

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ObjectOutputStream outputStream = new ObjectOutputStream(out);
                outputStream.writeObject(ticketStore.getAllTickets());
                outputStream.flush();
                datagramSocket.send(new DatagramPacket(out.toByteArray(), out.toByteArray().length,
                        receivedPacket.getAddress(), receivedPacket.getPort()));


                // Store the new ticket if it is valid
                /*if (ticket.getId() > 0) {
                    ticketStore.storeNewTicket(ticket.getReporter(), ticket.getTopic(),
                            ticket.getDescription(), ticket.getType(), ticket.getPriority());
                }
                // Serialize the ticket list
                List<Ticket> allTickets = ticketStore.getAllTickets();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                objectOutputStream.writeObject(allTickets);
                objectOutputStream.flush();
                byte[] ticketData = byteArrayOutputStream.toByteArray();
                // Define packet size and calculate the number of packets needed
                int packetSize = bufferLen - 1; // Reserve 1 byte for the sequence number
                int numPackets = (int) Math.ceil((double) ticketData.length / packetSize);
                // Send each packet
                for (int i = 0; i < numPackets; i++) {
                    int start = i * packetSize;
                    int length = Math.min(ticketData.length - start, packetSize);
                    byte[] packetData = new byte[length + 1];
                    packetData[0] = (byte) i; // Sequence number
                    System.arraycopy(ticketData, start, packetData, 1, length);
                    DatagramPacket packet = new DatagramPacket(packetData, packetData.length,
                            receivedPacket.getAddress(), receivedPacket.getPort());
                    datagramSocket.send(packet);
                }
                // Send an END signal to indicate the end of the transmission
                byte[] endSignal = "END".getBytes(); // END signal
                DatagramPacket endPacket = new DatagramPacket(endSignal, endSignal.length,
                        receivedPacket.getAddress(), receivedPacket.getPort());
                datagramSocket.send(endPacket);*/
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
