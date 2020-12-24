import java.rmi.*;

public interface ServerInterface extends Remote {

    /* registrazione per la callback */
    public void registerForCallback(UserUpdateNotify ClientInterface, String username) throws RemoteException;

    /* cancella registrazione per la callback */

    public void  unregisterForCallback (UserUpdateNotify ClientInterface, String username) throws RemoteException;

}