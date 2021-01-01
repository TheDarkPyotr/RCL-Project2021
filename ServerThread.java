import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
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
    private Integer status = 1;


    ArrayList<Project> projectList;

    public ServerThread(Socket s, DataManager dataContainer){

        this.s = s;
        this.data = dataContainer;
    }

    public void run() {

        System.out.println("[SERVER THREAD " + currentThread().getId()+  "] New connection established with " + s.getInetAddress());


        try {
            is = new BufferedReader(new InputStreamReader(s.getInputStream()));
            os = new PrintWriter(s.getOutputStream());

        } catch (IOException e) {
            System.out.println("IO error in server thread");
            System.exit(1);
        }

        request = "LOOP";
        while (request.compareTo("EXIT") != 0) {


            try {
                    request = is.readLine();
                    String delims = "[#]+";
                    tokens = request.split(delims);

            } catch (IOException e) {

                System.out.println("[SERVER THREAD " + currentThread().getId()+  "] ERROR: request loop error!");
                e.printStackTrace();

            }


            String choice = tokens[0];
            switch(choice) {
                case "LOGIN-REQUEST":

                    System.out.println("[SERVER THREAD " + currentThread().getId()+ "]  Received " + choice + " request from " + s.getInetAddress() + " with data: " + tokens[1]);
                    response =  "";
                    try {

                        status = data.login(tokens[1], tokens[2]);
                        if (status == 1) response = "USERNAME-ERROR";
                        else if (status == 2) response = "PASSWORD-ERROR";
                        else if(status == 3) response = "ONLINE-ERROR";
                        else if(status == 4) throw new NullPointerException();
                        else {

                                Iterator it = this.data.getUserOnlineList().entrySet().iterator();
                                while (it.hasNext()) {
                                     Map.Entry pair = (Map.Entry)it.next();
                                     response = response.concat(pair.getKey() + "#" + pair.getValue()).concat("#");
                                }

                                authenticatedUsername = tokens[1];
                                this.data.setUserStatus(authenticatedUsername, "online");

                            }
                    } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                    os.println(response);
                    os.flush();
                    System.out.println("[SERVER THREAD " + currentThread().getId()+ "] Response to login request from" + s.getInetAddress() + " with: " + response +  "\n");

                break;


                case "LOGOUT-REQUEST":

                    System.out.println("[SERVER] Received " + choice + " request from " + s.getInetAddress() + " with data: ");

                    //Close only TCP Connection, for non-authenticated user
                    if(tokens.length == 1){
                        System.out.println("[SERVER THREAD " + currentThread().getId()+ "] Close TCP connection with non authenticated user");
                        response = "OK";
                    } else{
                         // Close TCP connection and change user status, for authenticated user
                        if (data.getUserOnlineList().get(tokens[1]).compareTo("offline") == 0) response = "ERROR";
                        else {
                            response = "OK";
                            data.setUserStatus(tokens[1], "offline");
                        }
                    }

                    os.println(response);
                    os.flush();
                    System.out.println("[SERVER] Response to logout request from" + s.getInetAddress() + " with: " + response+  "\n");
                    request =  "EXIT";


                    break;

                case "SHOW-PROJECT-MEMBERS":

                    System.out.println("[SERVER] Received " + choice + " request from " + s.getInetAddress() + " with data: " + tokens[1]);
                    response = "";
                    ArrayList<String> projectMembers = data.getProjectMembers(tokens[1]);
                    if(projectMembers != null){
                        for (String name : projectMembers)
                            response = response.concat(name).concat("#");
                    } else response = "ERROR";

                    os.println(response);
                    os.flush();
                    System.out.println("[SERVER] Response to  " + request + " request from" + s.getInetAddress() + " with: " + response+  "\n");

                    break;

                case  "CANCEL-PROJECT-REQUEST":

                    System.out.println("[SERVER] Received " + choice + " request from " + s.getInetAddress() + " with data: " + tokens[1]);
                    response = "";

                    status = data.cancelProject(tokens[1]);
                    if(status == 0) response =  "OK";
                    else if(status == 1) response = "CARD-STATUS-ERROR";
                    else response = "PROJECT-ELIMINATED";
                    os.println(response);
                    os.flush();
                    System.out.println("[SERVER] Response to  " + request + " request from" + s.getInetAddress() + " with: " + response+  "\n");


                    break;

                case  "ADD-CARD-REQUEST":

                    System.out.println("[SERVER] Received " + choice + " request from " + s.getInetAddress() + " with data: " + tokens[1]+  "\n");
                    response = "";
                    status = data.addCard(tokens[1], tokens[2], tokens[3]);
                    if(status == 0) response =  "OK";
                    else if(status == 2) response = "PROJECT-ELIMINATED";
                    else response = "CARD-DUPLICATE-ERROR";
                    os.println(response);
                    os.flush();
                    System.out.println("[SERVER] Response to  " + request + " request from" + s.getInetAddress() + " with: " + response+  "\n");


                    break;

                case  "ADD-MEMBER-REQUEST":

                    System.out.println("[SERVER] Received " + choice + " request from " + s.getInetAddress() + " with data: " + tokens[1] +  " " + tokens[2]);
                    response = "";

                    status = data.addMember(tokens[1],tokens[2]);
                    if(status == 0) response =  "OK";
                    else if(status == 1) response = "ALREADY-MEMBER";
                    else response = "ERROR";
                    os.println(response);
                    os.flush();
                    System.out.println("[SERVER] Response to  " + choice + " request from" + s.getInetAddress() + " with: " + response+  "\n");


                    break;

                case  "SHOW-CARDS-REQUEST":
                    System.out.println("[SERVER] Received " + choice + " request from " + s.getInetAddress() + " with data: " + tokens[1]);
                    response = "";

                    String[] cardList = data.showCards(tokens[1]);
                    if(cardList == null) response = "NO-CARDS-ERROR";
                    else{
                        for (int i = 0; i < cardList.length; i++)
                        response = response.concat(cardList[i]).concat("#");
                    }
                    os.println(response);
                    os.flush();
                    System.out.println("[SERVER] Response to  " + request + " request from" + s.getInetAddress() + " with: " + response+  "\n");

                    break;

                case  "SHOW-CARD-REQUEST":
                    System.out.println("[SERVER] Received " + choice + " request from " + s.getInetAddress() + " with data: " + tokens[1]);
                    response = "";

                    String[] cardInfo = data.showCard(tokens[1],tokens[2]);
                    if(cardInfo == null) response = "CARD-ERROR";
                    else{

                            response = response.concat(cardInfo[0]).concat("#").concat(cardInfo[1]);
                    }
                    os.println(response);
                    os.flush();
                    System.out.println("[SERVER] Response to  " + request + " request from" + s.getInetAddress() + " with: " + response+  "\n");

                    break;

                case  "MOVE-CARD-REQUEST":
                    System.out.println("[SERVER] Received " + choice + " request from " + s.getInetAddress() + " with data: " + tokens[1] + tokens[2] + tokens[3]);
                    response = "";

                        /* STATUS CODEX
            return 0 --> ok
            return 1 --> initial and destination list equals
            return 2 --> error
            return 3 --> internal error

         */

                    Integer status = data.moveCard(tokens[1],tokens[2],tokens[3]);
                    switch(status){
                        case 0:
                            response = "OK";
                            try {
                                chatStateUpdater(tokens[1],tokens[2],tokens[3]);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        case 1:
                            response = "COINCIDENT-LISTS";
                            break;
                        case 2:
                            response = "STATE-VIOLATION";
                            break;
                        case 3:
                            response =  "INTERNAL-ERROR";
                            break;
                        case 4:
                            response =  "PROJECT-ELIMINATED";
                            break;
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
                           response = response.concat(pr.getProjectName()).concat("#").concat(pr.getMulticastIP()).concat("#");
                    }

                    os.println(response);
                    os.flush();
                    System.out.println("[SERVER] Response to  " + request + " request from" + s.getInetAddress() + " with: " + response);

                    break;

                case "CREATE-PROJECT-REQUEST":

                    System.out.println("[SERVER] Received " + choice + " request from " + s.getInetAddress() + " with data: " + tokens[1]);

                    response = "OK";

                    if (this.data.newProject(tokens[1],authenticatedUsername) == 1)
                        response = "PROJECT-TITLE-ERROR";

                    os.println(response);
                    os.flush();
                    System.out.println("[SERVER] Response to create project request from" + s.getInetAddress() + " with: " + response +  "\n");

                    break;


                case "CARD-HISTORY-REQUEST":

                    System.out.println("[SERVER] Received " + choice + " request from " + s.getInetAddress() + " with data: " + tokens[1]);
                    response = "";
                    String[] history = data.getCardHistory(tokens[1],tokens[2]);
                    if(history != null){
                        for (String s : history)
                            response = response.concat(s).concat("#");
                    } else response =  "PROJECT-ELIMINATED";

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

            System.out.println("[SERVER THREAD " + currentThread().getId()+ "]] Shutting down connection with " + s.getInetAddress());
        } catch (IOException e) {
            System.out.println("[SERVER] Error during shutdown connection with " + s.getInetAddress());
            e.printStackTrace();
        }

        System.out.println("[SERVER THREAD " + currentThread().getId()+ "]  Execution terminated, bye!");
    }

    public void chatStateUpdater(String projectName, String cardName, String destinationList) throws IOException {

        String projectIP = data.getProjectMulticastIP(projectName);


        InetAddress group = null;
        int port = 6789;
        try{

            group = InetAddress.getByName(projectIP);


        } catch(Exception e){

            System.out.println("[SERVER] Error: impossible update project " + projectName +  " multicast chat!\n");

        }


        MulticastSocket ms=null;

        try{
            ms = new MulticastSocket(port);
            //ms.setTimeToLive(1);
            ms.joinGroup(group);

            byte[] data;
            data = ("[SYSTEM] " + authenticatedUsername + " ha spostato la card " + cardName + " nella lista " + destinationList +  "").getBytes();
            DatagramPacket dpsend = new DatagramPacket(data, data.length, group, port);


            try{
                    int ttl = ms.getTimeToLive();
                    ms.setTimeToLive(10);
                    ms.send(dpsend);
                    ms.setTimeToLive(ttl);

                }catch(SocketException es){es.printStackTrace();}
                catch (IOException ex){
                    System.out.println (ex);
                }


        }finally{
            if (ms!= null) {
                try {
                    ms.leaveGroup(group);
                    ms.close();
            } catch (IOException ex){}
            }
        }
    }
}