package de.uniba.rz.backend.udp;

import de.uniba.rz.backend.RemoteAccess;
import de.uniba.rz.backend.TicketStore;
import de.uniba.rz.entities.Ticket;
import de.uniba.rz.entities.network.ConnectionUtil;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpRemoteAccess implements RemoteAccess {
    String host;
    int port;

    int bufferLen = 65536;
    byte[] buf = new byte[65530];
    public UdpRemoteAccess(String host, String port) {
        this.host = host;
        this.port = Integer.parseInt(port);
    }

    @Override
    public void prepareStartup(TicketStore ticketStore) {
        try{


            DatagramSocket datagramSocket = ConnectionUtil.
                    getDatagramSocketForServer(port).getDatagramSocket();
            DatagramPacket dp = new DatagramPacket(buf, buf.length);
            datagramSocket.receive(dp);

            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buf));
            Ticket ticket = (Ticket) ois.readObject();

            ticketStore.storeNewTicket(ticket.getReporter(),ticket.getTopic(),
                    ticket.getDescription(),ticket.getType(),ticket.getPriority());

        }
        catch (Exception e){

        }
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void run() {

    }
}
