import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketException;

public class ChatReceiverThread extends Thread{

    MulticastSocket local;
    JTextArea chatbox;
    Client client;
    String project;
    public ChatReceiverThread(MulticastSocket receiver, JTextArea chatbox, Client processor, String project){
        this.local=receiver;
        this.chatbox = chatbox;
        this.client = processor;
        this.project = project;
    }

    @Override
    public void run() {


        try{
            while(true) {
                byte [] buffer = new byte[8192];
                DatagramPacket dp=new DatagramPacket(buffer,buffer.length);
                local.receive(dp);
                String s = new String(dp.getData());
                System.out.println("[CHAT LISTENER] Received: " + s);

                client.chatHistorySaver(project,s+ "\n");

                chatbox.append(s+"\n");

            }
        }catch(SocketException es){es.printStackTrace();}
        catch (IOException ex){
            System.out.println (ex);
        }
    }

    void updateBoxArea(JTextArea box){
        this.chatbox = box;
    }
}
