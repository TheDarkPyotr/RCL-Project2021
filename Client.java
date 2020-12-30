import sun.misc.Signal;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;



public class Client extends RemoteObject implements UserUpdateNotify {

    private static int port;

    private InetAddress address=null;
    private Socket s1=null;
    private String line=null;
    private BufferedReader br=null;
    private BufferedReader is=null;
    private PrintWriter os=null;
    private ServerInterface serverNotifier = null;
    private UserUpdateNotify stubNotifier = null;
    private HashMap<String,String> userList;
    public String[] tempList = null;
    private String authenticatedUsername = null;
    private HashMap<String, MulticastSocket> multicastRegister;
    private HashMap<String, ChatReceiverThread>  chatListener;

    private HashMap<String,String> chatHistory;

    public Client(int port) throws UnknownHostException, IOException {

        super();
        address=InetAddress.getLocalHost();
        this.port = port;


            s1=new Socket(address, 2021); // You can use static final constant PORT_NUM
            br= new BufferedReader(new InputStreamReader(System.in));
            is=new BufferedReader(new InputStreamReader(s1.getInputStream()));
            os= new PrintWriter(s1.getOutputStream());
            userList = new HashMap<>();
            multicastRegister = new HashMap<>();
            chatHistory = new HashMap<>();
            chatListener = new HashMap<>();


    }

    public Integer moveCard(String projectName, String cardName, String destinationList){

        String response=null;
        Integer status = -1;
        try{
            line= "MOVE-CARD-REQUEST#" +projectName+"#"+cardName+"#"+destinationList;

            os.println(line);
            os.flush();
            response=is.readLine();
            System.out.println("[CLIENT] Move project card request - parameters: "+projectName+"#"+cardName+"#"+destinationList);
            System.out.println("[CLIENT] Server Response : "+response);

           switch(response){

               case "OK":
                   status = 0;
                   break;
               case "COINCIDENT-LISTS":
                   status = 1;
                   break;
               case "STATE-VIOLATION":
                   status = 2;
                   break;
               case "INTERNAL-ERROR":
                   status =  3;
                   break;
               case "PROJECT-ELIMINATED":
                   status =  4;
                   break;
               default:
                   status = 3;
                break;

           }


        }
        catch(IOException e){
            e.printStackTrace();
            System.out.println("[CLIENT] Socket read Error");
        }

        return status;

    }

    public String[] getCardHistory(String projectName, String cardName){

        String response=null;
        String[] history = null;
        try{
            line= "CARD-HISTORY-REQUEST#" +projectName+"#"+cardName;

            os.println(line);
            os.flush();
            response=is.readLine();
            System.out.println("[CLIENT] Show card history - parameters: "+projectName+"#"+cardName);
            System.out.println("[CLIENT] Server Response : "+response);

            if(response.compareTo("PROJECT-ELIMINATED") == 0) return null;
            else {
                String delims = "[#]+";
                history = response.split(delims);
            }


        }
        catch(IOException e){
            e.printStackTrace();
            System.out.println("[CLIENT] Socket read Error");
        }


        return history;

    }

    public Integer addMember(String projectName, String memberName){

        String response=null;
        try{
            line= "ADD-MEMBER-REQUEST#" +projectName+"#"+memberName;

            os.println(line);
            os.flush();
            response=is.readLine();
            System.out.println("[CLIENT] Show project cards - parameters: "+projectName+"#"+memberName);
            System.out.println("[CLIENT] Server Response : "+response);

        }
        catch(IOException e){
            e.printStackTrace();
            System.out.println("[CLIENT] Socket read Error");
        }

        if(response.compareTo("ALREADY-MEMBER") == 0) return 2;
        return 0;

    }

    public String[] showCard(String projectName, String cardName){
        String response=null;
        String[] cardList= null;
        try{
            line= "SHOW-CARD-REQUEST#" +projectName+"#"+cardName;

            os.println(line);
            os.flush();
            response=is.readLine();
            System.out.println("[CLIENT] Show project cards - parameters: "+projectName+"#"+cardName);
            System.out.println("[CLIENT] Server Response : "+response);

            String delims = "[#]+";
            cardList = response.split(delims);


        }
        catch(IOException e){
            e.printStackTrace();
            System.out.println("[CLIENT] Socket read Error");
        }

        if(response.compareTo("ERROR") == 0) return null;
        else return cardList;

    }

    public String[] showCards(String projectName){
        String response=null;
        String[] cardList= null;
        try{
            line= "SHOW-CARDS-REQUEST#" +projectName;

            os.println(line);
            os.flush();
            response=is.readLine();
            System.out.println("[CLIENT] Show project cards - parameters: "+projectName);
            System.out.println("[CLIENT] Server Response : "+response);

            String delims = "[#]+";
            cardList = response.split(delims);


        }
        catch(IOException e){
            e.printStackTrace();
            System.out.println("[CLIENT] Socket read Error");
        }

        if(response.compareTo("ERROR") == 0) return null;
        else return cardList;

    }

    public String[] showMembers(String projectName){

        String response=null;
        String[] memberList= null;
        try{
            line= "SHOW-PROJECT-MEMBERS#" +projectName;

            os.println(line);
            os.flush();
            response=is.readLine();
            System.out.println("[CLIENT] Show member of project - parameters: "+projectName);
            System.out.println("[CLIENT] Server Response : "+response);

            String delims = "[#]+";
            memberList = response.split(delims);


        }
        catch(IOException e){
            e.printStackTrace();
            System.out.println("[CLIENT] Socket read Error");
        }

        if(response.compareTo("ERROR") == 0) return null;
        else return memberList;
    }

    public Integer logout(String username, boolean logged) throws RemoteException {

        String[] projectWithMulticast = null;
        String[] projectWithoutMulticast = null;

        String response=null;
        try{
            if(!logged) username =  "";

            projectWithMulticast = this.listProjects(authenticatedUsername,true);
            projectWithoutMulticast = this.listProjects(authenticatedUsername,false);
            line= "LOGOUT-REQUEST#" +username;

            os.println(line);
            os.flush();
            response=is.readLine();
            System.out.println("[CLIENT] Logout request - parameters: "+username);
            System.out.println("[CLIENT] Server Response : "+response);



        }
        catch(IOException e){
            e.printStackTrace();
            System.out.println("[CLIENT] Socket read Error");
        }

        if(response.compareTo("ERROR") == 0) return 1;
        else if(response.compareTo("OK") == 0 && username.compareTo("") != 0){


            String projectMulticastIP;

            if(projectWithoutMulticast != null && projectWithMulticast != null) {
                for (String projectName : projectWithoutMulticast) {

                    MulticastSocket receiver = null;

                    int projectIndex = 1;
                    for (int i = 0; i < projectWithMulticast.length; i += 2) {
                        if (projectWithMulticast[i].compareTo(projectName) == 0) projectIndex = i + 1;
                    }


                    try {
                        receiver = this.multicastAdd(projectWithMulticast[projectIndex]);
                        projectMulticastIP = projectWithMulticast[projectIndex];
                    } catch (IOException e) {
                        e.printStackTrace();

                    }


                    ChatReceiverThread chatThread = null;
                    JTextArea messageListBoxArea = new JTextArea(1, 1); //Fake text area for history update
                    if (this.isListenerRegistered(projectName)) {

                        chatThread = this.getListenerThead(projectName);
                        if(chatThread.isAlive()) chatThread.interrupt();
                    }

                }
            }



            serverNotifier.unregisterForCallback(stubNotifier,username);
            return 0;

        } else if(response.compareTo("OK") == 0) return 0;
        return 1;
    }

    public Integer addCard(String projectName, String cardName, String descrizione){

        String response=null;
        try{
            line= "ADD-CARD-REQUEST#" +projectName + "#" + cardName +  "#"+descrizione;

            os.println(line);
            os.flush();
            response=is.readLine();
            System.out.println("[CLIENT] Create card request - parameters: "+projectName + "#" + cardName +  "#"+descrizione);
            System.out.println("[CLIENT] Server Response : "+response);



        }
        catch(IOException e){
            e.printStackTrace();
            System.out.println("[CLIENT] Socket read Error");
        }

        if(response.compareTo("CARD-DUPLICATE-ERROR") == 0) return 1;
        else if (response.compareTo("PROJECT-ELIMINATED") == 0) return 2;
        else if(response.compareTo("OK") == 0){

            return 0;

        }
        return 1;


    }
    public Integer cancelProject(String projectName){

        String response=null;
        try{
            line= "CANCEL-PROJECT-REQUEST#" +projectName;

            os.println(line);
            os.flush();
            response=is.readLine();
            System.out.println("[CLIENT] Cancel project request - parameters: "+projectName);
            System.out.println("[CLIENT] Server Response : "+response);



        }
        catch(IOException e){
            e.printStackTrace();
            System.out.println("[CLIENT] Socket read Error");
        }

        if(response.compareTo("CARD-STATUS-ERROR") == 0) return 1;
        else if(response.compareTo("OK") == 0){

            return 0;

        }
        return 1;

    }

    public String[] listUsers(){


        String[] allUsers = new String[userList.size()];

            int i = 0;
            Iterator it = userList.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
               allUsers[i] = pair.getKey() + " (" + pair.getValue() + ")";
                i++;
            }

        if(userList.size() == 0) return null;
        else return allUsers;

    }

    public String[] listOnlineUsers(){

        int onlineUsersCount = 0;
        Iterator ite = userList.entrySet().iterator();
        while (ite.hasNext()) {

            Map.Entry pairs = (Map.Entry) ite.next();
            if (pairs.getValue().toString().compareTo("online") == 0) {
                onlineUsersCount++;
            }
        }

        String[] onlineUsers = new String[onlineUsersCount];

        int i = 0;
        Iterator it = userList.entrySet().iterator();
        while (it.hasNext()) {

            Map.Entry pair = (Map.Entry)it.next();
            if(pair.getValue().toString().compareTo("online") == 0){
                onlineUsers[i] = pair.getKey().toString();
                i++;
            }
        }

        if(onlineUsersCount == 0) return null;
        else return onlineUsers;

    }


    public String[] listProjects(String username, boolean multicastGroup){

        String response=null;
        String[] projectListWithIP = null;
        String[] projectListWithoutIP = null;
        try{
            line= "LIST-PROJECT-REQUEST#" +username;

            os.println(line);
            os.flush();
            response=is.readLine();
            System.out.println("[CLIENT] List project - parameters: "+username);
            System.out.println("[CLIENT] Server Response : "+response);

            if(multicastGroup && response.compareTo("") != 0){

                String delims = "[#]+";
                projectListWithIP = response.split(delims);

                for (int i = 0; i < projectListWithIP.length; i+=2) {


                    System.out.println("[CLIENT] Project name: " + projectListWithIP[i] +  " multicast IP: " + projectListWithIP[i+1]);

                }


            } else if(response.compareTo("") != 0){

                String delims = "[#]+";
                projectListWithIP = response.split(delims);
                projectListWithoutIP = new String[(projectListWithIP.length/2)];
                for (int i = 0; i < projectListWithIP.length/2; i++) {

                    //if(i == 0) projectListWithoutIP[i] = projectListWithIP[i*2];
                    projectListWithoutIP[i] = projectListWithIP[i*2];
                    System.out.println("[CLIENT] Project name: " + projectListWithIP.length/2 +projectListWithoutIP[i]);

                }
            }


        }
        catch(IOException e){
            e.printStackTrace();
            System.out.println("[CLIENT] Socket read Error");
        }

        if(response.compareTo("ERROR") == 0) return null;
        else if(multicastGroup) return projectListWithIP;
        else return projectListWithoutIP;


    }

    public boolean isListenerRegistered(String project){

        if(!chatListener.containsKey(project)) return false;
        else return true;

    }

    public void registerListener(String project, ChatReceiverThread listener){
        chatListener.put(project,listener);
    }

    public ChatReceiverThread getListenerThead(String project){

        return chatListener.get(project);
    }

    public void chatHistorySaver(String projectName, String history) {

        if(!chatHistory.containsKey(projectName)) chatHistory.put(projectName,history);
        chatHistory.replace(projectName,chatHistory.get(projectName).concat(history));

    }

    public String getChatHistory(String projectName){

        if(!chatHistory.containsKey(projectName)) return "";
        else return chatHistory.get(projectName);
    }


    public boolean isMulticastRegistered(String multicastIP){

        if(multicastRegister.containsValue(multicastIP)) return true;
        return false;
    }
    public MulticastSocket multicastAdd(String multicastIP) throws IOException {


        if (multicastRegister.containsValue(multicastIP)) return multicastRegister.get(multicastIP);
        else {


            InetAddress group = null;
            int port = 6789;
            try {

                group = InetAddress.getByName(multicastIP);

            } catch (Exception e) {

                System.out.println("Uso:java TimeClient multicast_address port [server default setting: 228.5.6.7:6789] ");
                System.exit(1);
            }

            MulticastSocket chatReceiver = null;

            try {
                chatReceiver = new MulticastSocket(port);
                //ms.setTimeToLive(1);
                chatReceiver.joinGroup(group);

                multicastRegister.put(multicastIP,chatReceiver);
                return chatReceiver;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

       return null;
    }

    public Integer createProject(String name){


        String response=null;
        try{
            line= "CREATE-PROJECT-REQUEST#" +name;

            os.println(line);
            os.flush();
            response=is.readLine();
            System.out.println("[CLIENT] Create new project request - parameters: "+name);
            System.out.println("[CLIENT] Server Response : "+response);



        }
        catch(IOException e){
            e.printStackTrace();
            System.out.println("[CLIENT] Socket read Error");
        }

        if(response.compareTo("PROJECT-TITLE-ERROR") == 0) return 1;
        else if(response.compareTo("OK") == 0) return 0;
        return 2;
    }


    public String login(String username, String password) throws IOException, NotBoundException {


        String response=null;
        try{
            line= "LOGIN-REQUEST#" +username +  "#" + password;

                os.println(line);
                os.flush();
                response=is.readLine();
            System.out.println("[CLIENT] Login request - parameters: "+username + " **********");
            System.out.println("[CLIENT] Server Response : "+response);



            String delims = "[#]+";
            String[] tokens = response.split(delims);
            if(tokens[0].compareTo("USERNAME-ERROR") != 0 && tokens[0].compareTo("PASSWORD-ERROR") != 0 && tokens[0].compareTo("ONLINE-ERROR") != 0) {


                System.out.println("[CLIENT - WORTH-UPDATE-SERVICE] Looking for registry service on port " + this.port);
                Registry registry = LocateRegistry.getRegistry(this.port);
                serverNotifier =(ServerInterface) registry.lookup("WORTH-UPDATE-SERVICE");

                /* si registra per la callback */
                System.out.println("[CLIENT - WORTH-UPDATE-SERVICE] Registering for callback on WORTH-UPDATE-SERVICE");


                stubNotifier = (UserUpdateNotify) UnicastRemoteObject.exportObject(this, 0);
                serverNotifier.registerForCallback(stubNotifier,username);

                for (int i = 1; i < tokens.length; i = i + 2) {
                    if (tokens[i-1] != null && tokens[i] != null) userList.put(tokens[i-1], tokens[i]);
                    System.out.println("tokens[" + i + "] = " + tokens[i-1] + "tokens[" + i + "+1] = " + tokens[i]);
                }


                authenticatedUsername = username;
                userList.replace(username, "online");

                String[] projectWithMulticast = this.listProjects(authenticatedUsername,true);
                String[] projectWithoutMulticast = this.listProjects(authenticatedUsername,false);
                String projectMulticastIP;

                if(projectWithoutMulticast != null && projectWithMulticast != null) {
                    for (String projectName : projectWithoutMulticast) {

                        MulticastSocket receiver = null;

                        int projectIndex = 1;
                        for (int i = 0; i < projectWithMulticast.length; i += 2) {
                            if (projectWithMulticast[i].compareTo(projectName) == 0) projectIndex = i + 1;
                        }


                        try {
                            receiver = this.multicastAdd(projectWithMulticast[projectIndex]);
                            projectMulticastIP = projectWithMulticast[projectIndex];
                        } catch (IOException e) {
                            e.printStackTrace();

                        }


                        ChatReceiverThread chatThread = null;
                        JTextArea messageListBoxArea = new JTextArea(1, 1); //Fake text area for history update
                        if (!this.isListenerRegistered(projectName)) {

                            chatThread = new ChatReceiverThread(receiver, messageListBoxArea, this, projectName);
                            this.registerListener(projectName, chatThread);
                            chatThread.start();
                        } else this.getListenerThead(projectName).updateBoxArea(messageListBoxArea);

                    }
                }

                }



        }
        catch(IOException e){
            e.printStackTrace();
            System.out.println("Socket read Error");
        }


        return response;
    }


    public Integer registerRequest(String username, String password){


        WorthRegisterServiceInterface serverObject;
        Remote RemoteObject;
        Integer response = -11;
        try {


            Registry r = LocateRegistry.getRegistry(port);
            RemoteObject = r.lookup("WORTH-SERVER");
            serverObject = (WorthRegisterServiceInterface) RemoteObject;

            response = serverObject.register(username, password);

            return response;



        }
        catch (Exception e) {
            System.out.println("[CLIENT ERROR]: response " + response + e.toString() + " - (" + e.getMessage() +")\n");
            e.printStackTrace();
        }

        return -1;

    }

    @Override
    public void notifyEvent(String username, String status) throws RemoteException {

        if(userList.containsKey(username)) userList.replace(username, status);
        else userList.put(username,status);
        String returnMessage = "[CLIENT] Update event received from server with data: " + username +  " - " + status;
        System.out.println(returnMessage);
    }
}

