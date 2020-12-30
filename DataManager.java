import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class DataManager  extends JsonSerializer<Project> {

    private HashMap<String,String> allUserList;
    private HashMap<String,String> allUserListStatus;
    private ServerUpdateNotify serverNotifier = null;
    //private HashMap<String, >

    private File userJSON = null;
    private File projectJSONMainFolder = null;
    private final ObjectMapper mapper; //ObjectMapper for json

    @JsonSerialize(keyUsing = DataManager.class)
    private HashMap<String, Project> projectList = new HashMap<>();


    public DataManager(ServerUpdateNotify serverNotifier){

        //Load initial data from file system
        this.mapper = new ObjectMapper();
        allUserListStatus = new HashMap<>();

        this.dataRestore(new File("./storage"));

        this.serverNotifier = serverNotifier;


    }

    public void dataWriter(File restoreDir) throws IOException {

        String jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(allUserList);

        if (!restoreDir.exists()) restoreDir.mkdir();
        if(restoreDir.isDirectory()) {
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
                            if(!projectList.containsKey(projectFolder.getName())){

                                for (File file: projectFolder.listFiles()) file.delete();

                                projectFolder.delete();
                            }
                            }
                        }

                    for (Project project : projectList.values()) {

                        File projectDir = new File(projectJSONMainFolder+"/"+project.getProjectName());
                        if(!projectDir.exists()) projectDir.mkdir();
                        if(projectDir.isDirectory()) {

                            File projectFile = new File(projectDir + "/project.txt");
                            if(!projectFile.exists()) projectFile.createNewFile();
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
                                    System.out.println("[SERVER] FOUND PROJECT " + dataProject.getProjectName() + dataProject.getMulticastIP());
                                    String apex = "[, ]+";
                                    int limit = tokens[1].length();
                                    tokens[1] = tokens[1].substring(1, limit - 1);
                                    String[] memberList = tokens[1].split(apex);

                                    for (String member : memberList) dataProject.addMember(member);


                                }
                            }


                                if(dataProject !=null) {
                                    for (File pFile : projectFiles) {


                                        System.out.println("[SERVER] FILENAME: " + pFile.getName());
                                        if (pFile.getName().contains(".json")) {

                                            Card deserializedCard = new ObjectMapper().readValue(pFile, Card.class);
                                            System.out.println("[SERVER] CARD DATA: " + deserializedCard.getTitolo() + " " + deserializedCard.getDescrizione() + deserializedCard.getHistory());

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

                                if(dataProject != null) projectList.put(dataProject.getProjectName(),dataProject);

                                }

                            }
                        }
                    } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
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

    public String getProjectMulticastIP(String projectName){

        synchronized (projectList.get(projectName)){
            Project pr = projectList.get(projectName);
            if(pr != null) return pr.getMulticastIP();
            return null;
        }
    }

    public void setUserStatus(String username, String status) throws RemoteException {

        synchronized (allUserListStatus) {
            allUserListStatus.replace(username, status);
        }
        serverNotifier.update(username,status);


    }

    public ArrayList<Project> getProjectList(String username){

        ArrayList<Project> userProjectList = new ArrayList<>();
        for (Project pr: projectList.values())  {

            if(pr.getMemberList().contains(username)) userProjectList.add(pr);
        }

        return userProjectList;
    }

    public ArrayList<String> getProjectMembers(String project){

        synchronized (projectList.get(project)){
            return projectList.get(project).getMemberList();
        }
    }
    public synchronized int newProject(String name, String authorName){


            Project newProject = new Project(name,authorName, "");
            for (Project pr: projectList.values()){

                if(pr.equals(newProject)) return 1;

            }

        Random random = new Random();
        String multicastIP = (random.nextInt(239 - 224) + 224) + "." + random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256);

        for (Project pr: projectList.values()){

            if(pr.getMulticastIP().compareTo(multicastIP) == 0) multicastIP = random.nextInt(224 - 239) + 224 + "." + random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256);

        }

        newProject = new Project(name,authorName, multicastIP);
        projectList.put(name, newProject);
            return 0;

    }

    public boolean cancelProject(String projectName){

            if(projectList.get(projectName).done()){

                projectList.remove(projectName);
                return true;
            }
            return false;
    }

    public Integer addCard(String project, String cardName, String descrizione){
        Project p = projectList.get(project);
        if(p != null) {
            if (projectList.get(project).addCard(cardName, descrizione)) return 0;
            else return 1;
        }
        return 2;
    }

    public Integer addMember(String project, String memberName){

        synchronized (projectList.get(project)){
            if(projectList.get(project).addMember(memberName)) return 0;
             return 1;
        }
    }

    public String[] showCards(String projectName){

        String[] stringCardList;
        ArrayList<String> cardList = projectList.get(projectName).showCards();
        if(cardList.size() == 0) return null;
        else {
            stringCardList = new String[cardList.size()];
            for (int i = 0; i < cardList.size(); i++) stringCardList[i] = cardList.get(i);
        }
        return stringCardList;
    }

    public String[] showCard(String projectName, String cardName){

        String[] stringCardInfo;
        ArrayList<String> cardInfo = projectList.get(projectName).showCard(cardName);
        if(cardInfo.size() == 0) return null;
        else {
            stringCardInfo = new String[cardInfo.size()];
            for (int i = 0; i < cardInfo.size(); i++) stringCardInfo[i] = cardInfo.get(i);
        }
        return stringCardInfo;
    }

    public Integer moveCard(String projectName, String cardName, String destionationList){

        if(projectList.get(projectName) == null) return 4;
        return projectList.get(projectName).moveCard(cardName,destionationList);
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


    @Override
    public void serialize(Project project, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {


        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, project);
        jsonGenerator.writeFieldName(writer.toString());
    }
}

