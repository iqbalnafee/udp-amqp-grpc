package de.uniba.rz.entities.network;

import de.uniba.rz.entities.Ticket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;

public class UdpDatagramPacket {

    public static DatagramPacket getNewUdpDatagramPacket(Ticket ticket) throws IOException {
        ByteArrayStream byteArrayStream = ByteArrayStream.getInstance();
        ObjectOutputStream os = byteArrayStream.getOs();
        os.writeObject(ticket);
        os.flush();
        byte[] data = byteArrayStream.getOut().toByteArray();
        return new DatagramPacket(data, data.length);
    }
}
