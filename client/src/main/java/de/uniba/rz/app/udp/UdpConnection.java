package de.uniba.rz.app.udp;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

public class UdpConnection {
    private static UdpConnection udpConnection;
    private DatagramSocket datagramSocket;

    private UdpConnection(String host, int port) {
        try {
            datagramSocket = new DatagramSocket();
            datagramSocket.connect(new InetSocketAddress(host,port));
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public static synchronized UdpConnection getUdpConnection(String host, int port){
        if (udpConnection == null) udpConnection = new UdpConnection(host, port);
        return udpConnection;
    }

    public DatagramSocket getDatagramSocket(){
        return datagramSocket;
    }

}
