/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Juan Castillo
 */
public class ServerThread extends Thread{
    
    private Socket con;
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private ChatServer server;
    private String currentTopic;
    
    public ServerThread(Socket con, ChatServer server){
        this.server = server;
        currentTopic = "antesala";
        server.createTopic(currentTopic);
        this.con = con;
        initStreams();
        sendMessage("JOIN>Bienvenid@ a la antesala\nUsted puede:\nVer los usuarios conectados: list_users\nVer los temas activos: list_topics\n"
                + "Crear un nuevo tema: create_'nombre del tema'\nUnirse a un tema: join_'nombre del tema'\nSalir: quit");
    }

    private void initStreams() {
        System.out.println("Inicializando streams...");
        try {
            os = new ObjectOutputStream(con.getOutputStream());
            os.flush();
            is = new ObjectInputStream(con.getInputStream());
            System.out.println("Streams OK.");
        } catch (IOException ex) {
            System.out.println("Error al inicializar streams.");
            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void close(){
        System.out.println("Cerrando...");
        try{
            os.close();
            is.close();
            con.close();
            System.out.println("Conexión cerrada.");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    @Override
    public void run(){
        try{
            while(true){
                String msg = "";
                try {
                    msg = (String) is.readObject();
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                    msg = "Error al recibir mensaje.";
                }
                System.out.println(msg);
                String[] splitMsg = msg.split(">+");
                System.out.println("MESSAGE TYPE: " + splitMsg[0]);
                switch(splitMsg[0]){
                    case "LOGN":
                        sendMessage("NAME>" + server.checkForName(splitMsg[1]));
                        break;
                    case "LSTU":
                        sendMessage("LSTU>" + server.getUserNames());
                        break;
                    case "LSTT":
                        sendMessage("LSTT>" + listTopics());
                        break;
                    case "CREA":
                        currentTopic = splitMsg[2];
                        server.createTopic(splitMsg[2]);
                        sendMessage("CREA>" + server.subscribeToTopic(splitMsg[2], os));
                        break;
                    case "JOIN":
                        if(!splitMsg[2].equals(currentTopic)){
                            server.unsubscribeToTopic(currentTopic, os);
                            currentTopic = splitMsg[2];
                            sendMessage("JOIN>" + server.subscribeToTopic(splitMsg[2], os));
                        } else {
                            sendMessage("JOIN>Ya se encuentra en esa sala");
                        }
                        break;
                    case "MESS":
                        ArrayList<ObjectOutputStream> subs = server.getTopicOutputStreams(currentTopic);
                        sendMessageToMany("MESS>" + splitMsg[1], subs);
                        break;
                    default:
                        break;
                } 
            }
        }
        catch (IOException ex) {
            close();
            try {
                join();
            } catch (InterruptedException ex1) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }

    private String listTopics() {
        Object[] topics = server.getTopics();
        String s = "Temas:\n";
        for(int i = 0; i < topics.length; i++){
            s += (String)topics[i] + "\n";
        }
        if(s.equals("Temas:\n")){
            s += "No se ha creado ningun tema. Para crear uno, escriba 'create_topic'";
        }
        return s;
    }
    
    private void sendMessage(String message) {
        try{
            os.flush();
            os.writeObject(message);
        }
        catch(Exception e){
            System.out.println("\n error al enviar");
        }
    }
    
    private void sendMessageToMany(String message, ArrayList<ObjectOutputStream> subs){
        for(ObjectOutputStream o : subs){
            try {
                o.writeObject(message);
            } catch (IOException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}