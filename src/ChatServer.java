import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer implements  Runnable {
    public final static int PORT = 2020;
    private final static int BUFFER = 1024;

    private DatagramSocket socket;
    private ArrayList<InetAddress> client_addresses;
    private ArrayList<Integer> client_ports;
    private ArrayList<String> existing_clients;
    private ArrayList<String> joined_clients;
    private ArrayList<String> client_handles;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ChatServer() throws IOException {
        socket = new DatagramSocket(PORT);
        System.out.println("Server is running and is listening on port " + PORT);
        client_addresses = new ArrayList();
        client_ports = new ArrayList();
        existing_clients = new ArrayList();
        joined_clients = new ArrayList();
        client_handles = new ArrayList();
    }

    public void run() {
        byte[] buffer = new byte[BUFFER];
        while (true) {
            try {
                Arrays.fill(buffer, (byte) 0);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(buffer, 0, buffer.length);
                message = message.trim();

                InetAddress clientAddress = packet.getAddress();
                int client_port = packet.getPort();

                String id = clientAddress.toString().substring(1) + "|" + client_port;
                if (!existing_clients.contains(id)) {
                    existing_clients.add(id);
                    client_ports.add(client_port);
                    client_addresses.add(clientAddress);
                }
                String temp = "ERROR";
                try {
                    temp = message.substring(0, message.indexOf(' '));
                }
                catch (Exception e) {

                }

                if (temp.equals("/join") && !joined_clients.contains(id)) {
                    joined_clients.add(id);
                    System.out.println("Connection to the Message Board Server is successful!");
                    byte[] data = ("Connection to the Message Board Server is successful!").getBytes();
                    packet = new DatagramPacket(data, data.length, clientAddress, client_port);
                    socket.send(packet);
                }

                else if (temp.equals("/join") && joined_clients.contains(id)) {
                    System.out.println("Error: Command parameters do not match or is not allowed.");
                    byte[] data = ("Error: Command parameters do not match or is not allowed.").getBytes();
                    packet = new DatagramPacket(data, data.length, clientAddress, client_port);
                    socket.send(packet);
                }

                else if (temp.equals("/all")) {
                    message = message.substring(5);
                    System.out.println(id + ": " + message);
                    byte[] data = (id + ": " + message).getBytes();
                    for (int i = 0; i < client_addresses.size(); i++) {
                        InetAddress cl_address = client_addresses.get(i);
                        int cl_port = client_ports.get(i);
                        packet = new DatagramPacket(data, data.length, cl_address, cl_port);
                        socket.send(packet);
                    }
                }

                else {
                    System.out.println("Error: Command not found.");
                    byte[] data = ("Error: Command not found.").getBytes();
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