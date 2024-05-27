package de.uniba.rz.entities.network;

import de.uniba.rz.entities.Ticket;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;

public class UdpDatagramPacket {

    public static DatagramPacket getNewUdpDatagramPacket(byte[] data) {
        return new DatagramPacket(data, data.length);
    }
}
