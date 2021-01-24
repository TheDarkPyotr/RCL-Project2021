import java.io.*;
import java.rmi.RemoteException;
import java.util.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DataManager {

    //Declare user list with username and password
    private HashMap<String, String> allUserList;
    //Declare user list with username and status
    private HashMap<String, String> allUserListStatus;
    //Declare project list with projectName and Project object
    private HashMap<String, Project> projectList = null;
    //Declare notifier for RMI callback on user event
    private ServerUpdateNotify serverNotifier = null;
    //Declare file for user serialization/deserialization
    private File userJSON = null;
    //Declare file for project serialization/deserialization
    private File projectJSONMainFolder = null;
    //Declare objectMapper for serialization/deserialization
    private final ObjectMapper mapper;
    //Declare storage name folder for restore/store data
    private static final String STORAGE_DIR = "./storage";

    public DataManager(ServerUpdateNotify serverNotifier) {

        //Initialize objectMapper
        this.mapper = new ObjectMapper();
        //Initialize projectList
        projectList = new HashMap<>();
        //Initialize user list status
        allUserListStatus = new HashMap<>();

        //Restore data from file system in STORAGE_DIR
        this.dataRestore(new File(STORAGE_DIR));
        //Initialize for RMI callback on user event
        this.serverNotifier = serverNotifier;

    }

    public synchronized void shutdown() throws IOException {
        //Method invoked to save data on server shutdown
        System.out.println("[SERVER DATA MANAGER] Storing data");
        this.dataWriter(new File(STORAGE_DIR));
        System.out.println("[SERVER DATA MANAGER] Data saved!");

    }

    public int login(String username, String password) {

        int response = 0;
        if (username == null || password == null) response = 4;
        else {
            synchronized (allUserList) {
                if (!allUserList.containsKey(username)) response = 1;
                else if (allUserList.get(username).compareTo(password) != 0) response = 2;
                else if (this.getUserOnlineList().containsKey(username) && this.getUserOnlineList().get(username).compareTo("online") == 0)
                    response = 3;

                if (response == 0) this.setUserStatus(username, "online");

            }
        }


        return response;
    }

    public void writeUser(File restoreDir){

        if (!restoreDir.exists()) restoreDir.mkdir();
        if (restoreDir.isDirectory()) {
            try {
                String jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(allUserList);

                if (!userJSON.exists()) {

                    userJSON.createNewFile();

                    //Serialize allUserList to JSON
                    mapper.writeValue(userJSON, allUserList);

                    System.out.println(jsonResult);
                } else {
                    mapper.writeValue(userJSON, allUserList);
                }
            } catch (IOException e) {
                System.out.println("[SERVER DATA MANAGER] Error while writing user.json file!\n");
            }
            }

    }

    public void dataWriter(File restoreDir) {


        if (!restoreDir.exists()) restoreDir.mkdir();
        if (restoreDir.isDirectory()) {
            try {
                synchronized (projectList) {
                    projectJSONMainFolder = new File(restoreDir + "/projects");
                    if (!projectJSONMainFolder.exists()) projectJSONMainFolder.mkdir();
                    if (projectJSONMainFolder.isDirectory()) {

                        //Delete project folder and files of project already eliminated in projectList data structure
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
                        /* Project information are splitted in n+1 files (where n = #cards).
                           Project details like project name, multicast IP and project member list are writed in  "project.txt"
                           file as: projectName#multicastIP#[userMember1,userMember2, .. , userMemberN]
                           Cards information are serialized and writed in JSON (in file "cardName.json").

                         */

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

                }
            } catch (IOException e) {
                System.out.println("[SERVER DATA MANAGER] Error while writing projects data!\n");
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

                //Restore user data
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

                //Restore project data
                if (!projectJSONMainFolder.exists()) projectJSONMainFolder.mkdir();
                if (projectJSONMainFolder.isDirectory()) {
                    File[] projectFolderList = projectJSONMainFolder.listFiles();

                    for (File projectFolder : projectFolderList) {
                        //Check if its a project folder
                        if (projectFolder.isDirectory()) {

                            Project dataProject = null;
                            File[] projectFiles = projectFolder.listFiles();
                            for (File pFile : projectFiles) {

                                String content;
                                //Search project.txt file that contains projectName,multicastIP and project member ist
                                if (pFile.getName().compareTo("project.txt") == 0) {


                                    content = mapper.readValue(pFile, String.class);
                                    String delims = "[#]+";
                                    String[] tokens = content.split(delims);

                                    //Create new project with tokens[0] = projectName, tokens[2] = multicastIP
                                    dataProject = new Project(tokens[0], tokens[2]);
                                    System.out.println("[SERVER DATA MANAGER] Found project : " + dataProject.getProjectName() + " (Multicast IP: " + dataProject.getMulticastIP() + ")");
                                    String apex = "[, ]+";

                                    //Parse project member list
                                    int limit = tokens[1].length();
                                    tokens[1] = tokens[1].substring(1, limit - 1);
                                    String[] memberList = tokens[1].split(apex);

                                    //Add every member to project
                                    for (String member : memberList) dataProject.addMember(member);


                                }
                            }

                            //If project.txt was found
                            if (dataProject != null) {
                                for (File pFile : projectFiles) {
                                    //For every card file (.json)
                                    if (pFile.getName().contains(".json")) {

                                        //Deserialize card
                                        Card deserializedCard = new ObjectMapper().readValue(pFile, Card.class);
                                        System.out.println("[SERVER DATA MANAGER] Found card : " + deserializedCard.getTitolo() + " State: " + deserializedCard.getLastHistory());

                                        //Restore last card status and update deserializedCard status in project dataProject
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

                                //If project.txt was found, add project to projectList data structure
                                projectList.put(dataProject.getProjectName(), dataProject);
                            }
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
                //Check if username is already taken
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
            //Update server user status data structure
            setUserStatus(username, "offline");
            writeUser(new File(STORAGE_DIR));

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

        synchronized (projectList) {
            if (projectList.containsKey(projectName)) {
                return projectList.get(projectName).getMulticastIP();
            } else return null;
        }
    }

    public int logout(String username) {

        //Check if user username is already logged out
        if (this.getUserOnlineList().get(username).compareTo("offline") == 0) return 1;
        //Update server user status data structure
        else this.setUserStatus(username, "offline");

        return 0;
    }

    public void setUserStatus(String username, String status) {
    //Update server user status data structure
        synchronized (allUserListStatus) {
            //If username (key) exists, update status (value)
            if (allUserListStatus.containsKey(username)) allUserListStatus.replace(username, status);
            //Else insert new user username with status
            else allUserListStatus.put(username, status);

            try {
                //Do RMI callback on user event
                serverNotifier.update(username, status);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<Project> getProjectList(String username) {

        //Project list of username
        ArrayList<Project> userProjectList = new ArrayList<>();
        synchronized (projectList) {

            for (Project pr : projectList.values()) {
                //Add pr to userProjectList
                if (pr.isMember(username)) userProjectList.add(pr);
            }
        }
        return userProjectList;
    }

    public ArrayList<String> getProjectMembers(String project) {

        synchronized (projectList) {
            if (projectList.containsKey(project)) return projectList.get(project).getMemberList();
            else return null;
        }
    }

    public int newProject(String name, String authorName) {

        synchronized (projectList) {

            //Create fake project to check if project "name" already exists
            Project newProject = new Project(name, authorName, "");
            for (Project pr : projectList.values()) if (pr.equals(newProject)) return 1;
            boolean unique = true;

            //Generate multicast IP address based on class D address format
            Random random = new Random();
            int part1 = 224;
            int part2 = 0;
            int part3 = 0;
            int part4 = 0;
            String multicastIP = part1+"."+part2+"."+part3+"."+part4;
            //String multicastIP = (random.nextInt(239 - 224) + 224) + "." + random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256);

            //Check if generate multicast ip is already taken by another project
            boolean limit = false;
            while(unique && !limit) {
                unique = false;

                for (Project pr : projectList.values()) {

                    //If equals, generate another mulicast IP
                    if (pr.getMulticastIP().compareTo(multicastIP) == 0) {

                        unique = true;
                        if(part4 < 255) part4++;
                        else if(part3 < 255){
                            part4 = 0;
                            part3++;

                        }
                        else if(part2 < 255){
                            part4 = 0;
                            part3 = 0;
                            part2++;
                        }
                        else if (part1 < 239) {
                            part4 = 0;
                            part3 = 0;
                            part2 = 0;
                            part1++;
                        } else limit = true;
                        //multicastIP = random.nextInt(239-224) + 224 + "." + random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256);
                        unique = true;
                        multicastIP = part1+"."+part2+"."+part3+"."+part4;
                        break;
                    }
                }
            }

            //Create new project
            if(limit) return 2;
            newProject = new Project(name, authorName, multicastIP);
            //Update server project list data structure
            projectList.put(name, newProject);
            return 0;
        }

    }

    public int cancelProject(String projectName) {
        int status = 2;

        synchronized (projectList) {
            if (projectList.containsKey(projectName)) {
                //Check if all cards are in done list
                if (projectList.get(projectName).done()) {

                    projectList.remove(projectName);
                    status = 0;
                } else status = 1;
            }

        }
        return status;
    }

    public Integer addCard(String project, String cardName, String descrizione){

        synchronized (projectList) {
            if(projectList.containsKey(project)) {
                if (projectList.get(project).addCard(cardName, descrizione)) return 0;
                else return 1;
            }else return 2;
        }
    }

    public Integer addMember(String project, String memberName){

        synchronized (projectList) {
            if (projectList.containsKey(project)) {
                if (projectList.get(project).addMember(memberName)) return 0;
                else return 1;
            } else return 2;
        }
    }

    public String[] showCards(String projectName){

        synchronized (projectList) {
            if (projectList.containsKey(projectName)) {

                ArrayList<String> cardList = projectList.get(projectName).showCards();
                if (cardList.size() == 0) return null;
                //Return array string of card list
                else return cardList.toArray(new String[0]);
            } else return null;
        }
    }

    public String[] showCard(String projectName, String cardName){

        String[] stringCardInfo = null;
        synchronized (projectList) {
            if (projectList.containsKey(projectName)) {
                ArrayList<String> cardInfo = projectList.get(projectName).showCard(cardName);
                if (cardInfo.size() == 0) return null;
                else {
                    //Return cardName info (status, description) on string array
                    stringCardInfo = new String[cardInfo.size()];
                    for (int i = 0; i < cardInfo.size(); i++) stringCardInfo[i] = cardInfo.get(i);
                }
            } else return null;
        }

        return stringCardInfo;
    }

    public Integer moveCard(String projectName, String cardName, String destionationList){

        synchronized (projectList) {
            if (projectList.containsKey(projectName)) {
                return projectList.get(projectName).moveCard(cardName, destionationList);
            } else return 4;
        }
    }

    public String[] getCardHistory(String projectName, String cardName) {

        String[] cardHistory;
        synchronized (projectList) {
            if (projectList.containsKey(projectName)) {
                ArrayList<String> history = projectList.get(projectName).getCardHistory(cardName);
                if (history == null) return null;
                else {
                    //Return cardName history on array string structure
                    cardHistory = new String[history.size()];
                    for (int i = 0; i < history.size(); i++) {
                        cardHistory[i] = history.get(i);

                    }
                    return cardHistory;

                }
            } else return null;
        }
    }

}

