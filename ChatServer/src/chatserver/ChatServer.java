/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.util.ArrayList;
import java.util.Set;

/**
 *
 * @author Juan Castillo
 */
public class ChatServer implements Runnable{

    private static int port;
    private JTextField usrText;
    private JTextArea chatWindow;
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private ServerSocket server;
    private Socket con;
    private int nextPort;
    private static HashMap<String, ArrayList<ObjectOutputStream>> topicsAndSubs;
    private static ArrayList<String> users;
    
    /**
     * @param args the command line arguments
     */    
    public static void main(String[] args) {
        int serverPort;
        //        String s = "Ingrese el puerto que desee utilizar:";
        try {
            System.out.println(InetAddress.getLocalHost());
        } catch (UnknownHostException ex) {
            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        while(true){
            System.out.println("Ingrese el puerto que desee utilizar:");
            try{
//                s = (String)JOptionPane.showInputDialog(null, s, "Input", JOptionPane.PLAIN_MESSAGE);
                BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                String s = bufferRead.readLine();
                serverPort = Integer.parseInt(s);
                break;
            }
            catch(Exception e)
            {
//                s = "Ingrese un puerto valido";
                    System.out.println("Por favor ingrese un puerto valido");
            }
        }
        
        ChatServer cs = new ChatServer(serverPort);
        Thread mainThread = new Thread(cs);
        mainThread.start();
    }
    
    public ChatServer(int serverPort){
        this.port = serverPort;
        topicsAndSubs = new HashMap<String, ArrayList<ObjectOutputStream>>();
        users = new ArrayList<String>();
//        nextPort = port + 1;
        try{
            server = new ServerSocket(port);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public ArrayList<ObjectOutputStream> getTopicOutputStreams(String roomName){
        return topicsAndSubs.get(roomName);
    }
    
    public Object[] getTopics(){
        Set rooms = topicsAndSubs.keySet();
        ArrayList<Object> roomNames = new ArrayList<Object>();
        for(Object roomName : rooms){
            roomNames.add(roomName);
        }
        return roomNames.toArray();
    }
   
    public String checkForName(String checkName){
        if(!users.isEmpty()){
            for(String n : users){
                if(n.equals(checkName)){
                    checkName += 1;
                    break;
                }
                System.out.print(n);
            }
        }
        registerUser(checkName);
        return checkName;
    }
    
    public String getUserNames(){
        String s = "Usuarios conectados:\n";
        for(String u : users){
            s += (u + "\n");
        }
        System.out.println(s);
        return s;
    }
    
    public void createTopic(String topic){
        if(!topicsAndSubs.containsKey(topic)){
            topicsAndSubs.put(topic, new ArrayList<ObjectOutputStream>());
        } else {
            topicsAndSubs.put(topic + 1, new ArrayList<ObjectOutputStream>());
        }
    }
    
    public String subscribeToTopic(String topicName, ObjectOutputStream clientOutputStream){
        String s = "Bienvenido a " + topicName;
        if(topicsAndSubs.containsKey(topicName)){
            topicsAndSubs.get(topicName).add(clientOutputStream);
        } else {
            s = "El tema no existe.";
        }
        return s;
    }
    
    public void unsubscribeToTopic(String topicName, ObjectOutputStream clientOuputStream){
        System.out.println("Se dessubscribira a " + topicName);
        if(topicsAndSubs.containsKey(topicName)){
            topicsAndSubs.get(topicName).remove(clientOuputStream);
            if(topicsAndSubs.get(topicName).isEmpty()){
                topicsAndSubs.remove(topicName);
            }
        }
    }
    
    public void registerUser(String userName){
        users.add(userName);
    }
    
    @Override
    public void run() {
        String msg = "";
        while(true){
            try{
                ServerThread st = new ServerThread(server.accept(), this);
                st.start();
//                msg = (String) is.readObject();
                String[] splitMsg = msg.split(">");
                switch(splitMsg[0]){
                    case "LGN":
                        st.start();
                        break;
                    default:
                        System.out.println(splitMsg[0]);
                        break;
                }
//                showMessage("\n" + msg);
            }
            catch(IOException e){
                e.printStackTrace();
            }
//            catch(ClassNotFoundException e){
//                e.printStackTrace();
//            }
        }
//        while(true){
//            try{
//                Socket client = server.accept();
//            } 
//            catch (Exception e){
//                e.printStackTrace();
//            }
//        }
    }

//    private void initStreams(){
//        System.out.println("Inicializando streams...");
//        try {
//            os = new ObjectOutputStream(con.getOutputStream());
//            os.flush();
//            is = new ObjectInputStream(con.getInputStream());
//            System.out.println("Streams OK.");
//        } catch (IOException ex) {
//            System.out.println("Error al inicializar streams.");
//            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        
//    }
    
//    private void close(){
//        System.out.println("Cerrando...");
//        try{
//            os.close();
//            is.close();
//            con.close();
//            System.out.println("Conexión cerrada.");
//        }
//        catch(Exception e){
//            e.printStackTrace();
//        }
//    }
    
//    public void sendMessage(String msg){
//        try{
//            os.writeObject("SERVER - " + msg);
//            os.flush();
//            System.out.println("\nSERVER - " + msg);
//        }
//        catch(Exception e){
//            chatWindow.append("\n Error while sending");
//        }
//    }
}
