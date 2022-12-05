import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

class MessageSender implements Runnable {
    private int portNo;
    private DatagramSocket socket;
    private String hostName;
    private ClientWindow window;
    private MessageReceiver receiver;
    private String join;

    MessageSender(DatagramSocket sock, String host, ClientWindow win, MessageReceiver receive, int port, String join) {
        socket = sock;
        hostName = host;
        window = win;
        receiver = receive;
        portNo = port;
        this.join = join;
    }

    private void sendMessage(String s) throws Exception {
        String command;
        JSONObject json;

        try {
            if (s.startsWith("/join") || s.equals("/leave") || s.startsWith("/register") ||
                    s.startsWith("/all") || s.startsWith("/msg") || s.equals("/?")) {
                if (!s.equals("/leave") && !s.equals("/?"))
                    command = s.substring(1, s.indexOf(' '));
                else
                    command = s.substring(1);
            }
            else if ((s.startsWith("/leave ") && s.length() > 7) || (s.startsWith("/? ") && s.length() > 3))
                command = "wrongparam";
            else
                command = " ";
        }
        catch (Exception e) {
            command = "wrongparam";
        }

        if (command.equals("join") || command.equals("leave") || command.equals("?")) {
            json = new JSONObject()
                    .put("command", command);
        }
        else if (command.equals("register")) {
            String handle;
            try {
                handle = s.substring(10);
            }
            catch (Exception e){
                handle = " ";
            }

            json = new JSONObject()
                    .put("command", command)
                    .put("handle", handle);
        }

        else if (command.equals("all")) {
            String message;
            try {
                message = s.substring(5);
            }
            catch (Exception e){
                message = "";
            }

            json = new JSONObject()
                    .put("command", command)
                    .put("message", message);
        }

        else if (command.equals("msg")) {
            String handle;
            String message;

            try {
                handle = s.substring(5);
                handle = handle.substring(0, handle.indexOf(' '));
            }
            catch (Exception e){
                handle = " ";
            }

            try {
                message = s.substring(5);
                message = message.substring(message.indexOf(' ') + 1);
            }
            catch (Exception e){
                message = "";
            }

            json = new JSONObject()
                    .put("command", command)
                    .put("handle", handle)
                    .put("message", message);
        }

        else {
            json = new JSONObject()
                    .put("command", command);
        }

        byte buffer[] = json.toString().getBytes();
        InetAddress address = InetAddress.getByName(hostName);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, portNo);
        socket.send(packet);
    }

    public void run() {
        boolean connected = false;
        try {
            sendMessage(join);
            Thread.sleep(100);
            if (receiver.joined) {
                connected = true;
            }
            else {
                System.out.println("Error: Connection to the Message Board Server has failed! Please check IP Address and Port Number.");
                window.dispose();
            }
        } catch (Exception e) {
            System.out.println("Error: Connection to the Message Board Server has failed! Please check IP Address and Port Number.");
            window.dispose();
        }

        while (!connected) {
            System.out.print("Enter /join <server_ip_add> <port> command: ");
            Scanner sc = new Scanner(System.in);
            String temp;
            do {
                temp = sc.nextLine();
                temp = temp.trim();

                if (temp.equals("/leave")) {
                    System.out.println("Error: Disconnection failed. Please connect to the server first.");
                    System.out.print("Enter /join <server_ip_add> <port> command: ");
                }

                else if (temp.startsWith("/register") || temp.startsWith("/all") || temp.startsWith("/msg")) {
                    System.out.println("Error: Command parameters do not match or is not allowed. Command not allowed in this case.");
                    System.out.print("Enter /join <server_ip_add> <port> command: ");
                }

                else if (temp.equals("/?")) {
                    System.out.println("\n---------------------------------------------------------\n" +
                            "Connect to the server application: /join <server_ip_add> <port>\n" +
                            "Disconnect to the server application: /leave\n" +
                            "Register a unique handle or alias (NO SPACES): /register <handle>\n" +
                            "Send message to all: /all <message>\n" +
                            "Send direct message to a single handle: /msg <handle> <message>\n" +
                            "Request command help to output all Input Syntax commands for references: /?\n" +
                            "---------------------------------------------------------\n");
                    System.out.print("Enter /join <server_ip_add> <port> command: ");
                }

                else if (!temp.startsWith("/join")) {
                    System.out.println("Error: Command not found.");
                    System.out.print("Enter /join <server_ip_add> <port> command: ");
                }
            } while (!temp.startsWith("/join"));
            String join = temp;
            try {
                temp = temp.substring(6);
            }
            catch (Exception e) {
                temp = " ";
            }
            try {
                hostName = temp.substring(0, temp.indexOf(' '));
            }
            catch (StringIndexOutOfBoundsException e) {
                hostName = " ";
            }
            try {
                portNo = Integer.parseInt(temp.substring(temp.indexOf(' ') + 1));
            }
            catch (Exception e) {
                portNo = 0;
            }
            try {
                window = new ClientWindow();
                receiver.window = window;
                sendMessage(join);
                Thread.sleep(100);
                if (receiver.joined) {
                    connected = true;
                }
                else {
                    System.out.println("Error: Connection to the Message Board Server has failed! Please check IP Address and Port Number.");
                    window.dispose();
                }
            } catch (Exception e) {
                System.out.println("Error: Connection to the Message Board Server has failed! Please check IP Address and Port Number.");
                window.dispose();
            }
        }

        while (true) {
            try {
                while (!window.message_is_ready) {
                    Thread.sleep(100);
                }
                sendMessage(window.getMessage());
                window.setMessageReady(false);
                if (window.getMessage().length() == 6 && window.getMessage().equals("/leave")) {
                    connected = false;
                    hostName = " ";
                    portNo = 0;
                    window.dispose();
                    System.out.println("Connection closed. Thank you!");
                    while (!connected) {
                        System.out.print("Enter /join <server_ip_add> <port> command: ");
                        Scanner sc = new Scanner(System.in);
                        String temp;
                        do {
                            temp = sc.nextLine();
                            temp = temp.trim();

                            if (temp.equals("/leave")) {
                                System.out.println("Error: Disconnection failed. Please connect to the server first.");
                                System.out.print("Enter /join <server_ip_add> <port> command: ");
                            }

                            else if (temp.startsWith("/register") || temp.startsWith("/all") || temp.startsWith("/msg")) {
                                System.out.println("Error: Command parameters do not match or is not allowed. Command not allowed in this case.");
                                System.out.print("Enter /join <server_ip_add> <port> command: ");
                            }

                            else if (temp.equals("/?")) {
                                System.out.println("\n---------------------------------------------------------\n" +
                                        "Connect to the server application: /join <server_ip_add> <port>\n" +
                                        "Disconnect to the server application: /leave\n" +
                                        "Register a unique handle or alias (NO SPACES): /register <handle>\n" +
                                        "Send message to all: /all <message>\n" +
                                        "Send direct message to a single handle: /msg <handle> <message>\n" +
                                        "Request command help to output all Input Syntax commands for references: /?\n" +
                                        "---------------------------------------------------------\n");
                                System.out.print("Enter /join <server_ip_add> <port> command: ");
                            }

                            else if (!temp.startsWith("/join")) {
                                System.out.println("Error: Command not found.");
                                System.out.print("Enter /join <server_ip_add> <port> command: ");
                            }
                        } while (!temp.startsWith("/join"));
                        String join = temp;
                        try {
                            temp = temp.substring(6);
                        }
                        catch (Exception e) {
                            temp = " ";
                        }
                        try {
                            hostName = temp.substring(0, temp.indexOf(' '));
                        }
                        catch (StringIndexOutOfBoundsException e) {
                            hostName = " ";
                        }
                        try {
                            portNo = Integer.parseInt(temp.substring(temp.indexOf(' ') + 1));
                        }
                        catch (Exception e) {
                            portNo = 0;
                        }
                        try {
                            window = new ClientWindow();
                            receiver.window = window;
                            sendMessage(join);
                            Thread.sleep(100);
                            if (receiver.joined) {
                                connected = true;
                            }
                            else {
                                System.out.println("Error: Connection to the Message Board Server has failed! Please check IP Address and Port Number.");
                                window.dispose();
                            }
                        } catch (Exception e) {
                            System.out.println("Error: Connection to the Message Board Server has failed! Please check IP Address and Port Number.");
                            window.dispose();
                        }
                    }
                }

                else if (window.getMessage().length() == 2 && window.getMessage().equals("/?")) {
                    System.out.println("\n---------------------------------------------------------\n" +
                            "Connect to the server application: /join <server_ip_add> <port>\n" +
                            "Disconnect to the server application: /leave\n" +
                            "Register a unique handle or alias (NO SPACES): /register <handle>\n" +
                            "Send message to all: /all <message>\n" +
                            "Send direct message to a single handle: /msg <handle> <message>\n" +
                            "Request command help to output all Input Syntax commands for references: /?\n" +
                            "---------------------------------------------------------\n");
                }
            } catch (Exception e) {
                window.displayMessage(e.getMessage());
            }
        }
    }
}

class MessageReceiver implements Runnable {
    DatagramSocket socket;
    byte buffer[];
    ClientWindow window;
    boolean joined = false;

    MessageReceiver(DatagramSocket sock, ClientWindow win) {
        socket = sock;
        buffer = new byte[1024];
        window = win;
    }

    public void run() {
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                JSONObject received = new JSONObject(new String(packet.getData(), 0, packet.getLength()).trim());

                String command = received.getString("command");

                if (command.equals("join")) {
                    joined = true;
                    window.displayMessage("Connection to the Message Board Server is successful!");
                }

                else if (command.equals("leave")) {
                    joined = false;
                    window.displayMessage("Connection closed. Thank you!");
                }

                else if (command.equals("register")) {
                    window.displayMessage("Welcome " + received.getString("handle") + "!");
                }

                else if (command.equals("all")) {
                    window.displayMessage(received.getString("handle") + ": " + received.getString("message"));
                }

                else if (command.equals("msg")) {
                    window.displayMessage(received.getString("message"));
                }

                else if (command.equals("?")) {
                    window.displayMessage(received.getString("message"));
                }

                else if (command.equals("error")) {
                    window.displayMessage(received.getString("message"));
                }

            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }
}

public class ChatClient {

    public static void main(String args[]) throws Exception {
        System.out.println("Upon connecting to a server, GUI will open.");
        // Enter /join localhost 2020
        System.out.print("Enter /join <server_ip_add> <port> command: ");
        Scanner sc = new Scanner(System.in);
        String temp;
        do {
            temp = sc.nextLine();
            temp = temp.trim();

            if (temp.equals("/leave")) {
                System.out.println("Error: Disconnection failed. Please connect to the server first.");
                System.out.print("Enter /join <server_ip_add> <port> command: ");
            }

            else if (temp.startsWith("/register") || temp.startsWith("/all") || temp.startsWith("/msg")) {
                System.out.println("Error: Command parameters do not match or is not allowed. Command not allowed in this case.");
                System.out.print("Enter /join <server_ip_add> <port> command: ");
            }

            else if (temp.equals("/?")) {
                System.out.println("\n---------------------------------------------------------\n" +
                        "Connect to the server application: /join <server_ip_add> <port>\n" +
                        "Disconnect to the server application: /leave\n" +
                        "Register a unique handle or alias (NO SPACES): /register <handle>\n" +
                        "Send message to all: /all <message>\n" +
                        "Send direct message to a single handle: /msg <handle> <message>\n" +
                        "Request command help to output all Input Syntax commands for references: /?\n" +
                        "---------------------------------------------------------\n");
                System.out.print("Enter /join <server_ip_add> <port> command: ");
            }

            else if (!temp.startsWith("/join")) {
                System.out.println("Error: Command not found.");
                System.out.print("Enter /join <server_ip_add> <port> command: ");
            }
        } while (!temp.startsWith("/join"));
        String hostName;
        int portNo;
        String join = temp;
        try {
            temp = temp.substring(6);
        }
        catch (Exception e) {
            temp = " ";
        }
        try {
            hostName = temp.substring(0, temp.indexOf(' '));
        }
        catch (StringIndexOutOfBoundsException e) {
            hostName = " ";
        }
        try {
            portNo = Integer.parseInt(temp.substring(temp.indexOf(' ') + 1));
        }
        catch (Exception e) {
            portNo = 0;
        }
        ClientWindow window = new ClientWindow();
        window.setTitle("Client Window");
        DatagramSocket socket = new DatagramSocket();
        MessageReceiver receiver = new MessageReceiver(socket, window);
        MessageSender sender = new MessageSender(socket, hostName, window, receiver, portNo, join);
        Thread receiverThread = new Thread(receiver);
        Thread senderThread = new Thread(sender);
        receiverThread.start();
        senderThread.start();
    }
}