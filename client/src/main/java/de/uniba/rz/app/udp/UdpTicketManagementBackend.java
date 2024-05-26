package de.uniba.rz.app.udp;

import de.uniba.rz.app.TicketManagementBackend;
import de.uniba.rz.entities.Priority;
import de.uniba.rz.entities.Ticket;
import de.uniba.rz.entities.TicketException;
import de.uniba.rz.entities.Type;

import java.util.List;

public class UdpTicketManagementBackend implements TicketManagementBackend {

    String host;
    int port;

    public UdpTicketManagementBackend(String host, int port){
        this.host = host;
        this.port = port;
    }
    @Override
    public void triggerShutdown() {

    }

    @Override
    public Ticket createNewTicket(String reporter, String topic, String description, Type type, Priority priority) throws TicketException {
        return null;
    }

    @Override
    public List<Ticket> getAllTickets() throws TicketException {
        return null;
    }

    @Override
    public Ticket getTicketById(int id) throws TicketException {
        return null;
    }

    @Override
    public Ticket acceptTicket(int id) throws TicketException {
        return null;
    }

    @Override
    public Ticket rejectTicket(int id) throws TicketException {
        return null;
    }

    @Override
    public Ticket closeTicket(int id) throws TicketException {
        return null;
    }
}
