import java.rmi.RemoteException;
import java.util.*;

public class DataManager {

    private HashMap<String,String> allUserList;
    private HashMap<String,String> allUserListStatus;
    private ServerUpdateNotify serverNotifier = null;

    private ArrayList<Project> projectList = new ArrayList<Project>();

    // Returns a synchronized (thread-safe) list backed by the specified
    // list. In order to guarantee serial access, it is critical that all
    // access to the backing list is accomplished through the returned list.
    List<Project> synchronizedList = Collections.synchronizedList(projectList);


    public DataManager(ServerUpdateNotify serverNotifier){

        //Load initial data from file system
        allUserList = new HashMap<>();
        allUserListStatus = new HashMap<>();
        this.serverNotifier = serverNotifier;


    }

    public int registerCheckUsername(String username){

        synchronized (allUserList){
            if(!allUserList.isEmpty()) {
                if(allUserList.get(username) != null) return 1;
                return 0;
            }

            return 0;
        }

    }

    public void register(String username, String password){

        System.out.println("[SERVER - DATA MANAGER] New user: " + username +  " " + password);
        allUserList.put(username,password);
        allUserListStatus.put(username, "offline");
        System.out.println("[SERVER - DATA MANAGER] User list: "+ allUserList.toString());



    }

    public HashMap<String,String> getUserList(){

        synchronized (allUserList) {
            return this.allUserList;
        }

    }

    public HashMap<String,String> getUserOnlineList(){
        synchronized (allUserListStatus) {
            return this.allUserListStatus;
        }
    }

    public void setUserStatus(String username, String status) throws RemoteException {

        synchronized (allUserListStatus) {
            allUserListStatus.replace(username, status);
        }
        serverNotifier.update(username,status);


    }

    public ArrayList<Project> getProjectList(String username){

        synchronized(synchronizedList){

            ArrayList<Project> userProject = new ArrayList<>();
            for (Project pr: synchronizedList) {
                    if(pr.getMemberList().contains(username)) userProject.add(pr);
            }

            return userProject;

        }


    }

    public ArrayList<String> getProjectMembers(String project){

        synchronized (synchronizedList){
            return synchronizedList.get(synchronizedList.indexOf(new Project(project,  ""))).getMemberList();
        }
    }
    public synchronized int newProject(String name, String authorName){


            Project newProject = new Project(name,authorName);
            for (Project pr : synchronizedList) if(pr.equals(newProject)) return 1;

            synchronizedList.add(newProject);
            return 0;

    }


}

