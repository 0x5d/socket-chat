/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package chatclient;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author Juan Castillo
 */
public class ChatClient extends JFrame implements ActionListener{
    
    private  int port;
    private String ip;
    private String name;
    private JTextField usrText;
    private JTextArea chatWindow;
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private Socket con;
    private boolean lobby;
    private boolean writing;
    
    public static void main(String[] args) {
        int clientPort;
        String serverIP;
        String clientName;
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        while(true){
            serverIP = JOptionPane.showInputDialog(null, "Ingrese la direccion IP del servidor:", "IP", 1);
                if (serverIP == null || serverIP.equals("")){
                    continue;
                }
                break;
        }
        while(true){
            try{
                String s = JOptionPane.showInputDialog(null, "Ingrese el puerto que desee usar:", "Puerto", 1);
                clientPort = Integer.parseInt(s);
                break;
            }
            catch(Exception e)
            {
                    System.out.println("Por favor ingrese un puerto valido");
            }
        }
        while(true){
            clientName = JOptionPane.showInputDialog(null, "Ingrese su pseudonimo:", "Pseudonimo", 1);
            if(clientName.equals("") || clientName == null){
                continue;
            }
            break;
        }
        
        ChatClient client = new ChatClient(serverIP, clientPort, clientName);
        client.startClient();
    }
    
    public ChatClient(String serverIP, int clientPort, String clientName){
        ip = serverIP;
        port = clientPort;
        name = clientName;
        lobby = false;
        writing = false;
        usrText = new JTextField();
        usrText.setEditable(false);
        usrText.addActionListener(this);
        add(usrText, BorderLayout.NORTH);
        chatWindow = new JTextArea();
        chatWindow.setEditable(false);
        add(new JScrollPane(chatWindow), BorderLayout.CENTER);
        setSize(300, 150);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = e.getActionCommand();
        if(msg.length() <= 160){
            switch(msg){
                case "list_users":
                    sendMessage("LSTU>" + name);
                    break;
                case "list_topics":
                    sendMessage("LSTT>" + name);
                    break;
                case "quit":
                    this.dispose();
                    System.exit(1);
                default:
                    if(msg.length() > 7 && msg.substring(0, 7).equals("create_")){
                        sendMessage("CREA>" + name + ">" + msg.substring(7));
                        break;
                    } else if(msg.length() > 5 && msg.substring(0, 5).equals("join_")){
                        sendMessage("JOIN>" + name + ">" + msg.substring(5));
                    }
                    else{
                        sendMessage("MESS>" + name + ">" + e.getActionCommand());
                    }
                    break;
            }
        }
        else{
            showMessage("\nSu mensaje excede los 160 caracteres y no pudo enviarse.\n");
        }
        usrText.setText("");
    }

    private void sendMessage(String message) {
        try {
            os.flush();
            os.writeObject(message);
        } catch (IOException ex) {
            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, null, ex);
            close();
            System.exit(0);
        }
    }

    public void startClient(){
        try{
            connect(ip, port);
            setUpStreams();
            sendMessage("LOGN>" + name);
            runClient();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            close();
            System.exit(0);
        }
    }

    private void close() {
        showMessage("\n Cerrando...");
        ableToType(false);
        try{
            os.close();
            is.close();
            con.close();
        }
        catch(Exception e){
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void setUpStreams() throws IOException{
        os = new ObjectOutputStream(con.getOutputStream());
        os.flush();
        is = new ObjectInputStream(con.getInputStream());
    }

    private void runClient() throws IOException{
        ableToType(true);
        String msg = "";
        while(true){
            try{
                msg = (String) is.readObject();
                String[] splitMsg = msg.split(">+");
                switch(splitMsg[0]){
                    case "NAME":
                        name = splitMsg[1];
                        break;
                    case "MESS":
                        System.out.println(msg);
                        showMessage(splitMsg[1] + "> " + splitMsg[2] + "\n");
                        break;
                    case "LSTT":
                        showMessage(splitMsg[1]);
                        break;
                    case "LSTU":
                        showMessage(splitMsg[1]);
                        break;
                    case "CREA":
                        showMessage(splitMsg[1]);
                        break;
                    case "JOIN":
                        showMessage(splitMsg[1]);
                        break;
                    default:
                        break;
                }
            }
            catch(ClassNotFoundException e){
                showMessage("Hubo un error al recibir mensajes.\n");
                e.printStackTrace();
            }
        }
    }

    private void showMessage(String m) {
        chatWindow.append(m);
    }
    
    private void connect(String ipAddress, int portNumber){
        try {
            con = new Socket(ipAddress, portNumber);
            showMessage("Conectado.");
        } catch (IOException ex) {
            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(0);
        }
    }

    private void ableToType(boolean b) {
        usrText.setEditable(b);
    }
}
