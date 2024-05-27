package de.uniba.rz.entities.network;

import de.uniba.rz.entities.Ticket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class ByteArrayStream {

    private static ByteArrayStream instance;
    ByteArrayOutputStream out;
    ObjectOutputStream os;

    private ByteArrayStream() {
        try {
            out = new ByteArrayOutputStream();
            os = new ObjectOutputStream(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized ByteArrayStream getInstance() {
        if (instance == null) {
            instance = new ByteArrayStream();
        }
        return instance;
    }

    public ByteArrayOutputStream getOut() {
        return out;
    }

    public ObjectOutputStream getOs() {
        return os;
    }

    public static byte[] getByteDataFromObject(Ticket ticket) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(ticket);
        objectOutputStream.flush();
        return byteArrayOutputStream.toByteArray();
    }
}
