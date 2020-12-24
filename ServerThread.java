import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.*;

class ServerThread extends Thread{

    String request=null;
    String response = null;
    BufferedReader is = null;
    PrintWriter os=null;
    Socket s=null;
    HashMap<String,String> list = null;
    String[] tokens = null;
    String authenticatedUsername;
    private DataManager data = null;


    ArrayList<Project> projectList;

    public ServerThread(Socket s, DataManager dataContainer){
        this.s=s;
        this.data = dataContainer;
    }

    public void run() {



        try {
            is = new BufferedReader(new InputStreamReader(s.getInputStream()));
            os = new PrintWriter(s.getOutputStream());
        } catch (IOException e) {
            System.out.println("IO error in server thread");
        }

        request = "LOOP";
        while (request.compareTo("EXIT") != 0) {


            try {

                    request = is.readLine();
                    String delims = "[#]+";
                    tokens = request.split(delims);


            } catch (IOException e) {

            }


            String choice = tokens[0];
            switch(choice) {
                case "LOGIN-REQUEST":

                    list = this.data.getUserList();
                    System.out.println("[SERVER] Received " + choice + " request from " + s.getInetAddress() + " with data: " + list.toString());
                    response =  "";


                    if (!list.containsKey(tokens[1])) response = "USERNAME-ERROR";
                    else if (list.get(tokens[1]).compareTo(tokens[2]) != 0) response = "PASSWORD-ERROR";
                    else {

                        Iterator it = this.data.getUserOnlineList().entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry)it.next();
                           response = response.concat(pair.getKey() + "#" + pair.getValue()).concat("#");

                        }

                        authenticatedUsername = tokens[1];
                        try {
                            this.data.setUserStatus(authenticatedUsername, "online");
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }

                    }


                    os.println(response);
                    os.flush();
                    System.out.println("[SERVER] Response to login request from" + s.getInetAddress() + " with: " + response);

                break;


                case "CREATE-PROJECT-REQUEST":

                    System.out.println("[SERVER] Received " + choice + " request from " + s.getInetAddress() + " with data: ");

                    response = "OK";
                    //if (!list.containsKey(tokens[1])) response = "USERNAME-ERROR";
                    //else if (list.get(tokens[1]).compareTo(tokens[2]) != 0) response = "PASSWORD-ERROR";
                    //else response = list.toString();


                    if (this.data.newProject(tokens[1],authenticatedUsername) == 1)
                        response = "PROJECT-TITLE-ERROR";

                    os.println(response);
                    os.flush();
                    System.out.println("[SERVER] Response to create project request from" + s.getInetAddress() + " with: " + response);

                    break;


                case "LOGOUT-REQUEST":

                    System.out.println("[SERVER] Received " + choice + " request from " + s.getInetAddress() + " with data: ");

                    if (data.getUserOnlineList().get(tokens[1]).compareTo("offline") == 0) response = "ERROR";
                    else {
                        response = "OK";
                        try {
                            data.setUserStatus(tokens[1],"offline");
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    os.println(response);
                    os.flush();
                    System.out.println("[SERVER] Response to logout request from" + s.getInetAddress() + " with: " + response);
                    request =  "EXIT";


                    break;

                case "SHOW-PROJECT-MEMBERS":

                    System.out.println("[SERVER] Received " + choice + " request from " + s.getInetAddress() + " with data: " + tokens[1]);
                    response = "";
                    ArrayList<String> projectMembers = data.getProjectMembers(tokens[1]);
                    if(projectMembers != null){
                        for (String name : projectMembers)
                            response = response.concat(name).concat("#");
                    }

                    os.println(response);
                    os.flush();
                    System.out.println("[SERVER] Response to  " + request + " request from" + s.getInetAddress() + " with: " + response);

                    break;


                case "LIST-PROJECT-REQUEST":

                    System.out.println("[SERVER] Received " + choice + " request from " + s.getInetAddress() + " with data: " + tokens[1]);
                    response = "";
                    projectList = data.getProjectList(authenticatedUsername);
                    if(projectList != null){
                        for (Project pr : projectList)
                           response = response.concat(pr.getProjectName()).concat("#");
                    }

                    os.println(response);
                    os.flush();
                    System.out.println("[SERVER] Response to  " + request + " request from" + s.getInetAddress() + " with: " + response);

                    break;


                default:
                  System.out.println("SERVER SIDE ERROR - QUIT ALL! ");
                    System.exit(1);
                    break;

            }
        } //while end

        try {
            s.close();
            System.out.println("[SERVER] Shutting down connection with " + s.getInetAddress());
        } catch (IOException e) {
            System.out.println("[SERVER] Error during shutdown connection with " + s.getInetAddress());
            e.printStackTrace();
        }
    }
}