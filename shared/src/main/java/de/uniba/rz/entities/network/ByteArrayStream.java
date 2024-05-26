package de.uniba.rz.entities.network;

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
}
