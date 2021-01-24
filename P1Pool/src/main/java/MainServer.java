import sun.misc.Signal;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.*;             // Classes and support for RMI
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;     // Classes and support for RMI servers
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


public class MainServer extends RemoteServer implements WorthRegisterServiceInterface {

    public ServerUpdateNotify serverUpdate = null;
    private DataManager dataContainer = null;
    private static int port = 2020;
    private ThreadPoolExecutor executor;

    public MainServer(int port) throws RemoteException {

        //Launch RMI registration service for clients
        registerServiceLauncher(port, this);

        //Create update notifier for RMI callback on user event
        serverUpdate = new ServerUpdateNotify();

        //Launch RMI notifier service for RMI callback on user event
        callbackRegisterLauncher(port,serverUpdate);

        //Create new instance of DataManager, data core project
        dataContainer = new DataManager(serverUpdate);

        //Create new thread pool for submitting client thread manager instance
        executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        //Signal register for shutdown operations
        Signal.handle(new Signal("INT"),  // SIGINT
                signal -> {

                    try {
                        dataContainer.shutdown();
                    } catch (IOException e) {
                        System.out.println("[SERVER SHUTDOWN SERVICE] FATAL ERROR DURING DATA SAVING, FAIL TO SAVE!");
                        e.printStackTrace();
                    }
                    executor.shutdown();
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
                //Accept new client
                s= ss2.accept();

                //Delegate client management to ServerThread instance
                ServerThread clientThread = new ServerThread(s,dataContainer);
                //Submit serverThread instance to threadPool
                executor.execute(clientThread);

            }

            catch(Exception e){
                e.printStackTrace();
                System.exit(1);
            }
        }


    }


    public static void main(String args[]) throws RemoteException {

        //Create new server instance on port
        MainServer mainServer = new MainServer(port);


    }

    public void callbackRegisterLauncher(int port, ServerUpdateNotify server){


        try{

            //Export event notify object to port 39000
            ServerInterface stub=(ServerInterface) UnicastRemoteObject.exportObject (server,39000);
            Registry registry = LocateRegistry.getRegistry(port);
            registry.bind ("WORTH-UPDATE-SERVICE", stub);
            System.out.println("[RMI CALLBACK NOTIFY SERVICE] Notify service launched on port "+ 39000);



        } catch (Exception e) {
            System.out.println("[RMI CALLBACK NOTIFY SERVICE] ERRORE: " +e);
            System.exit(1);
        }
    }

    private void registerServiceLauncher(int port, MainServer registryInstance){
        try {

            //Export registration object to port 0 (JVM choice)
            WorthRegisterServiceInterface stub = (WorthRegisterServiceInterface) UnicastRemoteObject.exportObject(registryInstance, 0);

            LocateRegistry.createRegistry(port);
            Registry r = LocateRegistry.getRegistry(port);
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
    public synchronized Integer register(String username, String password) throws RemoteException {

        /*#### ERROR CODEX
        * error = 0 --> OK
        * error = 1 --> DUPLICATED USERNAME
        * error 2 --> SAME PASSWORD AND USERNAME
        * */

        System.out.println("[SERVER] Registered new user: " + username +  " - psw: **********");
        Integer error = 0;

        //Check if username is already taken
        if(dataContainer.registerCheckUsername(username) == 1) error = 1;
        //Check if username and password are equal
        else if(username.compareTo(password) == 0) error = 2;
        else {
            //Register new user in DataManager structure
            dataContainer.register(username,password);
            //Do callback to notify new user (offline)
            serverUpdate.update(username, "offline");

        }

        return error;
    }

}




