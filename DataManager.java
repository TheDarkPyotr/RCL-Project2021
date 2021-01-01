import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DataManager {

    private HashMap<String, String> allUserList;
    private HashMap<String, String> allUserListStatus;
    private ServerUpdateNotify serverNotifier = null;


    private File userJSON = null;
    private File projectJSONMainFolder = null;
    private final ObjectMapper mapper; //ObjectMapper for json

    private static String STORAGE_DIR = "./storage";


    private HashMap<String, Project> projectList = null;


    public DataManager(ServerUpdateNotify serverNotifier) {

        //Load initial data from file system
        this.mapper = new ObjectMapper();
        projectList = new HashMap<>();
        allUserListStatus = new HashMap<>();

        this.dataRestore(new File(STORAGE_DIR));

        this.serverNotifier = serverNotifier;


    }

    public synchronized void shutdown() throws IOException {

        System.out.println("[SERVER DATA MANAGER] Storing data");
            this.dataWriter(new File(STORAGE_DIR));
        System.out.println("[SERVER DATA MANAGER] Data saved!");

    }

    public int login(String username, String password){

        int response = 0;
        if(username == null || password == null) response = 4;
        else {
                synchronized (allUserList) {
                    if (!allUserList.containsKey(username)) response = 1;
                    else if (allUserList.get(username).compareTo(password) != 0) response = 2;
                    else if (this.getUserOnlineList().containsKey(username) && this.getUserOnlineList().get(username).compareTo("online") == 0) response = 3;
                }
            }

        return response;
    }




    public void dataWriter(File restoreDir) throws IOException {

        String jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(allUserList);

        if (!restoreDir.exists()) restoreDir.mkdir();
        if (restoreDir.isDirectory()) {
            try {

                if (!userJSON.exists()) {

                    userJSON.createNewFile();

                    // convert map to JSON file
                    mapper.writeValue(userJSON, allUserList);

                    System.out.println(jsonResult);
                } else {
                    mapper.writeValue(userJSON, allUserList);
                }

                projectJSONMainFolder = new File(restoreDir + "/projects");
                if (!projectJSONMainFolder.exists()) projectJSONMainFolder.mkdir();
                if (projectJSONMainFolder.isDirectory()) {

                    //Elimino cartella e file di progetti eliminati
                    File[] projectFolderList = projectJSONMainFolder.listFiles();

                    for (File projectFolder : projectFolderList) {

                        if (projectFolder.isDirectory()) {
                            if (!projectList.containsKey(projectFolder.getName())) {

                                for (File file : projectFolder.listFiles()) file.delete();

                                projectFolder.delete();
                            }
                        }
                    }

                    for (Project project : projectList.values()) {

                        File projectDir = new File(projectJSONMainFolder + "/" + project.getProjectName());
                        if (!projectDir.exists()) projectDir.mkdir();
                        if (projectDir.isDirectory()) {

                            File projectFile = new File(projectDir + "/project.txt");
                            if (!projectFile.exists()) projectFile.createNewFile();
                            mapper.writeValue(projectFile, project.getProjectName() + "#" + project.getMemberList() + "#" + project.getMulticastIP());

                            for (Card c : project.getToDoList()) {

                                File card = new File(projectDir + "/" + c.getTitolo() + ".json");
                                if (!card.exists()) card.createNewFile();

                                mapper.writeValue(card, c);

                            }

                            for (Card c : project.getInProgressList()) {

                                File card = new File(projectDir + "/" + c.getTitolo() + ".json");
                                if (!card.exists()) card.createNewFile();

                                mapper.writeValue(card, c);

                            }

                            for (Card c : project.getToBeRevisedList()) {

                                File card = new File(projectDir + "/" + c.getTitolo() + ".json");
                                if (!card.exists()) card.createNewFile();

                                mapper.writeValue(card, c);

                            }

                            for (Card c : project.getDoneList()) {

                                File card = new File(projectDir + "/" + c.getTitolo() + ".json");
                                if (!card.exists()) card.createNewFile();

                                mapper.writeValue(card, c);

                            }
                        }
                    }

                }


            } catch (IOException e) {
            }
        }
    }


    private void dataRestore(File restoreDir) {

        System.out.println("[SERVER DATA MANAGER] Data restore started!");
        this.userJSON = new File(restoreDir + "/users.json");
        this.projectJSONMainFolder = new File(restoreDir + "/projects/");
        ObjectMapper mapper = new ObjectMapper();

        if (!restoreDir.exists()) restoreDir.mkdir();
        if (restoreDir.isDirectory()) {
            try {

                if (!userJSON.exists()) {

                    allUserList = new HashMap<>();
                    userJSON.createNewFile();

                } else {

                    com.fasterxml.jackson.core.type.TypeReference<HashMap<String, String>> typeRef = new com.fasterxml.jackson.core.type.TypeReference<HashMap<String, String>>() {
                    };
                    allUserList = mapper.readValue(userJSON, typeRef);

                    for (String user : this.allUserList.keySet()) {
                        System.out.println("[SERVER DATA MANAGER] Found user: " + user);
                        allUserListStatus.put(user, "offline");
                    }

                }

                if (!projectJSONMainFolder.exists()) projectJSONMainFolder.mkdir();
                if (projectJSONMainFolder.isDirectory()) {
                    File[] projectFolderList = projectJSONMainFolder.listFiles();

                    for (File projectFolder : projectFolderList) {

                        if (projectFolder.isDirectory()) {

                            Project dataProject = null;
                            File[] projectFiles = projectFolder.listFiles();
                            for (File pFile : projectFiles) {

                                String content;


                                if (pFile.getName().compareTo("project.txt") == 0) {


                                    content = mapper.readValue(pFile, String.class);
                                    String delims = "[#]+";
                                    String[] tokens = content.split(delims);


                                    dataProject = new Project(tokens[0], tokens[2]);
                                    System.out.println("[SERVER DATA MANAGER] Found project : " + dataProject.getProjectName() +  " (Multicast IP: " + dataProject.getMulticastIP()+ ")");
                                    String apex = "[, ]+";
                                    int limit = tokens[1].length();
                                    tokens[1] = tokens[1].substring(1, limit - 1);
                                    String[] memberList = tokens[1].split(apex);

                                    for (String member : memberList) dataProject.addMember(member);


                                }
                            }


                            if (dataProject != null) {
                                for (File pFile : projectFiles) {


                                    if (pFile.getName().contains(".json")) {

                                        Card deserializedCard = new ObjectMapper().readValue(pFile, Card.class);
                                        System.out.println("[SERVER DATA MANAGER] Found card : " + deserializedCard.getTitolo() +  " State: " + deserializedCard.getLastHistory());

                                        switch (deserializedCard.getLastHistory()) {


                                            case "To do":
                                                dataProject.addToDoList(deserializedCard);
                                                break;

                                            case "In progress":
                                                dataProject.addInProgressList(deserializedCard);
                                                break;

                                            case "To be revised":
                                                dataProject.addToBeRevisedList(deserializedCard);
                                                break;

                                            case "Done":
                                                dataProject.addDoneList(deserializedCard);
                                                break;


                                        }

                                    }

                                }


                            }

                            if (dataProject != null) projectList.put(dataProject.getProjectName(), dataProject);

                        }

                    }
                }
            } catch (JsonParseException e) {
                e.printStackTrace();
                System.exit(1);
            } catch (JsonMappingException e) {
                e.printStackTrace();
                System.exit(1);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

        }

        System.out.println("[SERVER DATA MANAGER] Data restore finished!\n");

    }


    public int registerCheckUsername(String username) {

        synchronized (allUserList) {
            if (!allUserList.isEmpty()) {
                if (allUserList.get(username) != null) return 1;
                return 0;
            }

            return 0;
        }

    }

    public void register(String username, String password) {

        synchronized (allUserList) {
            System.out.println("[SERVER - DATA MANAGER] New user: " + username + " " + password);
            allUserList.put(username, password);
            allUserListStatus.put(username, "offline");
            System.out.println("[SERVER - DATA MANAGER] User list: " + allUserList.toString());
        }


    }

    public HashMap<String, String> getUserList() {

        synchronized (allUserList) {
            return this.allUserList;
        }

    }

    public HashMap<String, String> getUserOnlineList() {
        synchronized (allUserListStatus) {
            return this.allUserListStatus;
        }
    }

    public String getProjectMulticastIP(String projectName) {

        try {
            synchronized (projectList.get(projectName)) {
                Project pr = projectList.get(projectName);
                return pr.getMulticastIP();

            }
        }catch (Exception e){
            return null;
        }
    }

    public void setUserStatus(String username, String status) {

        synchronized (allUserListStatus) {
            if(allUserListStatus.containsKey(username))
                 allUserListStatus.replace(username, status);
        }
        try {
            serverNotifier.update(username, status);
        }catch (RemoteException e){
            e.printStackTrace();
        }

    }

    public ArrayList<Project> getProjectList(String username) {

        ArrayList<Project> userProjectList = new ArrayList<>();
        synchronized (projectList) {

            for (Project pr : projectList.values()) {

                if (pr.isMember(username)) userProjectList.add(pr);
            }
        }
        return userProjectList;
    }

    public ArrayList<String> getProjectMembers(String project) {

        try{
            synchronized (projectList.get(project)) {
                return projectList.get(project).getMemberList();
            }
        }catch (Exception e){

            return null;
        }

    }

    public int newProject(String name, String authorName) {

        synchronized (projectList) {

            Project newProject = new Project(name, authorName, "");
            for (Project pr : projectList.values()) {

                if (pr.equals(newProject)) return 1;

            }

            Random random = new Random();
            String multicastIP = (random.nextInt(239 - 224) + 224) + "." + random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256);

            for (Project pr : projectList.values()) {

                if (pr.getMulticastIP().compareTo(multicastIP) == 0)
                    multicastIP = random.nextInt(224 - 239) + 224 + "." + random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256);

            }

            newProject = new Project(name, authorName, multicastIP);
            projectList.put(name, newProject);
            return 0;
        }

    }

    public int cancelProject(String projectName){
        int status;
        try {
            synchronized (projectList.get(projectName)) {
                if (projectList.get(projectName).done()) {

                    projectList.remove(projectName);
                    status = 0;
                } else status = 1;
            }

        }catch (Exception e){
            status = 2;
        }
        return status;
    }

    public Integer addCard(String project, String cardName, String descrizione){
        Project p = projectList.get(project);
        if(p != null) {
            if (p.addCard(cardName, descrizione)) return 0;
            else return 1;
        }
        return 2;
    }

    public Integer addMember(String project, String memberName){

        if(projectList.containsKey(project)) {
            synchronized (projectList.get(project)) {
                if (projectList.get(project).addMember(memberName)) return 0;
            }
            return 1;
        }
        return 2;
    }

    public String[] showCards(String projectName){

        String[] stringCardList = null;
        if (projectList.containsKey(projectName)) {

            ArrayList<String> cardList = projectList.get(projectName).showCards();
            if(cardList.size() == 0) return null;
            else stringCardList = cardList.toArray(new String[0]);
        }
        return stringCardList;
    }

    public String[] showCard(String projectName, String cardName){

        String[] stringCardInfo = null;
        if (projectList.containsKey(projectName)) {
            ArrayList<String> cardInfo = projectList.get(projectName).showCard(cardName);
            if (cardInfo.size() == 0) return null;
            else {
                stringCardInfo = new String[cardInfo.size()];
                for (int i = 0; i < cardInfo.size(); i++) stringCardInfo[i] = cardInfo.get(i);
            }
        }
        return stringCardInfo;
    }

    public Integer moveCard(String projectName, String cardName, String destionationList){

        Project p = projectList.get(projectName);
        if(p == null) return 4;
        return p.moveCard(cardName,destionationList);
    }

    public String[] getCardHistory(String projectName, String cardName){

        if(!projectList.containsKey(projectName)) return null;
        String [] cardHistory;
        ArrayList<String> history = projectList.get(projectName).getCardHistory(cardName);
        if(history == null) return null;
        else {

            cardHistory = new String[history.size()];
            for (int i = 0; i < history.size(); i++) {
                cardHistory[i] = history.get(i);

            }
            return cardHistory;

        }
    }


}

