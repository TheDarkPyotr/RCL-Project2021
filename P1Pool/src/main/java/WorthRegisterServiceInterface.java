import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WorthRegisterServiceInterface extends Remote{

    /*

    register(nickUtente, password): per inserire un nuovo utente, il server mette a disposizione una operazione
    di registrazione di un utente. Il server risponde con un codice che può indicare l’avvenuta registrazione,
    oppure, se il nickname è già presente, o se la password è vuota, restituisce un messaggio d’errore. Come
    specificato in seguito, le registrazioni sono tra le informazioni da persistere.

     */

    Integer register(String username, String password) throws RemoteException;

}
