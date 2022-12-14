import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

@SuppressWarnings("serial")
public class ClientWindow extends JFrame {

    String host_name;
    JTextPane message_field;
    JTextPane room_field;

    String message = "";
    boolean message_is_ready = false;
    String recentMessage = "";

    public ClientWindow() {

        setSize(800, 600);
        setTitle("Client Window");

        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        room_field = new JTextPane();
        message_field = new JTextPane();
        room_field.setEditable(false);
        ScrollPane x = new ScrollPane();
        x.add(room_field);
        ScrollPane z = new ScrollPane();
        z.add(message_field);
        z.setPreferredSize(new Dimension(100, 100));
        add(x, BorderLayout.CENTER);
        add(z, BorderLayout.SOUTH);
        room_field.setBackground(Color.BLACK);
        message_field.setBackground(Color.BLACK);
        message_field.setForeground(Color.WHITE);
        setVisible(true);
        message_field.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {

                if (e.getKeyCode() == 10) {
                    message_field.setCaretPosition(0);
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {

                if (e.getKeyCode() == 10 && !message_is_ready) {
                    message = message_field.getText().trim();
                    message_field.setText(null);
                    if (!message.equals(null) && !message.equals("")) {
                        message_is_ready = true;
                    }
                }
            }
        });
    }
    public Style changeTextColor (String command, String message) {
        Style style = message_field.addStyle("", null);
        if(command.equals("join"))
            StyleConstants.setForeground(style, Color.green);
        else if(command.equals("leave"))
            StyleConstants.setForeground(style, Color.green);
        else if(command.equals("register"))
            StyleConstants.setForeground(style, Color.green);
        else if(command.equals("all"))
            StyleConstants.setForeground(style, Color.white);
        else if(command.equals("msg"))
            StyleConstants.setForeground(style, Color.yellow);
        else if(command.equals("?"))
            StyleConstants.setForeground(style, Color.pink);
        else if(command.equals("error"))
            StyleConstants.setForeground(style, Color.red);
        else if(command.equals("list"))
            StyleConstants.setForeground(style, Color.white);
        else
            StyleConstants.setForeground(style, Color.white);
        return style;
    }
    public void displayMessage(String command, String receivedMessage) {
        recentMessage = receivedMessage;
        StyledDocument doc = room_field.getStyledDocument();
        Style style = changeTextColor(command, receivedMessage);
        try {
            doc.insertString(doc.getLength(), receivedMessage + "\n", style);
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }
    }

    public boolean isMessageReady() {
        return message_is_ready;
    }

    public void setMessageReady(boolean messageReady) {
        this.message_is_ready = messageReady;
    }

    public String getMessage() {
        return message;
    }

}