import java.rmi.*;

public interface UserUpdateNotify extends Remote {

    /* Metodo invocato dal  server per notificare un evento ad un client remoto. */
    public void notifyEvent(String username,String status) throws RemoteException;

}