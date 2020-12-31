import sun.misc.Signal;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.*;             // Classes and support for RMI
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;     // Classes and support for RMI servers
import java.util.*;



public class Server extends RemoteServer implements WorthRegisterServiceInterface {

    public HashMap<String, String> userList = null;
    public ArrayList<Project> projectList;
    public ServerUpdateNotify serverUpdate = null;
    private  DataManager dataContainer = null;




    public Server(int port) throws RemoteException {

        registerServiceLauncher(port, this);
        serverUpdate = new ServerUpdateNotify( );
        callbackRegisterLauncher(port,serverUpdate);

        dataContainer = new DataManager(serverUpdate);


        Signal.handle(new Signal("INT"),  // SIGINT
                signal -> {

                    try {
                        dataContainer.shutdown();
                    } catch (IOException e) {
                        System.out.println("[SERVER SHUTDOWN SERVICE] FATAL ERROR DURING DATA SAVING, FAIL TO SAVE!");
                        e.printStackTrace();
                    }
                    System.exit(0);

                });



        Socket s = null;
        ServerSocket ss2=null;
        System.out.println("[MAIN SERVER] Listening connection on port 2021");
        try{
            ss2 = new ServerSocket(2021);

        }
        catch(IOException e){
            e.printStackTrace();
            System.out.println("[MAIN SERVER] ERROR: Error during establishing connection on 2021 port");


        }

        while(true){
            try{
                s= ss2.accept();
                ServerThread clientThread = new ServerThread(s,dataContainer);
                clientThread.start();
            }

            catch(Exception e){
                e.printStackTrace();
                System.exit(1);
            }
        }


    }


    public static void main(String args[]) throws RemoteException {


        int port = 0;
        try{
            port = Integer.parseInt(args[0]);
        }catch(NullPointerException e){
            e.printStackTrace();
        }
        Server mainServer = new Server(port);


    }

    public void callbackRegisterLauncher(int port, ServerUpdateNotify server){


        try{ /*registrazione presso il registry */

            ServerInterface stub=(ServerInterface) UnicastRemoteObject.exportObject (server,39000);

            Registry registry=LocateRegistry.getRegistry(port);
            registry.bind  ("WORTH-UPDATE-SERVICE", stub);

            System.out.println("[RMI CALLBACK NOTIFY SERVICE] Notify service launched on port "+ 39000);



        } catch (Exception e) {
            System.out.println("[RMI CALLBACK NOTIFY SERVICE] ERRORE: " +e);
            System.exit(1);
        }
    }

    private void registerServiceLauncher(int port,Server registryInstance){
        try {

            /* Esportazione dell'Oggetto */
            WorthRegisterServiceInterface stub = (WorthRegisterServiceInterface) UnicastRemoteObject.exportObject(registryInstance, 0);

            LocateRegistry.createRegistry(port);
            Registry r = LocateRegistry.getRegistry(port);
            /* Pubblicazione dello stub nel registry */
            r.rebind("WORTH-SERVER", stub);
            System.out.println("[RMI REGISTRATION SERVICE] Register service launched on port "+ port);

        }
        catch (RemoteException e) {

            System.out.println("[SERVER LAUNCH ERROR]: " + e.toString());
            e.printStackTrace();
            System.exit(1);
        } catch (NullPointerException er){

            System.out.println("[SERVER LAUNCH ERROR]: " + er.toString());
            er.printStackTrace();
            System.exit(1);
        }

    }
    public Integer register(String username, String password) throws RemoteException {

        /*#### ERROR CODEX
        * error = 0 --> OK
        * error = 1 --> DUPLICATED USERNAME
        * error 2 --> SAME PASSWORD AND USERNAME*/
        System.out.println("[SERVER] Registered new user: " + username +  " - psw: **********");
        Integer error = 0;

        if(dataContainer.registerCheckUsername(username) == 1) error = 1;
        else if(username.compareTo(password) == 0) error = 2;
        else {
            dataContainer.register(username,password);
            serverUpdate.update(username, "offline");


        }

        return error;
    }

}




