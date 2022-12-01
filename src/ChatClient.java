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

    MessageSender(DatagramSocket sock, String host, ClientWindow win, MessageReceiver receive, int port) {
        socket = sock;
        hostName = host;
        window = win;
        receiver = receive;
        portNo = port;
    }

    private void sendMessage(String s) throws Exception {
        byte buffer[] = s.getBytes();
        InetAddress address = InetAddress.getByName(hostName);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, portNo);
        socket.send(packet);
    }

    public void run() {
        boolean connected = false;
        try {
            sendMessage("Connection to the Message Board Server is successful!");
            connected = true;
        } catch (Exception e) {
            System.out.println("Error: Connection to the Message Board Server has failed! Please check IP Address and Port Number.");
            window.dispose();
        }

        while (!connected) {
            System.out.print("Enter /join command: ");
            Scanner sc = new Scanner(System.in);
            String temp;
            do {
                temp = sc.nextLine();
                if (!temp.startsWith("/join ")) {
                    System.out.println("Error: Command not found.");
                    System.out.print("Enter /join command: ");
                }
            } while (!temp.startsWith("/join "));
            temp = temp.substring(6);
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
            System.out.println(hostName + " " + portNo + " " + temp + " " + temp.substring(temp.indexOf(' ')));
            try {
                sendMessage("Connection to the Message Board Server is successful!");
                connected = true;
                window = new ClientWindow();
                receiver.window = window;
            } catch (Exception e) {
                System.out.println("Error: Connection to the Message Board Server has failed! Please check IP Address and Port Number.");
            }
        }

        while (true) {
            try {
                while (!window.message_is_ready) {
                    Thread.sleep(100);
                }
                sendMessage(window.getMessage());
                window.setMessageReady(false);
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
                String received = new String(packet.getData(), 1, packet.getLength() - 1).trim();
                System.out.println(received);
                window.displayMessage(received);
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
        System.out.print("Enter /join command: ");
        Scanner sc = new Scanner(System.in);
        String temp;
        do {
            temp = sc.nextLine();
            if (!temp.startsWith("/join ")) {
                System.out.println("Error: Command not found.");
                System.out.print("Enter /join command: ");
            }
        } while (!temp.startsWith("/join "));
        String hostName;
        int portNo;
        temp = temp.substring(6);
        try {
            hostName = temp.substring(0, temp.indexOf(' '));
        }
        catch (StringIndexOutOfBoundsException e) {
            hostName = "0";
        }
        try {
            portNo = Integer.parseInt(temp.substring(temp.indexOf(' ') + 1));
        }
        catch (Exception e) {
            portNo = 0;
        }
        ClientWindow window = new ClientWindow();
        window.setTitle("Message Board Client");
        DatagramSocket socket = new DatagramSocket();
        MessageReceiver receiver = new MessageReceiver(socket, window);
        MessageSender sender = new MessageSender(socket, hostName, window, receiver, portNo);
        Thread receiverThread = new Thread(receiver);
        Thread senderThread = new Thread(sender);
        receiverThread.start();
        senderThread.start();
    }
}