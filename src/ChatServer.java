import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer implements  Runnable {
    public final static int PORT = 12345;
    private final static int BUFFER = 1024;

    private DatagramSocket socket;
    private ArrayList<InetAddress> client_addresses;
    private ArrayList<Integer> client_ports;
    private ArrayList<Client> existing_clients;
    private ArrayList<String> existing_address_ports;
    private ArrayList<String> joined_clients;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ChatServer() throws IOException {
        socket = new DatagramSocket(PORT);
        System.out.println("Server is running and is listening on port " + PORT);
        client_addresses = new ArrayList();
        client_ports = new ArrayList();
        existing_clients = new ArrayList();
        existing_address_ports = new ArrayList();
        joined_clients = new ArrayList();
    }

    class Client
    {
        public String address_port;
        public String handle;

        public Client(String address_port, String handle) {
            this.address_port = address_port;
            this.handle = handle;
        }
    };

    public void run() {
        byte[] buffer = new byte[BUFFER];
        while (true) {
            try {
                Arrays.fill(buffer, (byte) 0);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                JSONObject message = new JSONObject(new String(buffer, 0, buffer.length));

                String command = message.getString("command");

                InetAddress clientAddress = packet.getAddress();
                int client_port = packet.getPort();

                String id = clientAddress.toString().substring(1) + "|" + client_port;
                if (!existing_address_ports.contains(id)) {
                    existing_address_ports.add(id);
                    existing_clients.add(new Client(id, " "));
                }

                JSONObject toSend;
                JSONObject toSendfrom;
                JSONObject toSendto;

                if (command.equals("join") && !joined_clients.contains(id)) {
                    joined_clients.add(id);
                    System.out.println("Connection to the Message Board Server is successful!");
                    toSend = new JSONObject()
                            .put("command", command)
                            .put("message", "Connection to the Message Board Server is successful!");
                    byte[] data = toSend.toString().getBytes();
                    packet = new DatagramPacket(data, data.length, clientAddress, client_port);
                    socket.send(packet);
                }

                else if (command.equals("join") && joined_clients.contains(id)) {
                    System.out.println("Error: Command parameters do not match or is not allowed.");
                    toSend = new JSONObject()
                            .put("command", "error")
                            .put("message", "Error: Command parameters do not match or is not allowed.");
                    byte[] data = toSend.toString().getBytes();
                    packet = new DatagramPacket(data, data.length, clientAddress, client_port);
                    socket.send(packet);
                }

                else if (command.equals("leave")) {
                    joined_clients.remove(id);
                    for (int i = 0; i < existing_clients.size(); i++) {
                        if (existing_clients.get(i).address_port.equals(id)) {
                            if (!existing_clients.get(i).handle.equals(" ")) {
                                client_ports.remove((Integer) client_port);
                                client_addresses.remove(clientAddress);
                            }
                            break;
                        }
                    }
                    existing_address_ports.remove(id);
                    for (int i = 0; i < existing_clients.size(); i++) {
                        if (existing_clients.get(i).address_port.equals(id)) {
                            existing_clients.remove(i);
                            break;
                        }
                    }
                    System.out.println("Connection closed. Thank you!");
                    toSend = new JSONObject()
                            .put("command", command)
                            .put("message", "Connection closed. Thank you!");
                    byte[] data = toSend.toString().getBytes();
                    packet = new DatagramPacket(data, data.length, clientAddress, client_port);
                    socket.send(packet);
                }

                else if (command.equals("register")) {
                    boolean taken = false;
                    boolean alreadyHas = false;
                    boolean space = false;

                    for (int i = 0; i < existing_clients.size(); i++) {
                        if (existing_clients.get(i).handle.equals(message.getString("handle"))) {
                            taken = true;
                        }
                    }

                    String handle = " ";
                    for (int i = 0; i < existing_clients.size(); i++) {
                        if (existing_clients.get(i).address_port.equals(id)) {
                            handle = existing_clients.get(i).handle;
                            break;
                        }
                    }
                    if (!handle.equals(" "))
                        alreadyHas = true;

                    if (message.getString("handle").contains(" "))
                        space = true;

                    if (!taken && !space && !alreadyHas) {
                        for (int i = 0; i < existing_clients.size(); i++) {
                            if (existing_clients.get(i).address_port.equals(id)) {
                                existing_clients.get(i).handle = message.getString("handle");
                            }
                        }
                        client_ports.add(client_port);
                        client_addresses.add(clientAddress);
                        System.out.println("Welcome " + message.getString("handle") + "!");
                        toSend = new JSONObject()
                                .put("command", command)
                                .put("handle", message.getString("handle"));
                        byte[] data = toSend.toString().getBytes();
                        packet = new DatagramPacket(data, data.length, clientAddress, client_port);
                        socket.send(packet);
                    }

                    else if (taken){
                        System.out.println("Error: Registration failed. Handle or alias already exists.");
                        toSend = new JSONObject()
                                .put("command", "error")
                                .put("message", "Error: Registration failed. Handle or alias already exists.");
                        byte[] data = toSend.toString().getBytes();
                        packet = new DatagramPacket(data, data.length, clientAddress, client_port);
                        socket.send(packet);
                    }

                    else if (space){
                        System.out.println("Error: Registration failed. Make sure you have no spaces in the handle.");
                        toSend = new JSONObject()
                                .put("command", "error")
                                .put("message", "Error: Registration failed. Make sure you have no spaces in the handle.");
                        byte[] data = toSend.toString().getBytes();
                        packet = new DatagramPacket(data, data.length, clientAddress, client_port);
                        socket.send(packet);
                    }

                    else if (alreadyHas) {
                        System.out.println("Error: Registration failed. You already have a handle.");
                        toSend = new JSONObject()
                                .put("command", "error")
                                .put("message", "Error: Registration failed. You already have a handle.");
                        byte[] data = toSend.toString().getBytes();
                        packet = new DatagramPacket(data, data.length, clientAddress, client_port);
                        socket.send(packet);
                    }
                }

                else if (command.equals("all")) {
                    String handle = " ";
                    for (int i = 0; i < existing_clients.size(); i++) {
                        if (existing_clients.get(i).address_port.equals(id)) {
                            handle = existing_clients.get(i).handle;
                            break;
                        }
                    }
                    if (!handle.equals(" ")) {
                        String msg = message.getString("message");
                        if (msg.equals("")) {
                            System.out.println("Error: Command parameters do not match or is not allowed.");
                            toSend = new JSONObject()
                                    .put("command", "error")
                                    .put("message", "Error: Command parameters do not match or is not allowed.");
                            byte[] data = toSend.toString().getBytes();
                            packet = new DatagramPacket(data, data.length, clientAddress, client_port);
                            socket.send(packet);
                        } else {
                            msg = msg.trim();
                            String name = " ";
                            for (int i = 0; i < existing_clients.size(); i++) {
                                if (existing_clients.get(i).address_port.equals(id)) {
                                    name = existing_clients.get(i).handle;
                                }
                            }
                            System.out.println(name + ": " + msg);
                            toSend = new JSONObject()
                                    .put("command", command)
                                    .put("handle", name)
                                    .put("message", msg);
                            byte[] data = toSend.toString().getBytes();
                            for (int i = 0; i < client_addresses.size(); i++) {
                                InetAddress cl_address = client_addresses.get(i);
                                int cl_port = client_ports.get(i);
                                packet = new DatagramPacket(data, data.length, cl_address, cl_port);
                                socket.send(packet);
                            }
                        }
                    }
                    else {
                        System.out.println("Error: Command parameters do not match or is not allowed. Register first.");
                        toSend = new JSONObject()
                                .put("command", "error")
                                .put("message", "Error: Command parameters do not match or is not allowed. Register first.");
                        byte[] data = toSend.toString().getBytes();
                        packet = new DatagramPacket(data, data.length, clientAddress, client_port);
                        socket.send(packet);
                    }
                }

                else if (command.equals("msg")) {
                    String handle = " ";
                    for (int i = 0; i < existing_clients.size(); i++) {
                        if (existing_clients.get(i).address_port.equals(id)) {
                            handle = existing_clients.get(i).handle;
                            break;
                        }
                    }
                    if (!handle.equals(" ")) {
                        String msg = message.getString("message");
                        if (msg.equals("")) {
                            System.out.println("Error: Command parameters do not match or is not allowed.");
                            toSend = new JSONObject()
                                    .put("command", "error")
                                    .put("message", "Error: Command parameters do not match or is not allowed.");
                            byte[] data = toSend.toString().getBytes();
                            packet = new DatagramPacket(data, data.length, clientAddress, client_port);
                            socket.send(packet);
                        } else {
                            msg = msg.trim();
                            boolean invalid = true;
                            String namefrom = " ";
                            String nameto = message.getString("handle");
                            InetAddress cl_addressfrom = clientAddress;
                            int cl_portfrom = client_port;
                            for (int i = 0; i < existing_clients.size(); i++) {
                                if (existing_clients.get(i).handle.equals(nameto)) {
                                    String address_port = existing_clients.get(i).address_port;
                                    try {
                                        cl_addressfrom = InetAddress.getByName(address_port.substring(0, address_port.indexOf("|")));
                                    } catch (Exception e) {
                                        invalid = true;
                                    }
                                    try {
                                        cl_portfrom = Integer.parseInt(address_port.substring(address_port.indexOf("|") + 1));
                                    } catch (Exception e) {
                                        invalid = true;
                                    }
                                    invalid = false;
                                }
                            }
                            for (int i = 0; i < existing_clients.size(); i++) {
                                if (existing_clients.get(i).address_port.equals(id)) {
                                    namefrom = existing_clients.get(i).handle;
                                }
                            }

                            if (!invalid) {
                                System.out.println("[From " + namefrom + "]: " + msg);
                                System.out.println("[To " + nameto + "]: " + msg);
                                toSendfrom = new JSONObject()
                                        .put("command", "msg")
                                        .put("message", "[From " + namefrom + "]: " + msg);
                                toSendto = new JSONObject()
                                        .put("command", "msg")
                                        .put("message", "[To " + nameto + "]: " + msg);
                                byte[] datafrom = toSendfrom.toString().getBytes();
                                byte[] datato = toSendto.toString().getBytes();
                                packet = new DatagramPacket(datafrom, datafrom.length, cl_addressfrom, cl_portfrom);
                                socket.send(packet);
                                packet = new DatagramPacket(datato, datato.length, clientAddress, client_port);
                                socket.send(packet);
                            } else {
                                System.out.println("Error: Wrong parameters, or handle or alias not found.");
                                toSend = new JSONObject()
                                        .put("command", "error")
                                        .put("message", "Error: Wrong parameters, or handle or alias not found.");
                                byte[] data = toSend.toString().getBytes();
                                packet = new DatagramPacket(data, data.length, clientAddress, client_port);
                                socket.send(packet);
                            }
                        }
                    }
                    else {
                        System.out.println("Error: Command parameters do not match or is not allowed. Register first.");
                        toSend = new JSONObject()
                                .put("command", "error")
                                .put("message", "Error: Command parameters do not match or is not allowed. Register first.");
                        byte[] data = toSend.toString().getBytes();
                        packet = new DatagramPacket(data, data.length, clientAddress, client_port);
                        socket.send(packet);
                    }
                }

                else if (command.equals("?")) {
                    String help = "\n---------------------------------------------------------\n" +
                            "Connect to the server application: /join <server_ip_add> <port>\n" +
                            "Disconnect to the server application: /leave\n" +
                            "Register a unique handle or alias (NO SPACES): /register <handle>\n" +
                            "Send message to all: /all <message>\n" +
                            "Send direct message to a single handle: /msg <handle> <message>\n" +
                            "Request command help to output all Input Syntax commands for references: /?\n" +
                            "---------------------------------------------------------\n";
                    System.out.println(help);
                    toSend = new JSONObject()
                            .put("command", command)
                            .put("message", help);
                    byte[] data = toSend.toString().getBytes();
                    packet = new DatagramPacket(data, data.length, clientAddress, client_port);
                    socket.send(packet);
                }

                else if (command.equals("wrongparam")){
                    System.out.println("Error: Command parameters do not match or is not allowed.");
                    toSend = new JSONObject()
                            .put("command", "error")
                            .put("message", "Error: Command parameters do not match or is not allowed.");
                    byte[] data = toSend.toString().getBytes();
                    packet = new DatagramPacket(data, data.length, clientAddress, client_port);
                    socket.send(packet);
                }

                else {
                    System.out.println("Error: Command not found.");
                    toSend = new JSONObject()
                            .put("command", "error")
                            .put("message", "Error: Command not found.");
                    byte[] data = toSend.toString().getBytes();
                    packet = new DatagramPacket(data, data.length, clientAddress, client_port);
                    socket.send(packet);
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    public static void main(String args[]) throws Exception {
        ChatServer server_thread = new ChatServer();
        server_thread.run();
    }
}