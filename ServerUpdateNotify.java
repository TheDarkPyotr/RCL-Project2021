import java.rmi.*;
import java.rmi.server.*;
import java.util.*;

public class ServerUpdateNotify extends RemoteObject implements ServerInterface {

    /* lista dei client registrati */
    private HashMap<String,UserUpdateNotify> clients;

    /* crea un nuovo servente */
    public ServerUpdateNotify()throws RemoteException {
        super();
        clients = new HashMap<String,UserUpdateNotify>( );
    }

    public synchronized void registerForCallback (UserUpdateNotify ClientInterface, String username) throws RemoteException {
        if (!clients.containsValue(ClientInterface)) {
            clients.put(username,ClientInterface);
            System.out.println("[SERVER - RMI CALLBACK NOTIFY SERVICE] New client registered on WORTH-UPDATE-SERVICE" );
        }
    }


    /* annulla registrazione per il callback */
        public synchronized void unregisterForCallback(UserUpdateNotify Client, String username) throws RemoteException {
                    if (clients.remove(username,Client)) System.out.println("[SERVER - RMI CALLBACK NOTIFY SERVICE] Client unregistered from update service");
                    else { System.out.println("[SERVER - RMI CALLBACK NOTIFY SERVICE] ERROR: Unable to unregister client");
                    }
        }

        public void update(String username, String status) throws RemoteException {
            doCallbacks(username,status);
        }


        private synchronized void doCallbacks(String username, String status) throws RemoteException{
                System.out.println("[SERVER - RMI CALLBACK NOTIFY SERVICE] Starting callback on event update ");
                Iterator i = clients.entrySet().iterator( );
            while (i.hasNext()) {
                Map.Entry pair = (Map.Entry)i.next();
                    UserUpdateNotify client =(UserUpdateNotify) pair.getValue();
                    client.notifyEvent(username,status);
            }
            System.out.println("[SERVER - RMI CALLBACK NOTIFY SERVICE] Callback successfull completed");
        }
}