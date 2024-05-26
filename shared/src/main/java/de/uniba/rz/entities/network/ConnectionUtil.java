package de.uniba.rz.entities.network;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class ConnectionUtil {
    private static ConnectionUtil udpConnection;
    private DatagramSocket datagramSocket;

    private ConnectionUtil(String host, int port) {
        try {
            datagramSocket = new DatagramSocket();
            datagramSocket.connect(new InetSocketAddress(host,port));
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private ConnectionUtil(int port) {
        try {
            datagramSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public static synchronized ConnectionUtil getUdpConnection(String host, int port){
        if (udpConnection == null) udpConnection = new ConnectionUtil(host, port);
        return udpConnection;
    }

    public static synchronized ConnectionUtil getDatagramSocketForServer(int port){
        if (udpConnection == null) udpConnection = new ConnectionUtil(port);
        return udpConnection;
    }

    public DatagramSocket getDatagramSocket(){
        return datagramSocket;
    }

}
