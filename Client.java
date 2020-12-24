import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
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

    public Client(int port) throws UnknownHostException, IOException {

        super();
        address=InetAddress.getLocalHost();
        this.port = port;


            s1=new Socket(address, 2021); // You can use static final constant PORT_NUM
            br= new BufferedReader(new InputStreamReader(System.in));
            is=new BufferedReader(new InputStreamReader(s1.getInputStream()));
            os= new PrintWriter(s1.getOutputStream());
            userList = new HashMap<>();



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

    public Integer logout(String username) throws RemoteException {


        String response=null;
        try{
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
        else if(response.compareTo("OK") == 0){

            serverNotifier.unregisterForCallback(stubNotifier,username);
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
                onlineUsers[i] = pair.getKey() + " (" + pair.getValue()+ ")";
                i++;
            }
        }

        if(onlineUsersCount == 0) return null;
        else return onlineUsers;

    }

    public String[] listProjects(String username){

        String response=null;
        String[] projectList = null;
        try{
            line= "LIST-PROJECT-REQUEST#" +username;

            os.println(line);
            os.flush();
            response=is.readLine();
            System.out.println("[CLIENT] List project - parameters: "+username);
            System.out.println("[CLIENT] Server Response : "+response);

            String delims = "[#]+";
            projectList = response.split(delims);





        }
        catch(IOException e){
            e.printStackTrace();
            System.out.println("[CLIENT] Socket read Error");
        }

        if(response.compareTo("ERROR") == 0) return null;
        else return projectList;


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
            if(tokens[0].compareTo("USERNAME-ERROR") != 0 && tokens[0].compareTo("PASSWORD-ERROR") != 0) {


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
                System.out.println("[CLIENT] Local copy or people around: " + userList.toString());
                userList.replace(username, "online");

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

